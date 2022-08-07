package cn.devmeteor.weihashi.controller

import cn.devmeteor.weihashi.data.Response
import cn.devmeteor.weihashi.service.ClassService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/v2/class")
class ClassControllerV2 {
    @Autowired
    lateinit var classService: ClassService

    @GetMapping("/messages")
    fun getMessages(classId: String, @RequestParam(defaultValue = "1") page: Int): Response =
        classService.getMessagesV2(classId,page)
}