package cn.devmeteor.weihashi.listener

import cn.devmeteor.weihashi.dao.SuDeviceDaoV2
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.redis.connection.Message
import org.springframework.data.redis.listener.KeyExpirationEventMessageListener
import org.springframework.data.redis.listener.RedisMessageListenerContainer
import org.springframework.stereotype.Component
import java.io.File

@Component
class RedisKeyExpireListener(container: RedisMessageListenerContainer) : KeyExpirationEventMessageListener(container) {

    @Autowired
    lateinit var deviceDaoV2: SuDeviceDaoV2

    override fun onMessage(message: Message, pattern: ByteArray?) {
        super.onMessage(message, pattern)
        handleImageCacheExpired(message.toString())
        handleDeviceIdeExpired(message.toString())
    }

    private fun handleDeviceIdeExpired(key: String) {
        if (!key.startsWith("Device-ID-")){
            return
        }
        val deviceId=key.removePrefix("Device-ID-")
        deviceDaoV2.removeDevice(deviceId)
    }

    private fun handleImageCacheExpired(key: String) {
        if (!key.startsWith("imageCache")){
            return
        }
        File(key).delete()
    }

}