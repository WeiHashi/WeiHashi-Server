package cn.devmeteor.weihashi.data

import java.sql.Timestamp

data class TestApplication(
    var openid: String?,
    var name: String?,
    var studentid: String?,
    var email: String?,
    var timestamp: Timestamp? = null,
    var version: String = "1.0.0",
    var enabled: Boolean = false
)
