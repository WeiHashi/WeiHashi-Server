package cn.devmeteor.weihashi.data

data class Task(
    var obj_id: String?,
    var openid: String,
    var formid: String?,
    var detail: String,
    var timestamp: Long,
    var push: Boolean,
    var mail: Boolean
)