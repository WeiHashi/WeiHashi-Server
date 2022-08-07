package cn.devmeteor.weihashi.interceptor

import cn.devmeteor.weihashi.data.Response
import cn.devmeteor.weihashi.data.ResultCode
import cn.devmeteor.weihashi.util.RedisUtil
import cn.devmeteor.weihashi.util.Util
import org.springframework.web.servlet.HandlerInterceptor
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse


class SuInterceptor(private val redisUtil: RedisUtil) : HandlerInterceptor {

    override fun preHandle(request: HttpServletRequest, response: HttpServletResponse, handler: Any): Boolean {
        val deviceId = request.getHeader("Device-ID")
        if (deviceId == null) {
            sendResponse(response, Response(ResultCode.INVALID_REQUEST), true)
            return false
        }
        if (!redisUtil.get("Device-ID-$deviceId", false)) {
            sendResponse(response, Response(ResultCode.INVALID_REQUEST))
            return false
        }
        return true
    }

    private fun sendResponse(response: HttpServletResponse, myResponse: Response, createDeviceId: Boolean = false) {
        response.characterEncoding = "UTF-8"
        response.contentType = "application/json"
        val writer = response.writer
        writer.print(
            Util.constructRes(
                myResponse.code,
                myResponse.msg,
                if (createDeviceId) Util.createUUID() else null
            )
        )
        writer?.close()
    }

}