package cn.devmeteor.weihashi.interceptor

import cn.devmeteor.weihashi.dao.ClassDao
import cn.devmeteor.weihashi.data.Response
import cn.devmeteor.weihashi.data.ResultCode
import cn.devmeteor.weihashi.util.Util
import org.springframework.web.servlet.HandlerInterceptor
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

class AdminInterceptor(private val classDao: ClassDao) : HandlerInterceptor {
    override fun preHandle(request: HttpServletRequest, response: HttpServletResponse, handler: Any): Boolean {
        val username = request.getParameter("username")
        val token = request.getParameter("token")
        val valid = classDao.verifyToken(username, token)
        if (!valid){
            sendResponse(response, Response(ResultCode.TOKEN_INVALID))
            return false
        }
        return true
    }

    private fun sendResponse(response: HttpServletResponse, myResponse: Response) {
        response.characterEncoding = "UTF-8"
        response.contentType = "application/json"
        val writer = response.writer
        writer.print(Util.constructRes(myResponse.code, myResponse.msg))
        writer?.close()
    }
}