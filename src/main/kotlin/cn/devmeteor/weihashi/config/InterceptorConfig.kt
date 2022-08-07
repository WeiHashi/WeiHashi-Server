package cn.devmeteor.weihashi.config

import cn.devmeteor.weihashi.dao.ClassDao
import cn.devmeteor.weihashi.interceptor.AdminInterceptor
import cn.devmeteor.weihashi.interceptor.DeviceInterceptor
import cn.devmeteor.weihashi.interceptor.SuInterceptor
import cn.devmeteor.weihashi.util.RedisUtil
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Configuration
import org.springframework.web.servlet.config.annotation.InterceptorRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer

@Configuration
class InterceptorConfig : WebMvcConfigurer {

    @Autowired
    lateinit var classDao: ClassDao

    @Autowired
    lateinit var redisUtil: RedisUtil

    override fun addInterceptors(registry: InterceptorRegistry) {
        registry.addInterceptor(SuInterceptor(redisUtil))
                .addPathPatterns("/su*","/v2/su/*")
                .excludePathPatterns("/suVerifyDevice")
        registry.addInterceptor(AdminInterceptor(classDao))
                .addPathPatterns("/admin*")
                .excludePathPatterns("/adminLogin")
        registry.addInterceptor(DeviceInterceptor())
            .addPathPatterns("/getDeviceList")
            .addPathPatterns("/allowDevice")
            .addPathPatterns("/v2/device/*")
            .excludePathPatterns("/v2/device/verify")
        super.addInterceptors(registry)
    }

}