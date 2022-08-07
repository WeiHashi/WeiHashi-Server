package cn.devmeteor.weihashi.service.impl

import cn.devmeteor.weihashi.controller.JwController
import cn.devmeteor.weihashi.dao.UserDao
import cn.devmeteor.weihashi.data.*
import cn.devmeteor.weihashi.service.JwService
import cn.devmeteor.weihashi.util.Util
import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory
import org.json.JSONObject
import org.jsoup.Connection
import org.jsoup.Jsoup
import org.jsoup.nodes.Attributes
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element
import org.jsoup.parser.Tag
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import java.io.File
import java.io.FileWriter
import java.util.*

@Service
class JwImpl : JwService {

    private val jwUrl = "http://jwpt.hrbnu.edu.cn"
    private val loginUrl = "$jwUrl/jsxsd/xk/LoginToXk"
    private val infoUrl = "$jwUrl/jsxsd/framework/xsMain_new.jsp?t1=1"
    private val gradeUrl = "$jwUrl/jsxsd/kscj/cjcx_list"
    private val pyfaUrl = "$jwUrl/jsxsd/pyfa/topyfamx"
    private val lessonUrl = "$jwUrl/jsxsd/xskb/xskb_list.do"
    private val log: Log = LogFactory.getLog(JwController::class.java)

    @Autowired
    lateinit var userDao: UserDao

    override fun login(openid: String, username: String, password: String): Response = try {
        val connection = constructConnection(HashMap(), loginUrl)
        connection.data("userAccount", username)
        connection.data("userPassword", "")
        connection.data("encoded", Util.jwEncode(username, password))
        val res = connection.method(Connection.Method.POST).execute()
        when {
            res.body().contains("用户名或密码错误") -> Response(ResultCode.LOGIN_FAIL)
            else -> {
                val document = Jsoup.connect(infoUrl)
                    .cookies(res.cookies())
                    .headers(Util.constructHeader)
                    .userAgent("Mozilla/5.0( X11;Ubuntu;Linux x86_64 ;rv:61.0) Gecko20100101Firefox/61.0")
                    .followRedirects(true)
                    .get()
                val elements = document.getElementsByClass("middletopttxlr")[0].getElementsByClass("middletopdwxxcont")
                val name = elements[1].text()
                if (name == "康广慧") {
                    res.cookies()["name"] = "康广慧"
                }
                userDao.updateJwInfo(openid, name, username)
                log.info(name + "(" + username + ")于" + Util.date2String(Date()) + "登录成功")
                Response(
                    ResultCode.LOGIN_OK,
                    hashMapOf("name" to name, "cookies" to map2Json(res.cookies()).toString())
                )
            }
        }
    } catch (e: Exception) {
        log.error("连接教务平台失败")
        e.printStackTrace()
        Response(ResultCode.CONNECT_ERROR)
    }

    override fun getGrade(cookieString: String): Response = try {
        var document = Jsoup.connect(gradeUrl)
            .cookies(string2Map(cookieString))
            .headers(Util.constructHeader)
            .userAgent("Mozilla/5.0( X11;Ubuntu;Linux x86_64 ;rv:61.0) Gecko20100101Firefox/61.0")
            .followRedirects(true)
            .get()
        if (document.getElementsByTag("title")[0].text() == "登录")
            Response(ResultCode.NEED_LOGIN)
        else {
            val table = document.getElementsByTag("table")[0]
            val statistics = table.parent()?.ownText()?.split(":", " ")!!
            var trs = table.getElementsByTag("tr")
            val cjs = ArrayList<NCJ>()
            for (i in 1 until trs.size) {
                val tds = trs[i].children()
                try {
                    cjs.add(
                        NCJ(
                            tds[1].text(),
                            tds[3].text(),
                            tds[5].text(),
                            tds[7].text(),
                            tds[8].text(),
                            tds[11].text(),
                            tds[12].text(),
                            tds[14].text()
                        )
                    )
                } catch (e: Exception) {
                    pyfaErrorOutput("cj${System.currentTimeMillis()}", document)
                    log.info("cj${System.currentTimeMillis()}:${ResultCode.RESOLVE_ERROR.msg}")
                }
            }
            try {
                document = Jsoup.connect(infoUrl)
                    .cookies(string2Map(cookieString))
                    .headers(Util.constructHeader)
                    .userAgent("Mozilla/5.0( X11;Ubuntu;Linux x86_64 ;rv:61.0) Gecko20100101Firefox/61.0")
                    .followRedirects(true)
                    .get()
                val elements = document.getElementsByClass("middletopttxlr")[0].getElementsByClass("middletopdwxxcont")
                val enterYear = elements[2].text().substring(0, 4)
                val name = elements[1].text()
                document = Jsoup.connect(pyfaUrl)
                    .cookies(string2Map(cookieString))
                    .headers(Util.constructHeader)
                    .userAgent("Mozilla/5.0( X11;Ubuntu;Linux x86_64 ;rv:61.0) Gecko20100101Firefox/61.0")
                    .followRedirects(true)
                    .get()
                try {
                    val tbody = document.getElementsByTag("TBODY")[1]
                    trs = tbody.getElementsByTag("tr")
                    for (i in 2 until trs.size) {
                        val tr = trs[i]
                        val tdCount = tr.getElementsByTag("td").size
                        if (tdCount == 14) {
                            tr.getElementsByTag("td")[0].remove()
                        } else if (tdCount < 13) {
                            tr.remove()
                        }
                    }
                } catch (e: Exception) {
                    pyfaErrorOutput(name, document)
                    log.info("$name:${ResultCode.RESOLVE_ERROR.msg}")
                }
                trs = document.getElementsByTag("TBODY")[1].getElementsByTag("tr")
                for (i in 2 until trs.size) {
                    val tds = trs[i].children()
                    if (tds[3].text().isBlank()) continue
                    try {
                        val cj = NCJ(
                            calcTerm(tds[12].text(), enterYear),
                            tds[2].text(),
                            resolveScore(tds[3].text()),
                            tds[6].text(),
                            tds[11].text(),
                            "",
                            "",
                            tds[4].text()
                        )
                        if (cjs.contains(cj)) continue
                        cjs.add(cj)
                    } catch (e: Exception) {
                        pyfaErrorOutput(name, document)
                        log.info("$name:${ResultCode.RESOLVE_ERROR.msg}")
                    }
                }
            } catch (e: Exception) {
                log.error("培养方案获取失败")
                e.printStackTrace()
            }
            cjs.sortBy { it.term }
            if (cjs.isEmpty())
                Response(ResultCode.NO_GRADE)
            else
                Response(ResultCode.GET_SUCCESS, CJ(statistics[4], statistics[6], statistics[8], cjs.reversed()))
        }
    } catch (e: Exception) {
        log.error("连接教务平台失败")
        e.printStackTrace()
        Response(ResultCode.CONNECT_ERROR)
    }

    private fun pyfaErrorOutput(name: String, document: Document) {
        val file = File("errorPages/$name-pyfa.html")
        if (file.exists())
            file.delete()
        val fileWriter = FileWriter(file)
        fileWriter.write(document.toString())
        fileWriter.close()
    }

    override fun getLessons(cookieString: String): Response = try {
        val lTerm = if (cookieString.contains("康广慧")) {
            "2020-2021-1"
        } else UserImpl.TERM
        val documentStr = Jsoup.connect(lessonUrl)
            .cookies(string2Map(cookieString).apply { remove("name") })
            .headers(Util.constructHeader)
            .data("jx0404id", "")
            .data("cj0701id", "")
            .data("xnxq01id", lTerm)
            .userAgent("Mozilla/5.0( X11;Ubuntu;Linux x86_64 ;rv:61.0) Gecko20100101Firefox/61.0")
            .followRedirects(true)
            .post()
        val document = Jsoup.parse(documentStr.toString().replace("<br/>", "").replace("&nbsp;", ""))
        if (document.getElementsByTag("title")[0].text() == "登录")
            Response(ResultCode.NEED_LOGIN)
        else {
            val weekdays = arrayOf("一", "二", "三", "四", "五", "六", "日")
            val formattedLessons = ArrayList<FormattedLesson>()
            try {
                val trs = document.getElementsByTag("table")[0].getElementsByTag("tr")
                for (tr in trs) {
                    if (tr.children().size < 6)
                        break
                    val tds = tr.getElementsByTag("td")
                    for (i in tds.indices) {
                        val td = tds[i]
                        val div = td.children()[3]
                        for (node in div.textNodes()) {
                            if (node.isBlank) {
                                node.remove()
                            }
                        }
                        for (ch in div.children()) {
                            if (ch.text().isBlank() || ch.tag().name == "span") {
                                ch.remove()
                            }
                        }
                        if (div.text().isNotBlank())
                            div.appendChild(Element(Tag.valueOf("font"), "", Attributes().put("title", "块结束")))
                        val textIterator = div.textNodes().iterator()
                        val childIterator = div.children().iterator()
                        var name = ""
                        var teacher = ""
                        var weeks: IntArray? = null
                        var start = 0
                        var end = 0
                        var place = ""
                        nodes@ for (j in div.childNodes().indices) {
                            val c = div.childNode(j)
                            when (c.nodeName()) {
                                "#text" -> {
                                    if (j + 1 < div.childNodes().size && div.childNode(j + 1).nodeName() == "#text") {
                                        textIterator.next()
                                        continue@nodes
                                    }
                                    if (weeks != null) {
                                        for (week in weeks) {
                                            formattedLessons.add(
                                                FormattedLesson(
                                                    lTerm,
                                                    week.toString(),
                                                    name,
                                                    "星期${weekdays[i]}",
                                                    start,
                                                    end,
                                                    teacher,
                                                    place,
                                                    "",
                                                    ""
                                                )
                                            )
                                        }
                                        weeks = null
                                    }
                                    name = textIterator.next().text().trim().split(" ")[0]
                                }
                                "font" -> {
                                    val node = childIterator.next()
                                    when (node.attr("title")) {
                                        "老师" -> teacher = node.text()
                                        "周次(节次)" -> {
                                            val wap = node.text().replace("(周)", " ")
                                                .replace("(单周)", " ")
                                                .replace("(双周)", " ")
                                                .replace("[", "")
                                                .replace("节]", "").split(" ")
                                            weeks = when {
                                                wap[0].contains(",") -> {
                                                    val strs = wap[0].split(",")
                                                    val list = ArrayList<Int>()
                                                    for (s in strs) {
                                                        if (s.contains("-")) {
                                                            val se = s.split("-")
                                                            val range = IntRange(se[0].toInt(), se[1].toInt())
                                                            list.addAll(range.toList())
                                                        } else {
                                                            list.add(s.toInt())
                                                        }
                                                    }
                                                    list.stream().mapToInt {
                                                        it.toInt()
                                                    }.toArray()
                                                }
                                                wap[0].contains("-") && !wap[0].contains(",") -> {
                                                    val se = wap[0].split("-")
                                                    val range = IntRange(se[0].toInt(), se[1].toInt())
                                                    range.toList().toIntArray()
                                                }
                                                else -> {
                                                    intArrayOf(wap[0].toInt())
                                                }
                                            }
                                            start = wap[1].substring(0, 2).toInt()
                                            end = wap[1].substring(wap[1].lastIndex - 1, wap[1].length).toInt()
                                        }
                                        "教室" -> place = node.text()
                                        "块结束" -> {
                                            for (week in weeks!!) {
                                                formattedLessons.add(
                                                    FormattedLesson(
                                                        lTerm,
                                                        week.toString(),
                                                        name,
                                                        "星期${weekdays[i]}",
                                                        start,
                                                        end,
                                                        teacher,
                                                        place,
                                                        "",
                                                        ""
                                                    )
                                                )
                                            }
                                            weeks = null
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                val info = Jsoup.connect(infoUrl)
                    .cookies(string2Map(cookieString))
                    .headers(Util.constructHeader)
                    .userAgent("Mozilla/5.0( X11;Ubuntu;Linux x86_64 ;rv:61.0) Gecko20100101Firefox/61.0")
                    .followRedirects(true)
                    .get()
                val elements = info.getElementsByClass("middletopttxlr")[0].getElementsByClass("middletopdwxxcont")
                val name = elements[1].text()
                val file = File("errorPages/$name-lesson.html")
                if (file.exists())
                    file.delete()
                val fileWriter = FileWriter(file)
                fileWriter.write(document.toString())
                fileWriter.close()
                log.info("$name:${ResultCode.RESOLVE_ERROR.msg}")
                Response(ResultCode.RESOLVE_ERROR)
            }
            val lessons = ArrayList<Lesson>()
            out@ for (formatted in formattedLessons) {
                val week = formatted.week.replace("第", "").replace("周", "").toInt()
                for (lesson in lessons) {
                    if (formatted.name == lesson.name && formatted.teacher == lesson.teacher) {
                        for (part in lesson.parts) {
                            if (formatted.start == part.start
                                && formatted.end == part.end
                                && formatted.weekDay == part.weekday
                                && formatted.place == part.place
                                && part.weeks.indexOf(week) == -1
                            ) {
                                part.weeks.add(week)
                                continue@out
                            }
                        }
                        for (part in lesson.parts) {
                            if (formatted.start == part.start
                                && formatted.end == part.end
                                && formatted.weekDay == part.weekday
                                && formatted.place == part.place
                                && part.weeks.indexOf(week) != -1
                            ) {
                                continue@out
                            }
                        }
                        lesson.parts.add(
                            Part(
                                formatted.weekDay,
                                formatted.start,
                                formatted.end!!,
                                formatted.place,
                                arrayListOf(week)
                            )
                        )
                        continue@out
                    }
                }
                lessons.add(
                    Lesson(
                        formatted.name,
                        formatted.teacher,
                        null,
                        arrayListOf(
                            Part(
                                formatted.weekDay,
                                formatted.start,
                                formatted.end!!,
                                formatted.place,
                                arrayListOf(week)
                            )
                        )
                    )
                )
            }
            val mBgMap: MutableMap<String, String> = HashMap()
            val colors = java.util.ArrayList<String>()
            for (lesson in lessons) {
                if (mBgMap[lesson.name + lesson.teacher] != null)
                    lesson.bg = mBgMap[lesson.name + lesson.teacher]!!
                else {
                    val randomColor = Util.createRandomColor(colors)
                    mBgMap[lesson.name + lesson.teacher] = randomColor
                    lesson.bg = randomColor
                }
            }
            if (lessons.size != 0) {
                Response(ResultCode.GET_SUCCESS, hashMapOf("weekTotal" to 18, "lessons" to lessons))
            } else {
                Response(ResultCode.NO_LESSON)
            }
        }
    } catch (e: Exception) {
        e.printStackTrace()
        Response(ResultCode.GET_FAILED)
    }


    fun constructConnection(cookies: Map<String, String>, url: String): Connection {
        val connection = Jsoup.connect(url)
        connection.cookies(cookies)
        connection.headers(Util.constructHeader)
        connection.userAgent("Mozilla/5.0( X11;Ubuntu;Linux x86_64 ;rv:61.0) Gecko20100101Firefox/61.0")
        connection.followRedirects(true)
        connection.ignoreContentType(true)
        return connection
    }

    fun string2Map(string: String): MutableMap<String, String> {
        val json = JSONObject(string)
        val keys = json.keys()
        val map: MutableMap<String, String> = HashMap()
        for (key in keys) {
            map[key] = json.getString(key)
        }
        return map
    }

    fun map2Json(map: Map<String, String>): JSONObject {
        val keys = map.keys
        val jsonObject = JSONObject()
        for (key in keys) {
            jsonObject.put(key, map[key])
        }
        return jsonObject
    }

    fun resolveScore(text: String): String =
        when {
            text == "已修不及格" -> "59.9"
            text.contains("已修不及格") ->
                text.replace("已修不及格(", "").replace(")", "")
            else ->
                text.replace("已修(", "").replace(")", "")
        }

    fun calcTerm(no: String, enterYear: String): String {
        val enterYearInt = enterYear.toInt()
        val noInt = if (no.contains(",")) no.split(",")[0].toInt() else no.toInt()
        val yearOffset = (noInt - 1) / 2
        val termOffset = (noInt + 1) % 2 + 1
        return "${enterYearInt + yearOffset}-${enterYearInt + yearOffset + 1}-$termOffset"
    }

}