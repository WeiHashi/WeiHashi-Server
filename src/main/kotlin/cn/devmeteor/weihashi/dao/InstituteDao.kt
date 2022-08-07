package cn.devmeteor.weihashi.dao

import cn.devmeteor.weihashi.data.Institute
import org.apache.ibatis.annotations.Insert
import org.apache.ibatis.annotations.Mapper
import org.apache.ibatis.annotations.Select
import org.apache.ibatis.annotations.Update
import org.springframework.stereotype.Repository

@Mapper
@Repository
interface InstituteDao {
    @Select("select * from institute")
    fun getInstituteList(): ArrayList<Institute>

    @Insert("insert into institute(institute_id,name,username,password) values(#{institute_id},#{name},#{username},#{password})")
    fun addInstitute(institute: Institute)

    @Select("select name from institute where institute_id=#{instituteId}")
    fun getInstituteNameById(instituteId: String): String?

    @Update("update institute set \${field}=#{value} where institute_id=#{instituteId}")
    fun updateInstituteById(instituteId: String, field: String, value: String)

    @Select("select count(*) from institute where username=#{username} and password=#{password}")
    fun verifyPassword(username: String, password: String):Int

    @Update("update institute set token=#{token} where username=#{username}")
    fun updateToken(token:String,username: String)

    @Select("select count(*) from institute where username=#{username} and token=#{token}")
    fun verifyToken(username: String,token: String)
}