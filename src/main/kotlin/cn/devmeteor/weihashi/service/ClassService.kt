package cn.devmeteor.weihashi.service

import cn.devmeteor.weihashi.data.Notice
import cn.devmeteor.weihashi.data.Questionnaire
import cn.devmeteor.weihashi.data.QuestionnaireCollection
import cn.devmeteor.weihashi.data.Response

interface ClassService {
    fun joinClass(openid:String,classId:String):Response
    fun getMessages(classId: String):Response
    fun getMoreMessages(classId: String,page:Int):Response
    fun getSystemMessages():Response
    fun addNotice(notice: Notice,classId: String,name:String)
    fun addQuestionnaire(questionnaire: Questionnaire,classId: String,name: String)
    fun submitQuestionnaire(questionnaireCollection: QuestionnaireCollection)
    fun getSavedCollection(openid: String,qid:String):Response
    fun coverCollection(objId:String,content:String)
    fun getMessagesV2(classId: String, page: Int): Response
}