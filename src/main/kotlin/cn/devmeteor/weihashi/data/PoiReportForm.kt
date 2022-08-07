package cn.devmeteor.weihashi.data

import java.sql.Timestamp

data class PoiReportForm(
    var obj_id: String?,
    var openid: String,
    var name: String,
    var location: String,
    var timestamp: Timestamp? = null
)
