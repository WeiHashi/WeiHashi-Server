package cn.devmeteor.weihashi.service.impl

import cn.devmeteor.weihashi.dao.AppWebDao
import cn.devmeteor.weihashi.data.PoiReportForm
import cn.devmeteor.weihashi.data.TestApplication
import cn.devmeteor.weihashi.service.AppWebService
import cn.devmeteor.weihashi.util.Util
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

@Service
class AppWebImpl : AppWebService {

    @Autowired
    lateinit var appWebDao: AppWebDao

    override fun addApplication(testApplication: TestApplication) {
        appWebDao.addTestApplication(testApplication)
    }

    override fun checkSubmit(openid: String): Boolean =
        appWebDao.checkSubmit(openid)

    override fun reportPoi(poiReportForm: PoiReportForm) {
        poiReportForm.obj_id = Util.createObjId()
        appWebDao.addPoiRecord(poiReportForm)
    }

    override fun freqCheck(openid: String): Boolean =
        !appWebDao.checkFreqValid(openid)
}