package cn.devmeteor.weihashi.controller

import cn.devmeteor.weihashi.dao.*
import cn.devmeteor.weihashi.data.*
import cn.devmeteor.weihashi.service.impl.UserImpl
import cn.devmeteor.weihashi.util.OSSUtil
import cn.devmeteor.weihashi.util.RedisUtil
import cn.devmeteor.weihashi.util.Util
import org.json.JSONArray
import org.json.JSONObject
import org.jsoup.Jsoup
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile
import java.io.File
import java.io.FileOutputStream
import java.time.Duration

@RestController
@RequestMapping("/v2/su")
class SuControllerV2 {

    companion object {
        const val KEY_UV_RECORD = "uv_record"
        const val KEY_DU_RECORD = "du_record"
    }

    @Autowired
    lateinit var testApplicationDao: TestApplicationDao

    @Autowired
    lateinit var userDao: UserDao

    @Autowired
    lateinit var noticeDao: NoticeDao

    @Autowired
    lateinit var questionnaireDao: QuestionnaireDao

    @Autowired
    lateinit var classDao: ClassDao

    @Autowired
    lateinit var redisUtil: RedisUtil

    @GetMapping("/overview")
    fun getOverview(): Response = Response(
        ResultCode.GET_SUCCESS, mapOf(
            "userCount" to userDao.getUserCount(),
            "bindedCount" to userDao.getUserCountBinded(),
            "todayCount" to redisUtil.pfCount(UserImpl.KEY_UV),
            "dailyCount" to redisUtil.trySetDefaultAndGet(UserImpl.KEY_DU, 0),
            "noticeCount" to noticeDao.getNoticeCount(),
            "questionnaireCount" to questionnaireDao.getQuestionnaireCount(),
            "classCount" to classDao.getClassCount(),
            "uv_rec_week" to redisUtil.lRange<DateValueRecord<Int>>(KEY_UV_RECORD, 0, 6),
            "du_rec_week" to redisUtil.lRange<DateValueRecord<Int>>(KEY_DU_RECORD, 0, 6)
        )
    )

    @PostMapping("/cache/upload", produces = ["application/json;charset=UTF-8"])
    fun sendNotice(
        @RequestParam("upload_file") file: MultipartFile,
        @RequestParam("original_filename") name: String
    ): String = try {
        val pointIndex = name.lastIndexOf('.')
        val prefix = name.substring(0 until pointIndex).replace(" ", "_")
        val postfix = name.substring(pointIndex until name.length)
        val cachePrefix = Util.getMd5("${prefix}_${System.currentTimeMillis()}")
        val cachePath = "imageCache/$cachePrefix$postfix"
        FileOutputStream(cachePath).use {
            it.write(file.bytes)
        }
        val cacheUrl = "https://devmeteor.cn:8080/cache/image/$cachePrefix$postfix"
        redisUtil.set(cachePath, 0, Duration.ofHours(1))
        """{"success":true,"msg":"上传成功","file_path":"$cacheUrl"}"""
    } catch (e: Exception) {
        e.printStackTrace()
        """{"success":false,"msg":"上传失败"}"""
    }

    @PostMapping("notice/publish")
    fun addNotice(@RequestBody notice: Notice, @RequestHeader top: Boolean) {
        val objId = Util.createObjId()
        notice.detail = getDetail(objId, notice)
        notice.obj_id = objId
        notice.sender = "Meteor"
        notice.receiver = "{\"institute\":[],\"classes\":[],\"students\":[]}"
        notice.cate = 0
        notice.type = 1
        notice.top_level = if (top) TopLevel.SU_TOP.code else TopLevel.NOT_TOP.code
        noticeDao.addNotice(notice)
    }

    @PutMapping("/notice/content/edit")
    fun updateNoticeContent(@RequestParam("obj_id") objId: String, value: String) {
        val notice = Notice(
            null,
            null,
            "",
            value,
            null,
            null,
            null,
            0,
            null,
            null
        )
        val detail = getDetail(objId, notice)
        val content = notice.content
        noticeDao.updateNoticeByIdV2(objId, content, detail)
    }

    @PutMapping("/testPermission")
    fun enableTest(openid: String, version: String, enabled: Boolean) {
        testApplicationDao.updateTestFlag(openid, version, enabled)
    }

    @GetMapping("/testPermission")
    fun getApplicationList(
        page: Int,
        limit: Int,
        @RequestParam(defaultValue = "false") enabled: Boolean,
        @RequestParam(defaultValue = "latest") version: String
    ): Response {
        if (version != "latest" && version != "all" && !version.matches(Regex("[0-9]+\\.[0-9]+\\.[0-9]+"))) {
            return Response(ResultCode.INVALID_PARAM)
        }
        val fVersion = when (version) {
            "latest" -> testApplicationDao.getLatestVersion()
            "all" -> "%"
            else -> version
        }
        val count = testApplicationDao.getApplicationCount(enabled, fVersion)
        val list = testApplicationDao.getApplicationList((page - 1) * limit, limit, enabled, fVersion)
        return Response(ResultCode.GET_SUCCESS, mapOf("total" to count, "list" to list))
    }

    @GetMapping("/testPermission/versions")
    fun getVersionList(): Response =
        Response(ResultCode.GET_SUCCESS, testApplicationDao.getVersionList())


    private fun getDetail(objId: String, notice: Notice): String {
        val content = notice.content
        val array = JSONArray()
        val map: HashMap<String, String> = HashMap()
        val doc = Jsoup.parse(content)
        doc.getElementsByTag("p").forEach { p ->
            p.childNodes().forEachIndexed { index, node ->
                when (node.nodeName()) {
                    "#text" -> {
                        var contentString = node.toString().replace("&nbsp;", " ")
                        if (contentString.isNotBlank()) {
                            try {
                                if (p.hasAttr("style") && index == 0) {
                                    val styles = p.attr("style")
                                    styles.split(";").forEach { style ->
                                        if (style.isNotBlank()) {
                                            val name = style.split(":")[0]
                                            if (name == "margin-left") {
                                                val spaceCount = try {
                                                    style.split(":")[1]
                                                        .replace(" ", "")
                                                        .replace("px", "")
                                                        .toInt() / 10
                                                } catch (e: Exception) {
                                                    0
                                                }
                                                repeat(spaceCount) {
                                                    contentString = " $contentString"
                                                }
                                            }
                                        }
                                    }
                                }
                            } catch (e: Exception) {
                            }
                            array.put(JSONObject().apply {
                                put("type", 1)
                                put("content", contentString)
                            })
                        }
                    }
                    "img" -> {
                        val src = node.attr("src")
                        val realUrl = if (src.startsWith("https://devmeteor.cn:8080")) {
                            if (map.containsKey(src))
                                map[src]
                            else
                                getRealUrl(objId, src, map)
                        } else src
                        if (realUrl != null) {
                            array.put(JSONObject().apply {
                                put("type", 2)
                                put("content", realUrl)
                            })
                        }
                    }
                }
            }
        }
        map.forEach { (k, v) ->
            notice.content = notice.content.replace(k, v)
        }
        return array.toString()
    }

    private fun getRealUrl(objId: String, cacheUrl: String, map: HashMap<String, String>): String? {
        val dividerIndex = cacheUrl.lastIndexOf('/')
        val filename = cacheUrl.substring(dividerIndex + 1 until cacheUrl.length)
        val file = File("imageCache/$filename")
        if (!file.exists()) {
            return null
        }
        val realPath = "whs/notice/$objId/$filename"
        OSSUtil.add(realPath, file) ?: return null
        val realUrl = "https://image.devmeteor.cn/$realPath"
        map[cacheUrl] = realUrl
        return realUrl
    }

}