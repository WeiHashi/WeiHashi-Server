package cn.devmeteor.weihashi.service.impl

import cn.devmeteor.weihashi.dao.BannerDao
import cn.devmeteor.weihashi.dao.NoticeDao
import cn.devmeteor.weihashi.dao.QuestionnaireDao
import cn.devmeteor.weihashi.dao.TaskDao
import cn.devmeteor.weihashi.data.Response
import cn.devmeteor.weihashi.data.ResultCode
import cn.devmeteor.weihashi.data.Task
import cn.devmeteor.weihashi.getEnv
import cn.devmeteor.weihashi.service.MiscService
import cn.devmeteor.weihashi.util.Util
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.mail.javamail.JavaMailSenderImpl
import org.springframework.mail.javamail.MimeMessageHelper
import org.springframework.stereotype.Service
import java.util.*

@Service
class MiscImpl : MiscService {

    private val mailUSer = getEnv("MAIL_USER")
    private val mailPass = getEnv("MAIL_PASSWORD")

    @Autowired
    lateinit var bannerDao: BannerDao

    @Autowired
    lateinit var taskDao: TaskDao

    @Autowired
    lateinit var noticeDao: NoticeDao

    @Autowired
    lateinit var questionnaireDao: QuestionnaireDao

    override fun getBanner(): Response = Response(ResultCode.GET_SUCCESS, bannerDao.getBanner())
    override fun addTask(task: Task) {
        task.obj_id = Util.createObjId()
        taskDao.addTask(task)
    }

    override fun getBannerContent(type: Int, objId: String): Response {
        val content: Any? = when (type) {
            1 -> noticeDao.getNoticeById(objId)
            2 -> questionnaireDao.getQuestionnaire(objId)
            else -> null
        }
        return if (content == null)
            Response(ResultCode.CONTENT_NOT_EXIST)
        else
            Response(ResultCode.GET_SUCCESS, content)
    }

    override fun sendMail(addr: String, subject: String, msg: String): Boolean {
        return try {
            println("begin")
            val properties = Properties()
            properties.setProperty("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory")
            val mailSender = JavaMailSenderImpl()
            mailSender.defaultEncoding = "utf-8"
            mailSender.host = "smtp.qq.com"
            mailSender.javaMailProperties = properties
            mailSender.username = mailUSer
            mailSender.password = mailPass
            mailSender.port = 465
            val mimeMessage = mailSender.createMimeMessage()
            val messageHelper = MimeMessageHelper(mimeMessage, true)
            messageHelper.setFrom("$mailUSer@qq.com", "no-reply")
            messageHelper.setTo(addr)
            messageHelper.setSubject(subject)
            messageHelper.setText(msg, true)
            mailSender.send(mimeMessage)
            println("success")
            true
        } catch (e: Exception) {
            e.printStackTrace()
            println("fail")
            false
        }
    }
}