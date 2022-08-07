package cn.devmeteor.weihashi

import org.mybatis.spring.annotation.MapperScan
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import java.util.*

@MapperScan("cn.devmeteor.weihashi.dao")
@SpringBootApplication
class WeiHaShiApplication

fun main(args: Array<String>) {
    TimeZone.setDefault(TimeZone.getTimeZone("Asia/Shanghai"))
    runApplication<WeiHaShiApplication>(*args)

}
