package cn.devmeteor.weihashi.util

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.stereotype.Component
import java.time.Duration

@Component
class RedisUtil {

    @Autowired
    lateinit var redisTemplate: RedisTemplate<String, Any>

    //---------------------------Common-----------------------------------

    fun del(key: String): Boolean = try {
        redisTemplate.delete(key)
    } catch (e: Exception) {
        false
    }

    //---------------------------Value-------------------------------------

    fun setNX(key: String, value: Any): Boolean = try {
        redisTemplate.opsForValue().setIfAbsent(key, value) ?: false
    } catch (e: Exception) {
        e.printStackTrace()
        false
    }

    fun set(key: String, value: Any, timeout: Duration? = null) = try {
        if (timeout == null) {
            redisTemplate.opsForValue().set(key, value)
        } else {
            redisTemplate.opsForValue().set(key, value, timeout)
        }
    } catch (e: Exception) {
        e.printStackTrace()
    }

    fun incr(key: String, step: Long = 1): Long? = try {
        redisTemplate.opsForValue().increment(key, step)
    } catch (e: Exception) {
        e.printStackTrace()
        -1
    }

    fun <T : Any> get(key: String, baseValue: T): T {
        try {
            val res = redisTemplate.opsForValue().get(key) ?: return baseValue
            if (baseValue is Boolean && res is Int) {
                return (res != 0) as T
            }
            return res as T
        } catch (e: Exception) {
            e.printStackTrace()
            return baseValue
        }
    }

    fun <T : Any> trySetDefaultAndGet(key: String, baseValue: T): T {
        try {
            setNX(key, baseValue)
            val res = redisTemplate.opsForValue().get(key) ?: baseValue
            if (baseValue is Boolean && res is Int) {
                return (res != 0) as T
            }
            return res as T
        } catch (e: Exception) {
            e.printStackTrace()
            return baseValue
        }
    }

    //---------------------------List----------------------------------------

    fun lPush(key: String, value: Any): Long = try {
        redisTemplate.opsForList().leftPush(key, value) ?: -1
    } catch (e: Exception) {
        e.printStackTrace()
        -1
    }

    fun <T> lRange(key: String, start: Long, end: Long): MutableList<T>? = try {
        redisTemplate.opsForList().range(key, start, end) as MutableList<T>
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }

    //---------------------------ZSet----------------------------------------

    fun zAdd(key: String, value: Any, score: Double): Boolean = try {
        redisTemplate.opsForZSet().add(key, value, score) ?: false
    } catch (e: Exception) {
        e.printStackTrace()
        false
    }

    fun <T> zRevRangeByScore(key: String, max: Double, min: Double, limit: Long, offset: Long = 0): Set<T>? = try {
        redisTemplate.opsForZSet().reverseRangeByScore(key, max, min, offset, limit) as Set<T>
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }

    // --------------------------HyperLogLog-------------------------------

    fun pfAdd(key: String, vararg value: Any): Long = try {
        redisTemplate.opsForHyperLogLog().add(key, value)
    } catch (e: Exception) {
        e.printStackTrace()
        -1
    }

    fun pfCount(key: String): Long = try {
        redisTemplate.opsForHyperLogLog().size(key)
    } catch (e: Exception) {
        e.printStackTrace()
        -1
    }


}