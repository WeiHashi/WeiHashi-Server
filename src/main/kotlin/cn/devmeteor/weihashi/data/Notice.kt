package cn.devmeteor.weihashi.data

import java.sql.Timestamp

data class Notice(
    var obj_id: String?,
    var cate: Int?,
    var title: String,
    var content: String,
    var detail: String?,
    var sender: String?,
    var receiver: String?,
    var type: Int,
    var timestamp: Timestamp?,
    var top_level: Int?
)