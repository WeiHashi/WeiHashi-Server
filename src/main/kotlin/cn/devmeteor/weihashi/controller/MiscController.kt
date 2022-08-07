package cn.devmeteor.weihashi.controller

import cn.devmeteor.weihashi.data.Response
import cn.devmeteor.weihashi.data.Task
import cn.devmeteor.weihashi.service.MiscService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.core.io.FileSystemResource
import org.springframework.core.io.Resource
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.io.File

@RestController
class MiscController {

    @Autowired
    lateinit var miscService: MiscService

    @GetMapping("/getBanner")
    fun getBanner(): Response = miscService.getBanner()

    @GetMapping("/getBannerContent")
    fun getNotice(type: Int, objId: String): Response =
        miscService.getBannerContent(type, objId)

    @PostMapping("/addTask")
    fun addTask(task: Task) = miscService.addTask(task)

    @PostMapping("/pushCallback")
    fun pushCallback(@RequestBody body: String): String {
        println("push callback:$body")
        return ""
    }

    @GetMapping("/cache/image/{filename}")
    fun getCachedImage(@PathVariable("filename") name: String): ResponseEntity<Resource> {
        val path = "imageCache/$name"
        if (!File(path).exists()) {
            return ResponseEntity.notFound().build()
        }
        return ResponseEntity.ok()
            .contentType(MediaType.IMAGE_PNG)
            .body(FileSystemResource(path))
    }
}