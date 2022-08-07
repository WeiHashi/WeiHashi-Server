package cn.devmeteor.weihashi.controller

import cn.devmeteor.weihashi.dao.SuDeviceDaoV2
import cn.devmeteor.weihashi.data.Response
import cn.devmeteor.weihashi.data.ResultCode
import cn.devmeteor.weihashi.util.RedisUtil
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.time.Duration

@RestController
@RequestMapping("/v2/device")
class DeviceControllerV2 {

    @Autowired
    lateinit var suDeviceDaoV2: SuDeviceDaoV2

    @Autowired
    lateinit var redisUtil: RedisUtil

    @GetMapping("/list")
    fun getDeviceList(): ArrayList<HashMap<String, Any>> =
        suDeviceDaoV2.getDeviceList().onEach {
            it["allow"] = redisUtil.get("Device-ID-${it["device_id"]}", false)
        }

    @PutMapping("/allow")
    fun allowDevice(deviceId: String, allow: Boolean) {
        val key = "Device-ID-$deviceId"
        if (allow) {
            redisUtil.set(key, 1, Duration.ofDays(7))
            return
        }
        redisUtil.del(key)
    }

    @GetMapping("/verify")
    fun verifyDevice(hostName: String, deviceId: String): Response {
        if (!suDeviceDaoV2.deviceExists(deviceId)) {
            suDeviceDaoV2.addDevice(deviceId, hostName)
            return Response(ResultCode.INVALID_DEVICE)
        }
        val allow = redisUtil.get("Device-ID-${deviceId}", false)
        if (!allow)
            return Response(ResultCode.DEVICE_VERIFYING)
        suDeviceDaoV2.updateDeviceInfo(deviceId, hostName)
        return Response(ResultCode.VERIFY_OK)
    }

}