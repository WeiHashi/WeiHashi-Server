package cn.devmeteor.weihashi.controller

import cn.devmeteor.weihashi.data.Notice
import cn.devmeteor.weihashi.data.Questionnaire
import cn.devmeteor.weihashi.data.QuestionnaireCollection
import cn.devmeteor.weihashi.data.Response
import cn.devmeteor.weihashi.service.ClassService
import cn.devmeteor.weihashi.util.Util
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.*

@RestController
class ClassController {

    @Autowired
    lateinit var classService: ClassService

    @PostMapping("/joinClass")
    fun joinClass(openid: String, classId: String): Response =
            classService.joinClass(openid, classId)

    @GetMapping("/getMessages")
    fun getMessages(classId: String): Response =
            classService.getMessages(classId)

    @GetMapping("/getSystemMessages")
    fun getSystemMessages():Response=
            classService.getSystemMessages()

    @GetMapping("/getMoreMessages")
    fun getMoreMessages(classId: String,page:Int):Response=
            classService.getMoreMessages(classId,page)

    @PostMapping("/addNotice")
    fun addNotice(@RequestBody notice: Notice,classId: String,name:String)=
            classService.addNotice(notice,classId,name)

    @PostMapping("/addQuestionnaire")
    fun addQuestionnaire(@RequestBody questionnaire: Questionnaire,classId: String,name: String)=
            classService.addQuestionnaire(questionnaire,classId,name)

    @PostMapping("/submitQuestionnaire")
    fun submitQuestionnaire(@RequestBody questionnaireCollection: QuestionnaireCollection) {
        questionnaireCollection.obj_id= Util.createObjId()
        classService.submitQuestionnaire(questionnaireCollection)
    }

    @GetMapping("/getSavedCollection")
    fun getSavedCollection(openid: String,qid:String):Response=
            classService.getSavedCollection(openid,qid)

    @PutMapping("/coverCollection")
    fun coverCollection(objId:String, content:String)=
            classService.coverCollection(objId,content)
}