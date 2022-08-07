package cn.devmeteor.weihashi.data


data class Response(var code: Int, var msg: String, var data: Any?) {
    constructor(resultCode: ResultCode) : this(resultCode, null)
    constructor(resultCode: ResultCode, data: Any?) : this(resultCode.code, resultCode.msg, data)
}