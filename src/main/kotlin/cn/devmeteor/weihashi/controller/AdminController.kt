package cn.devmeteor.weihashi.controller

import cn.devmeteor.weihashi.dao.*
import cn.devmeteor.weihashi.data.*
import cn.devmeteor.weihashi.service.AdminService
import cn.devmeteor.weihashi.util.Util
import org.json.JSONObject
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.*

@CrossOrigin
@RestController
class AdminController {

    @Autowired
    lateinit var adminService: AdminService

    @Autowired
    lateinit var classDao: ClassDao

    @Autowired
    lateinit var instituteDao: InstituteDao

    @Autowired
    lateinit var classMemberDao: ClassMemberDao

    @Autowired
    lateinit var noticeDao: NoticeDao

    @Autowired
    lateinit var questionnaireDao: QuestionnaireDao

    @Autowired
    lateinit var questionnaireCollectionDao: QuestionnaireCollectionDao

    @Autowired
    lateinit var userDao: UserDao

    @GetMapping("/adminLogin")
    fun login(username: String, password: String): Response {
        val classVerify = classDao.verifyPassword(username, password)
        val token = Util.getSaltedMd5(username + password + System.currentTimeMillis())
        if (classVerify) {
            classDao.updateTokenByUsername(token, username)
            return Response(ResultCode.LOGIN_OK, hashMapOf("token" to token, "type" to 0))
        }
        return Response(ResultCode.LOGIN_FAIL)
    }

    @GetMapping("adminGetInfo")
    fun getAccountInfo(username: String, type: Int): Response =
            when (type) {
                0 -> {
                    val info = classDao.getClassInfoByUsername(username)
                    val instituteName = instituteDao.getInstituteNameById(info.institute_id)
                    Response(ResultCode.GET_SUCCESS, hashMapOf("name" to info.name, "institute" to instituteName, "id" to info.class_id))
                }
                else -> Response(ResultCode.INVALID_PARAM)
            }

    @PutMapping("/adminUpdatePassword")
    fun updatePassword(username: String, origin: String, new: String, type: Int): Response {
        when (type) {
            0 -> {
                val classVerify = classDao.verifyPassword(username, origin)
                if (!classVerify)
                    return Response(ResultCode.ORIGIN_ERROR)
                classDao.updatePasswordByUsername(username, new)
                return Response(ResultCode.UPDATE_SUCCESS)
            }
            else -> return Response(ResultCode.INVALID_PARAM)
        }
    }

    @PutMapping("/adminLogout")
    fun logout(username: String, type: Int) =
            when (type) {
                0 -> classDao.updateTokenByUsername("", username)
                else -> null
            }

    @GetMapping("adminGetOverview")
    fun getOverview(class_id: String): Response {
        val studentCount = classMemberDao.getClassMemberCount(class_id)
        val noticeCount = noticeDao.getNoticeCountBySender("""{"id_type":1,"id":"$class_id"}""", class_id)
        val questionnaireCount = questionnaireDao.getQuestionnaireCountBySender("""{"id_type":1,"id":"$class_id"}""", class_id)
        return Response(ResultCode.GET_SUCCESS, hashMapOf("studentCount" to studentCount, "noticeCount" to noticeCount, "questionnaireCount" to questionnaireCount))
    }

    @PostMapping("/adminAddNotice")
    fun addNotice(@RequestBody notice: Notice, top: Boolean, id: String, type: Int): Response {
        val noticeTopCount = noticeDao.getTopCount("""{"id_type":1,"id":"$id"}""")
        val questionnaireTopCount = questionnaireDao.getTopCount("""{"id_type":1,"id":"$id"}""")
        notice.obj_id = Util.createObjId()
        notice.type = 0
        notice.top_level = if (top && noticeTopCount + questionnaireTopCount < 3) TopLevel.CLASS_TOP.code else TopLevel.NOT_TOP.code
        notice.sender = """{"id_type":1,"id":"$id"}"""
        notice.receiver = """{"institute":[],"classes":["$id"],"students":[]}"""
        noticeDao.addNotice(notice)
        return if (!top) return Response(ResultCode.OPERATION_SUCCESS) else if (top && noticeTopCount + questionnaireTopCount < 3) Response(ResultCode.OPERATION_SUCCESS) else Response(ResultCode.TOP_CANCELED)
    }

    @GetMapping("/adminGetNoticeList")
    fun getNoticeList(id: String, type: Int, page: Int, limit: Int): Response {
        var sender: String? = null
        val list = when (type) {
            0 -> {
                sender = classDao.getClassNameById(id)
                noticeDao.getNoticeListBySender("""{"id_type":1,"id":"$id"}""", id, (page - 1) * limit, limit)
            }
            else -> ArrayList()
        }
        for (notice in list) {
            if (JSONObject(notice.sender).getInt("id_type") == 2)
                notice.sender = JSONObject(notice.sender).getString("id")
            else
                notice.sender = sender
        }
        return Response(ResultCode.GET_SUCCESS, hashMapOf("total" to noticeDao.getNoticeCountBySender("""{"id_type":1,"id":"$id"}""", id), "list" to list))
    }

    @DeleteMapping("/adminDeleteNotice/{objId}")
    fun deleteNotice(@PathVariable objId: String) = noticeDao.deleteNoticeByObjId(objId)

    @PutMapping("/adminUpdateNotice")
    fun updateNotice(obj_id: String, field: String, value: String) = noticeDao.updateNoticeById(obj_id, field, value)

    @PutMapping("/adminSetNoticeTop")
    fun setNoticeTop(obj_id: String, top: Boolean, id: String): Response {
        val noticeTopCount = noticeDao.getTopCount("""{"id_type":1,"id":"$id"}""")
        val questionnaireTopCount = questionnaireDao.getTopCount("""{"id_type":1,"id":"$id"}""")
        if (top && noticeTopCount + questionnaireTopCount >= 3)
            return Response(ResultCode.TOP_EXCEED_CEILING)
        noticeDao.setTop(obj_id, if (top) TopLevel.CLASS_TOP.code else TopLevel.NOT_TOP.code)
        return Response(ResultCode.OPERATION_SUCCESS)
    }

    @PostMapping("/adminAddQuestionnaire")
    fun addQuestionnaire(@RequestBody questionnaire: Questionnaire, top: Boolean, id: String, type: Int): Response {
        val noticeTopCount = noticeDao.getTopCount("""{"id_type":1,"id":"$id"}""")
        val questionnaireTopCount = questionnaireDao.getTopCount("""{"id_type":1,"id":"$id"}""")
        questionnaire.obj_id = Util.createObjId()
        questionnaire.type = 0
        questionnaire.top_level = if (top && noticeTopCount + questionnaireTopCount < 3) TopLevel.CLASS_TOP.code else TopLevel.NOT_TOP.code
        when (type) {
            0 -> {
                questionnaire.sender = """{"id_type":1,"id":"$id"}"""
                questionnaire.receiver = """{"institute":[],"classes":["$id"],"students":[]}"""
            }
        }
        questionnaireDao.addQuestionnaire(questionnaire)
        return if (!top) return Response(ResultCode.OPERATION_SUCCESS) else if (top && noticeTopCount + questionnaireTopCount < 3) Response(ResultCode.OPERATION_SUCCESS) else Response(ResultCode.TOP_CANCELED)
    }

    @GetMapping("/adminGetQuestionnaireList")
    fun getQuestionnaireList(id: String, type: Int, page: Int, limit: Int): Response {
        var sender: String? = null
        val list = when (type) {
            0 -> {
                sender = classDao.getClassNameById(id)
                questionnaireDao.getQuestionnaireListBySender("""{"id_type":1,"id":"$id"}""", id, (page - 1) * limit, limit)
            }
            else -> ArrayList()
        }
        for (questionnaire in list) {
            if (JSONObject(questionnaire.sender).getInt("id_type") == 2)
                questionnaire.sender = JSONObject(questionnaire.sender).getString("id")
            else
                questionnaire.sender = sender
        }
        return Response(ResultCode.GET_SUCCESS, hashMapOf("total" to questionnaireDao.getQuestionnaireCountBySender("""{"id_type":1,"id":"$id"}""", id), "list" to list))
    }

    @DeleteMapping("/adminDeleteQuestionnaire/{objId}")
    fun deleteQuestionnaire(@PathVariable objId: String) {
        questionnaireCollectionDao.deleteQuestionnaireCollectionByQid(objId)
        questionnaireDao.deleteQuestionnaireByObjId(objId)
    }


    @PutMapping("/adminUpdateQuestionnaire")
    fun updateQuestionnaire(obj_id: String, field: String, value: String) = questionnaireDao.updateQuestionnaireById(obj_id, field, value)

    @PutMapping("/adminSetQuestionnaireTop")
    fun setQuestionnaireTop(obj_id: String, top: Boolean, id: String): Response {
        val noticeTopCount = noticeDao.getTopCount("""{"id_type":1,"id":"$id"}""")
        val questionnaireTopCount = questionnaireDao.getTopCount("""{"id_type":1,"id":"$id"}""")
        println(noticeTopCount)
        println(questionnaireTopCount)
        if (top && noticeTopCount + questionnaireTopCount >= 3)
            return Response(ResultCode.TOP_EXCEED_CEILING)
        questionnaireDao.setTop(obj_id, if (top) TopLevel.CLASS_TOP.code else TopLevel.NOT_TOP.code)
        return Response(ResultCode.OPERATION_SUCCESS)
    }

    @GetMapping("/adminGetMemberList")
    fun getMemberList(class_id: String, page: Int, limit: Int): Response {
        val list = classMemberDao.getClassMemberById(class_id, (page - 1) * limit, limit)
        for (map in list) {
            println(map)
            val info = userDao.getStudentInfoById(map["openid"]!!)
            map["name"] = if (info!!.getValue("name") == "")
                info.getValue("nickname")
            else
                info.getValue("name")
            map["studentid"] = info.getValue("studentid")
            map["openid"] = Util.getMd5(Util.getMd5(map["openid"]!!))
        }
        return Response(ResultCode.GET_SUCCESS, hashMapOf("list" to list, "total" to classMemberDao.getClassMemberCount(class_id)))
    }

    @PutMapping("/adminSetCadre")
    fun setCadre(class_id: String, openid: String, is_cadre: Boolean): Response {
        val cadreCount = classMemberDao.getCadreCount(class_id)
        if (is_cadre && cadreCount >= 8)
            return Response(ResultCode.CADRE_EXCEED_CEILING)
        classMemberDao.setCadre(openid, is_cadre)
        return Response(ResultCode.OPERATION_SUCCESS)
    }

    @DeleteMapping("/adminDeleteMember/{id}")
    fun deleteMember(@PathVariable id: String) = classMemberDao.deleteClassMemberById(id)

    @GetMapping("/adminGetMemberInfoBeforeAdd")
    fun getMemberInfoBeforeAdd(openid: String, class_id: String): Response {
        val classId = classMemberDao.getClassIdById(openid)
        if (classId == null) {
            val map = userDao.getStudentInfoByHashedId(openid)
            return if (map != null) Response(ResultCode.GET_SUCCESS, map) else Response(ResultCode.USER_NOT_EXISTS)
        }
        if (classId == class_id)
            return Response(ResultCode.MEMBER_EXIST)
        return Response(ResultCode.MEMBER_IN_OTHER_CLASS)
    }

    @PostMapping("/adminConfirmAddMember")
    fun confirmAddMember(openid: String, class_id: String) {
        val realOpenid = userDao.getIdByHashedId(openid)
        classMemberDao.addMember(realOpenid, class_id)
    }

    @GetMapping("/adminGetQuestionnaireCollection")
    fun getQuestionnaireCollection(qid: String, page: Int, limit: Int): Response {
        val list = questionnaireCollectionDao.getQuestionnaireCollectionByQid(qid, (page - 1) * limit, limit)
        for (map in list) {
            val info = userDao.getStudentInfoById(map["openid"] as String)
            map["name"] = if (info!!.getValue("name") == "") info.getValue("nickname") else info.getValue("name")
        }
        val total = questionnaireCollectionDao.countQuestionnaireCollectionByQid(qid)
        return Response(ResultCode.GET_SUCCESS, hashMapOf("total" to total, "list" to list))
    }

    @DeleteMapping("/adminDeleteQuestionnaireCollection/{id}")
    fun deleteQuestionnaireCollection(@PathVariable id: String) =
            questionnaireCollectionDao.deleteQuestionnaireCollectionByObjId(id)

}