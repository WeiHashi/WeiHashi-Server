package cn.devmeteor.weihashi.controller

import cn.devmeteor.weihashi.dao.SuDeviceDao
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RestController

@Deprecated("因验证策略修改，已废弃")
@RestController
class DeviceController {

    @Autowired
    lateinit var suDeviceDao: SuDeviceDao

    @GetMapping("/getDeviceList")
    fun getDeviceList(): ArrayList<Map<String, Any>> = suDeviceDao.getDeviceList()

    @PutMapping("/allowDevice")
    fun allowDevice(mac: String, allow: Boolean) = suDeviceDao.allowDevice(mac, allow)

}