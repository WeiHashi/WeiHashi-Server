package cn.devmeteor.weihashi.config

import com.fasterxml.jackson.annotation.JsonAutoDetect
import com.fasterxml.jackson.annotation.PropertyAccessor
import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.redis.connection.RedisConnectionFactory
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.data.redis.listener.RedisMessageListenerContainer
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer
import org.springframework.data.redis.serializer.StringRedisSerializer

@Configuration
class RedisConfig {
    @Bean
    fun redisTemplate(redisConnectionFactory: RedisConnectionFactory?): RedisTemplate<String, Any> {
        val template = RedisTemplate<String, Any>()
        template.setConnectionFactory(redisConnectionFactory!!)
        val jackson2JsonRedisSerializer: Jackson2JsonRedisSerializer<*> = Jackson2JsonRedisSerializer<Any>(
            Any::class.java
        )
        val om = ObjectMapper()
        om.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.ANY)
//        om.enableDefaultTyping(ObjectMapper.DefaultTyping.NON_FINAL)
        jackson2JsonRedisSerializer.setObjectMapper(om)
        // String 序列化
        val stringRedisSerializer = StringRedisSerializer()
        // key采用String的序列化方式
        template.keySerializer = stringRedisSerializer
        // hash的key也采用String的序列化方式
        template.hashKeySerializer = stringRedisSerializer
        // value序列化方式采用jackson
        template.valueSerializer = jackson2JsonRedisSerializer
        // hash的value序列化方式采用jackson
        template.hashValueSerializer = jackson2JsonRedisSerializer
        template.afterPropertiesSet()
        return template
    }

    @Autowired
    lateinit var redisConnectionFactory: RedisConnectionFactory

    @Bean
    fun listenerConfig():RedisMessageListenerContainer=
        RedisMessageListenerContainer().apply {
            setConnectionFactory(redisConnectionFactory)
        }
}