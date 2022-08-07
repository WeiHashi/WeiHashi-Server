package cn.devmeteor.weihashi.service.impl

import cn.devmeteor.weihashi.dao.*
import cn.devmeteor.weihashi.data.*
import cn.devmeteor.weihashi.service.ClassService
import cn.devmeteor.weihashi.util.Util
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

@Service
class ClassImpl : ClassService {

    @Autowired
    lateinit var classMemberDao: ClassMemberDao

    @Autowired
    lateinit var classDao: ClassDao

    @Autowired
    lateinit var noticeDao: NoticeDao

    @Autowired
    lateinit var questionnaireDao: QuestionnaireDao

    @Autowired
    lateinit var questionnaireCollectionDao: QuestionnaireCollectionDao

    override fun joinClass(openid: String, classId: String): Response {
        val className = classDao.getClassNameById(classId)
        return if (className != null) {
            classMemberDao.addMember(openid, classId)
            Response(ResultCode.OPERATION_SUCCESS, className)
        } else {
            Response(ResultCode.CLASS_NOT_EXISTS)
        }
    }

    override fun getMessages(classId: String): Response {
        val messages = noticeDao.getMessages(classId, 0)
        val total = noticeDao.getMessageTotal(classId)
        val tops = noticeDao.getTops(classId)
        return if (messages == null && tops == null)
            Response(ResultCode.NO_MESSAGE)
        else
            Response(
                ResultCode.GET_SUCCESS,
                hashMapOf("tops" to (tops ?: arrayListOf()), "total" to total, "normals" to (messages ?: arrayListOf()))
            )
    }

    override fun getMoreMessages(classId: String, page: Int): Response =
        Response(ResultCode.GET_SUCCESS, noticeDao.getMessages(classId, (page - 1) * 10))

    override fun getSystemMessages(): Response {
        val messages = noticeDao.getSystemMessages()
        val tops = noticeDao.getSystemTops()
        return if (messages == null && tops == null)
            Response(ResultCode.NO_MESSAGE)
        else
            Response(
                ResultCode.GET_SUCCESS,
                hashMapOf("tops" to (tops ?: arrayListOf()), "total" to 1, "normals" to (messages ?: arrayListOf()))
            )
    }

    override fun addNotice(notice: Notice, classId: String, name: String) {
        notice.obj_id = Util.createObjId()
        notice.type = 0
        notice.top_level = 0
        notice.sender = """{"id_type":2,"id":"$name"}"""
        notice.receiver = """{"institute":[],"classes":["$classId"],"students":[]}"""
        noticeDao.addNotice(notice)
    }

    override fun addQuestionnaire(questionnaire: Questionnaire, classId: String, name: String) {
        questionnaire.obj_id = Util.createObjId()
        questionnaire.type = 0
        questionnaire.top_level = 0
        questionnaire.sender = """{"id_type":2,"id":"$name"}"""
        questionnaire.receiver = """{"institute":[],"classes":["$classId"],"students":[]}"""
        questionnaireDao.addQuestionnaire(questionnaire)
    }

    override fun submitQuestionnaire(questionnaireCollection: QuestionnaireCollection) =
        questionnaireCollectionDao.submitQuestionnaire(questionnaireCollection)

    override fun getSavedCollection(openid: String, qid: String): Response {
        val questionnaireCollection = questionnaireCollectionDao.getSavedCollection(openid, qid)
        return if (questionnaireCollection == null)
            Response(ResultCode.NO_SAVED_COLLECTION)
        else
            Response(ResultCode.GET_SUCCESS, questionnaireCollection)
    }

    override fun coverCollection(objId: String, content: String) =
        questionnaireCollectionDao.coverCollection(objId, content)

    override fun getMessagesV2(classId: String, page: Int): Response {
        val messages = noticeDao.getMessagesV2(classId, (page - 1) * 10)
        if (page != 1) {
            return Response(ResultCode.GET_SUCCESS, messages)
        }
        val total = noticeDao.getMessageTotal(classId)
        val tops = noticeDao.getTopsV2(classId)
        return if (messages == null && tops == null)
            Response(ResultCode.NO_MESSAGE)
        else
            Response(
                ResultCode.GET_SUCCESS,
                hashMapOf("tops" to (tops ?: arrayListOf()), "total" to total, "normals" to (messages ?: arrayListOf()))
            )
    }

}