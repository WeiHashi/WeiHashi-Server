package cn.devmeteor.weihashi.dao

import org.apache.ibatis.annotations.*
import org.springframework.stereotype.Repository

@Mapper
@Repository
interface CodeDao {
    @Insert("insert into code(openid,code,timestamp) values(#{openid},#{code},#{timestamp})")
    fun saveCode(openid:String,code:String,timestamp: Long)

    @Select("select count(*) from code where openid=#{openid} and code=#{code}")
    fun verifyCode(openid: String,code: String):Boolean

    @Delete("delete from code where #{timestamp}-timestamp>=300000")
    fun deleteCodeByTime(timestamp:Long)

    @Delete("delete from code where openid=#{openid}")
    fun deleteCodeById(openid:String)

    @Update("update code set code=#{code},timestamp=#{timestamp} where openid=#{openid}")
    fun updateCode(openid: String,code: String,timestamp: Long)

    @Select("select count(*) from code where openid=#{openid}")
    fun checkId(openid: String):Boolean
}