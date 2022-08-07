package cn.devmeteor.weihashi.service

import cn.devmeteor.weihashi.data.Response

interface UserService {
    fun updateAccount(openid: String, nickname: String, head: String, platform: Int, version: String): Response
    fun getEmailCode(openid: String, addr: String): Response
    fun bindEmail(openid: String, email: String, code: String): Response
    fun unbindMail(openid: String)
    fun clearInfo(openid: String)
}