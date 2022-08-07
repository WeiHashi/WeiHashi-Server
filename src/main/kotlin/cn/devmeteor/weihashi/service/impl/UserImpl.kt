package cn.devmeteor.weihashi.service.impl

import cn.devmeteor.weihashi.dao.CodeDao
import cn.devmeteor.weihashi.dao.TestApplicationDao
import cn.devmeteor.weihashi.dao.UserDao
import cn.devmeteor.weihashi.data.Response
import cn.devmeteor.weihashi.data.ResultCode
import cn.devmeteor.weihashi.service.MiscService
import cn.devmeteor.weihashi.service.UserService
import cn.devmeteor.weihashi.util.RedisUtil
import cn.devmeteor.weihashi.util.Util
import com.vdurmont.emoji.EmojiParser
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

@Service
class UserImpl : UserService {

    companion object {
        const val TERM = "2021-2022-2"
        private const val TERM_START = "2022-02-28 00:00:00"
        const val KEY_UV = "uv"
        const val KEY_DU = "du"
    }

    @Autowired
    lateinit var userDao: UserDao

    @Autowired
    lateinit var codeDao: CodeDao

    @Autowired
    lateinit var testApplicationDao: TestApplicationDao

    @Autowired
    lateinit var miscService: MiscService

    @Autowired
    lateinit var redisUtil: RedisUtil

    override fun updateAccount(
        openid: String,
        nickname: String,
        head: String,
        platform: Int,
        version: String
    ): Response {
        redisUtil.pfAdd(KEY_UV, openid)
        redisUtil.incr(KEY_DU)
        val isExists = userExists(openid)
        val testFlag = testApplicationDao.getTestFlag(openid, version)
        val userInfo = if (isExists) {
            userDao.getUserInfo(openid)
        } else {
            hashMapOf("email" to "")
        }
        val info = userDao.getInfoById(openid)
            ?: hashMapOf(
                "email" to userInfo["email"],
                "class_id" to "",
                "is_cadre" to false,
                "className" to "",
                "instituteName" to "",
                "test_permission" to testFlag
            )
        if (isExists) {
            try {
                userDao.updateUseInfo(openid, EmojiParser.parseToAliases(nickname), head)
            } catch (e: Exception) {
                println("$openid：$nickname----$e")
                userDao.updateUseInfo(openid, "<存储错误>", head)
            }
        } else {
            try {
                userDao.createUser(openid, EmojiParser.parseToAliases(nickname), head, platform)
            } catch (e: Exception) {
                println("$openid：$nickname----$e")
                userDao.createUser(openid, "<存储错误>", head, platform)
            }
        }
        val res = hashMapOf(
            "email" to info["email"],
            "class_id" to info["class_id"],
            "is_cadre" to info["is_cadre"],
            "className" to info["className"],
            "instituteName" to info["instituteName"],
            "testPermission" to info["test_permission"],
            "term" to TERM,
            "termStart" to TERM_START
        )
        return Response(ResultCode.OPERATION_SUCCESS, res)
    }

    override fun bindEmail(openid: String, email: String, code: String): Response {
        if (codeDao.verifyCode(openid, code)) {
            userDao.bindMail(openid, email)
            codeDao.deleteCodeById(openid)
            return Response(ResultCode.OPERATION_SUCCESS)
        }
        return Response(ResultCode.VERIFY_ERROR)
    }

    override fun unbindMail(openid: String) = userDao.unbindMail(openid)
    override fun clearInfo(openid: String) {}


    override fun getEmailCode(openid: String, addr: String): Response {
        if (!userExists(openid))
            return Response(ResultCode.GET_CODE_FAILED)
        val code = Util.createCode()
        val success = miscService.sendMail(
            addr,
            "微哈师-绑定邮箱",
            """<div style="display: flex;flex-direction: column;align-items: center;width: 100%;"><img style="width: 100px;height:100px;" src="https://devmeteor.cn/whs/img/whs.png"><text style="font-size: 30px;margin-top: 20px;">微哈师-绑定邮箱</text><div style="padding: 20px;max-width: 80%;font-size: 20px;"><p style="text-indent: 2em;">您绑定邮箱的验证码为：<font color="blue">${code}</font>，5分钟内有效，请不要将此验证码提供给他人。本邮件由系统自动发送，请勿回复。</p><div style="height: 1px;background-color:#f2f2f2;width: 100%;margin-top: 20px"></div><p style="color: #888888;font-size: 16px;margin: 0px;padding: 0px;padding-top: 10px;">如果这不是您设置的提醒邮件请联系：2633979287@qq.com</p></div></div>"""
        )
        if (!success) return Response(ResultCode.GET_CODE_FAILED)
        if (codeDao.checkId(openid))
            codeDao.updateCode(openid, code, System.currentTimeMillis())
        else
            codeDao.saveCode(openid, code, System.currentTimeMillis())
        return Response(ResultCode.GET_SUCCESS)
    }

    fun userExists(openid: String): Boolean = userDao.checkUser(openid)

}