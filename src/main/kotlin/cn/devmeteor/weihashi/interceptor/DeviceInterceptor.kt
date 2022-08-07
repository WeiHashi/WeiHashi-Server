package cn.devmeteor.weihashi.interceptor

import cn.devmeteor.weihashi.util.RSAUtil
import org.springframework.web.servlet.HandlerInterceptor
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

class DeviceInterceptor : HandlerInterceptor {
    override fun preHandle(request: HttpServletRequest, response: HttpServletResponse, handler: Any): Boolean {
        val cipherText = request.getHeader("CipherText")
        if (cipherText == null) {
            response.sendError(403)
            return false
        }
        val decrypted = try {
            RSAUtil.decrypt(cipherText)
        } catch (e: Exception) {
            response.sendError(403)
            return false
        }
        val currentTime = System.currentTimeMillis()
        if (currentTime - decrypted > 10 * 1000) {
            response.sendError(403)
            return false
        }
        return true
    }
}