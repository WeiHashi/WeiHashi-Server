package cn.devmeteor.weihashi.controller

import cn.devmeteor.weihashi.dao.*
import cn.devmeteor.weihashi.data.*
import cn.devmeteor.weihashi.util.OSSUtil
import cn.devmeteor.weihashi.util.Util
import com.vdurmont.emoji.EmojiParser
import org.json.JSONObject
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile
import javax.servlet.http.HttpServletRequest

@RestController
class SuController {

    @Autowired
    lateinit var classDao: ClassDao

    @Autowired
    lateinit var noticeDao: NoticeDao

    @Autowired
    lateinit var questionnaireDao: QuestionnaireDao

    @Autowired
    lateinit var userDao: UserDao

    @Autowired
    lateinit var instituteDao: InstituteDao

    @Autowired
    lateinit var classMemberDao: ClassMemberDao

    @Autowired
    lateinit var questionnaireCollectionDao: QuestionnaireCollectionDao

    @Autowired
    lateinit var suDeviceDao: SuDeviceDao

    @Autowired
    lateinit var bannerDao: BannerDao


    @Deprecated("已废弃", ReplaceWith("/v2/su/overview"))
    @GetMapping("/suGetOverview")
    fun getOverview(): Response {
        val dataMap: MutableMap<String, String> = HashMap()
        dataMap["userCount"] = userDao.getUserCount().toString()
        dataMap["bindedCount"] = userDao.getUserCountBinded().toString()
        dataMap["todayCount"] = userDao.getUserCountToday().toString()
        dataMap["noticeCount"] = noticeDao.getNoticeCount().toString()
        dataMap["questionnaireCount"] = questionnaireDao.getQuestionnaireCount().toString()
        dataMap["classCount"] = classDao.getClassCount().toString()
        return Response(ResultCode.GET_SUCCESS, dataMap)
    }

    @GetMapping("/suGetBannerList")
    fun getBannerList(page: Int, limit: Int): Response =
        Response(
            ResultCode.GET_SUCCESS, hashMapOf(
                "total" to bannerDao.getBannerCount(),
                "list" to bannerDao.getBannerList((page - 1) * limit, limit)
            )
        )

    @PostMapping("/suUploadBanner")
    fun uploadBanner(@RequestBody file: MultipartFile): Map<String, Int> = try {
        if (file.isEmpty) {
            hashMapOf("code" to 500)
        } else {
            OSSUtil.add("whs/banner/${file.originalFilename}", file)
            bannerDao.addBanner(
                Banner(
                    Util.createObjId(),
                    "https://image.devmeteor.cn/whs/banner/${file.originalFilename}",
                    0, "", 10, true
                )
            )
            hashMapOf("code" to 200)
        }
    } catch (e: Exception) {
        hashMapOf("code" to 500)
    }

    @DeleteMapping("/suDeleteBanner/{id}")
    fun deleteBanner(@PathVariable id: String, name: String) {
        OSSUtil.delete("whs/banner/$name")
        bannerDao.deleteBanner(id)
    }

    @PutMapping("/suUpdateBanner")
    fun updateBanner(id: String, field: String, value: String) {
        if (field == "content_id") {
            bannerDao.updateBannerById(id, "type", if (value.isBlank()) "0" else "1")
        }
        bannerDao.updateBannerById(id, field, value)
    }

    @PutMapping("/suEnableBanner")
    fun enableBanner(id: String, enabled: Boolean) =
        bannerDao.enableBanner(id, enabled)

    @GetMapping("/suGetUserList")
    fun getUserList(page: Int, limit: Int): Response {
        val list = userDao.getUserList((page - 1) * limit, limit)
        for (user in list) {
            user.nickname = EmojiParser.parseToUnicode(user.nickname)
        }
        return Response(ResultCode.GET_SUCCESS, hashMapOf("total" to userDao.getUserCount(), "list" to list))
    }

    @GetMapping("/suGetInstituteList")
    fun getInstituteList(): Response {
        val list = instituteDao.getInstituteList()
        return Response(ResultCode.GET_SUCCESS, list)
    }

    @PutMapping("/suUpdateInstitute")
    fun updateInstitute(institute_id: String, field: String, value: String) =
        instituteDao.updateInstituteById(institute_id, field, value)

    @GetMapping("/suGetClassListOfInstitute")
    fun getClassList(institute_id: String, page: Int, limit: Int): Response {
        val list = classDao.getClassList(institute_id, (page - 1) * limit, limit)
        return Response(ResultCode.GET_SUCCESS, hashMapOf("total" to classDao.getClassCount(), "list" to list))
    }

    @PostMapping("/suAddAdmin")
    fun addAdmin(name: String, type: Int, to_ins: String?): Response {
        val id = Util.createAccountId()
        val aId = id["id"]
        val username = id["username"]
        val password = id["password"]
        return when (type) {
            0 -> {
                instituteDao.addInstitute(Institute(aId!!, name, username!!, password!!, null))
                Response(ResultCode.ADD_SUCCESS, id)
            }
            1 -> {
                classDao.addClass(Classes(aId!!, name, to_ins!!, username!!, password!!, null))
                Response(ResultCode.ADD_SUCCESS, id)
            }
            else -> Response(ResultCode.INVALID_PARAM)
        }
    }

    @DeleteMapping("/suDeleteAdmin/{id}")
    fun deleteAdmin(@PathVariable id: String) {
        questionnaireDao.deleteQuestionnaireBySender("{\"id_type\":1,\"id\":\"$id\"}")
        noticeDao.deleteNoticeBySender("{\"id_type\":1,\"id\":\"$id\"}")
        classMemberDao.deleteMemberAfterClassDeleted(id)
        classDao.deleteClass(id)
    }

    @PutMapping("/suUpdateClass")
    fun updateClass(class_id: String, field: String, value: String) {
        println(class_id + field + value)
        classDao.updateClassById(class_id, field, value)
    }

    @GetMapping("/suGetNoticeList")
    fun getNoticeList(page: Int, limit: Int): Response {
        val list = noticeDao.getNoticeList((page - 1) * limit, limit)
        for (notice in list) {
            if (notice.sender == "Meteor")
                continue
            val id = JSONObject(notice.sender).getString("id")
            notice.sender = when (JSONObject(notice.sender).getInt("id_type")) {
                0 -> {
                    if (instituteDao.getInstituteNameById(id) != null) classDao.getClassNameById(id) else "该账号已不存在"
                }
                1 -> {
                    if (classDao.getClassNameById(id) != null) classDao.getClassNameById(id) else "该账号已不存在"
                }
                2 -> id
                else -> "该账号已不存在"
            }
        }
        return Response(ResultCode.GET_SUCCESS, hashMapOf("total" to noticeDao.getNoticeCount(), "list" to list))
    }

    @PostMapping("/suAddNotice")
    fun addNotice(@RequestBody notice: Notice, @RequestHeader top: Boolean) {
        notice.obj_id = Util.createObjId()
        notice.sender = "Meteor"
        notice.receiver = "{\"institute\":[],\"classes\":[],\"students\":[]}"
        notice.cate = 0
        notice.type = 1
        notice.top_level = if (top) TopLevel.SU_TOP.code else TopLevel.NOT_TOP.code
        noticeDao.addNotice(notice)
    }

    @DeleteMapping("/suDeleteNotice/{objId}")
    fun deleteNotice(@PathVariable objId: String) = noticeDao.deleteNoticeByObjId(objId)

    @PutMapping("/suUpdateNotice")
    fun updateNotice(obj_id: String, field: String, value: String) = noticeDao.updateNoticeById(obj_id, field, value)

    @PutMapping("/suSetNoticeTop")
    fun setNoticeTop(obj_id: String, top: Boolean) =
        noticeDao.setTop(obj_id, if (top) TopLevel.SU_TOP.code else TopLevel.NOT_TOP.code)

    @GetMapping("/suGetQuestionnaireList")
    fun getQuestionnaireList(page: Int, limit: Int): Response {
        val list = questionnaireDao.getQuestionnaireList((page - 1) * limit, limit)
        for (questionnaire in list) {
            if (questionnaire.sender == "Meteor")
                continue
            val id = JSONObject(questionnaire.sender).getString("id")
            questionnaire.sender = when (JSONObject(questionnaire.sender).getInt("id_type")) {
                0 -> {
                    if (instituteDao.getInstituteNameById(id) != null) classDao.getClassNameById(id) else "该账号已不存在"
                }
                1 -> {
                    if (classDao.getClassNameById(id) != null) classDao.getClassNameById(id) else "该账号已不存在"
                }
                2 -> id
                else -> "该账号已不存在"
            }
        }
        return Response(
            ResultCode.GET_SUCCESS,
            hashMapOf("total" to questionnaireDao.getQuestionnaireCount(), "list" to list)
        )
    }

    @PostMapping("/suAddQuestionnaire")
    fun addQuestionnaire(@RequestBody questionnaire: Questionnaire, @RequestHeader top: Boolean) {
        questionnaire.obj_id = Util.createObjId()
        questionnaire.sender = "Meteor"
        questionnaire.receiver = "{\"institute\":[],\"classes\":[],\"students\":[]}"
        questionnaire.type = 1
        questionnaire.top_level = if (top) TopLevel.SU_TOP.code else TopLevel.NOT_TOP.code
        questionnaireDao.addQuestionnaire(questionnaire)
    }

    @DeleteMapping("/suDeleteQuestionnaire/{objId}")
    fun deleteQuestionnaire(@PathVariable objId: String) {
        questionnaireCollectionDao.deleteQuestionnaireCollectionByQid(objId)
        questionnaireDao.deleteQuestionnaireByObjId(objId)
    }

    @PutMapping("/suUpdateQuestionnaire")
    fun updateQuestionnaire(obj_id: String, field: String, value: String) =
        questionnaireDao.updateQuestionnaireById(obj_id, field, value)

    @PutMapping("/suSetQuestionnaireTop")
    fun setQuestionnaireTop(obj_id: String, top: Boolean) =
        questionnaireDao.setTop(obj_id, if (top) TopLevel.SU_TOP.code else TopLevel.NOT_TOP.code)

    @GetMapping("/suGetQuestionnaireCollection")
    fun getQuestionnaireCollection(qid: String, page: Int, limit: Int): Response {
        val list = questionnaireCollectionDao.getQuestionnaireCollectionByQid(qid, (page - 1) * limit, limit)
        for (map in list) {
            val info = userDao.getStudentInfoById(map["openid"] as String)
            map["name"] =
                if (info!!.getValue("name") == "") info.getValue("nickname") else info.getValue("name")
        }
        val total = questionnaireCollectionDao.countQuestionnaireCollectionByQid(qid)
        return Response(ResultCode.GET_SUCCESS, hashMapOf("total" to total, "list" to list))
    }

    @DeleteMapping("/suDeleteQuestionnaireCollection/{id}")
    fun deleteQuestionnaireCollection(@PathVariable id: String) =
        questionnaireCollectionDao.deleteQuestionnaireCollectionByObjId(id)

    @Deprecated("因验证策略修改，已废弃")
    @GetMapping("/suVerifyDevice")
    fun verifyDevice(request: HttpServletRequest, hostName: String, mac: String): Response {
        val ip = request.remoteAddr
        val macInvalid = !suDeviceDao.verifyMac(mac)
        if (macInvalid) {
            suDeviceDao.addDevice(mac, hostName, ip)
            return Response(ResultCode.INVALID_DEVICE)
        }
        val allow = suDeviceDao.getAllowByMac(mac)
        if (!allow)
            return Response(ResultCode.DEVICE_VERIFYING)
        suDeviceDao.updateDeviceInfo(mac, hostName, ip)
        return Response(ResultCode.VERIFY_OK)
    }

}