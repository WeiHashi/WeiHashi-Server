package cn.devmeteor.weihashi.data

import java.sql.Timestamp

data class User(
    var openid: String,
    var nickname: String,
    var head: String,
    var name: String,
    var studentid: String,
    var email: String,
    var platform: Int,
    var latest_use: Timestamp,
    var testPermission: Boolean
)