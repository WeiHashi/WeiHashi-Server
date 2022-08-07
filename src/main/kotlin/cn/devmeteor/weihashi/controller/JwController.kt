package cn.devmeteor.weihashi.controller

import cn.devmeteor.weihashi.data.CJ
import cn.devmeteor.weihashi.data.Response
import cn.devmeteor.weihashi.data.ResultCode
import cn.devmeteor.weihashi.getEnv
import cn.devmeteor.weihashi.service.JwService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
class JwController {

    @Autowired
    lateinit var jwService: JwService

    private val myUsername = getEnv("MY_JW_USERNAME")
    private val myPassword = getEnv("MY_JW_PASSWORD")

    @PostMapping("/login")
    fun login(openid: String, username: String, password: String): Response {
        val realUsername = if (username == "guest") myUsername else username
        val realPassword = if (username == "guest") myPassword else password
        return jwService.login(openid, realUsername, realPassword)
    }

    @GetMapping("/getDailyLessons")
    fun getDailyLesson(cookieString: String): Response =
        jwService.getLessons(cookieString)


    @Deprecated("已废弃", ReplaceWith("/grade"))
    @RequestMapping("/getGrade")
    fun getGrade(cookieString: String): Response =
        jwService.getGrade(cookieString).apply {
            if (code == ResultCode.GET_SUCCESS.code) {
                data = (data as CJ).list.reversed()
            }
        }

    @GetMapping("/grade")
    fun getGradeV2(cookieString: String): Response =
        jwService.getGrade(cookieString)

}