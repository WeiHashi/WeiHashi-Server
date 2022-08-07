package cn.devmeteor.weihashi.controller

import cn.devmeteor.weihashi.data.PoiReportForm
import cn.devmeteor.weihashi.data.TestApplication
import cn.devmeteor.weihashi.service.AppWebService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.ModelAttribute
import org.springframework.web.bind.annotation.PostMapping

@Controller
class AppWebController {

    @Autowired
    lateinit var appWebService: AppWebService

    @GetMapping("/testApply")
    fun requestTestApply(openid: String): String =
        if (appWebService.checkSubmit(openid)) {
            "m/finishSubmit"
        } else {
            "m/testApply"
        }

    @PostMapping("/testApply")
    fun submitTestApply(@ModelAttribute testApplication: TestApplication): String = try {
        appWebService.addApplication(testApplication)
        "m/finishSubmit"
    } catch (e: Exception) {
        e.printStackTrace()
        "m/submitError"
    }

    @GetMapping("/poiReport")
    fun requestPoiReport(): String = "m/poiReport"

    @PostMapping("/poiReport")
    fun poiReport(@ModelAttribute poiReportForm: PoiReportForm) = try {
        if (appWebService.freqCheck(poiReportForm.openid)) {
            "m/freqExceed"
        } else {
            appWebService.reportPoi(poiReportForm)
            "m/finishSubmit"
        }
    } catch (e: Exception) {
        e.printStackTrace()
        "m/submitError"
    }

}