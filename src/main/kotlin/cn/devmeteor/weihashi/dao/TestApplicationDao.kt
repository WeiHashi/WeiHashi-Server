package cn.devmeteor.weihashi.dao

import cn.devmeteor.weihashi.data.TestApplication
import org.apache.ibatis.annotations.Mapper
import org.apache.ibatis.annotations.Select
import org.apache.ibatis.annotations.Update
import org.springframework.stereotype.Repository

@Mapper
@Repository
interface TestApplicationDao {

    @Select("SELECT COUNT(*) FROM test_application WHERE openid=#{openid} AND version=#{version} AND enabled=1")
    fun getTestFlag(openid: String, version: String): Boolean

    @Update("update test_application set enabled=#{enabled} where openid=#{openid} and version=#{version}")
    fun updateTestFlag(openid: String, version: String, enabled: Boolean)

    @Select("SELECT * FROM test_application WHERE enabled=#{enabled} AND version LIKE #{version} ORDER BY timestamp DESC limit #{from},#{limit}")
    fun getApplicationList(from: Int, limit: Int, enabled: Boolean, version: String): List<TestApplication>

    @Select("SELECT COUNT(*) FROM test_application WHERE enabled=#{enabled} AND version LIKE #{version}")
    fun getApplicationCount(enabled: Boolean, version: String): Int

    @Select("SELECT DISTINCT version FROM test_application ORDER BY CONVERT(SUBSTRING_INDEX(version,'.',1),SIGNED) DESC,CONVERT(SUBSTRING_INDEX(SUBSTRING_INDEX(version,'.',-2),'.',1),SIGNED) DESC,CONVERT(SUBSTRING_INDEX(version,'.',-1),SIGNED) DESC")
    fun getVersionList(): List<String>

    @Select("SELECT DISTINCT version FROM test_application ORDER BY CONVERT(SUBSTRING_INDEX(version,'.',1),SIGNED) DESC,CONVERT(SUBSTRING_INDEX(SUBSTRING_INDEX(version,'.',-2),'.',1),SIGNED) DESC,CONVERT(SUBSTRING_INDEX(version,'.',-1),SIGNED) DESC LIMIT 1")
    fun getLatestVersion(): String

}