package cn.devmeteor.weihashi.service

import cn.devmeteor.weihashi.data.PoiReportForm
import cn.devmeteor.weihashi.data.TestApplication

interface AppWebService {
    fun addApplication(testApplication: TestApplication)
    fun checkSubmit(openid: String): Boolean
    fun reportPoi(poiReportForm: PoiReportForm)
    fun freqCheck(openid: String): Boolean
}