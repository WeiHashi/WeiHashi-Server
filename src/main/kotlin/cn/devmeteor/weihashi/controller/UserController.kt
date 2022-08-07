package cn.devmeteor.weihashi.controller

import cn.devmeteor.weihashi.data.Response
import cn.devmeteor.weihashi.getEnv
import cn.devmeteor.weihashi.service.UserService
import org.jsoup.Connection
import org.jsoup.Jsoup
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.*

@RestController
class UserController {

    @Autowired
    lateinit var userService: UserService

    private val appid = getEnv("QP_APP_ID")
    private val appSecret = getEnv("QP_APP_SECRET")

    @PostMapping("/updateAccount")
    fun updateAccount(
        openid: String,
        nickname: String,
        head: String,
        @RequestParam(defaultValue = "1") platform: Int,
        @RequestParam(defaultValue = "1.0.0") version: String
    ): Response =
        userService.updateAccount(openid, nickname, head, platform, version)

    @GetMapping("/getEmailCode")
    fun getEmailCode(openid: String, email: String): Response =
        userService.getEmailCode(openid, email)

    @PostMapping("/bindEmail")
    fun bindEmail(openid: String, email: String, code: String) =
        userService.bindEmail(openid, email, code)

    @PostMapping("/unbindMail")
    fun unbindMail(openid: String) = userService.unbindMail(openid)

    @GetMapping("/getOpenid", produces = ["application/json;charset=UTF-8"])
    fun getOpenid(code: String): String {
        val url =
            "https://api.q.qq.com/sns/jscode2session?appid=$appid&js_code=$code&secret=$appSecret&grant_type=authorization_code"
        val res = Jsoup.connect(url).ignoreContentType(true).method(Connection.Method.GET).execute()
        return res.body()
    }

    @PutMapping("/clearInfo")
    fun clearInfo(openid: String) =
        userService.clearInfo(openid)

}