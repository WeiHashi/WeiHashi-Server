package cn.devmeteor.weihashi.dao

import cn.devmeteor.weihashi.data.Classes
import org.apache.ibatis.annotations.*
import org.springframework.stereotype.Repository

@Mapper
@Repository
interface ClassDao {
    @Select("SELECT COUNT(*) FROM classes")
    fun getClassCount(): Int

    @Select("select * from classes where institute_id=#{institute_id} limit #{from},#{limit}")
    fun getClassList(institute_id: String, from: Int, limit: Int): List<Classes>

    @Select("select name from classes where class_id=#{class_id}")
    fun getClassNameById(class_id: String): String?

    @Insert("insert into classes(class_id,name,institute_id,username,password) values(#{class_id},#{name},#{institute_id},#{username},#{password})")
    fun addClass(classes: Classes)

    @Delete("delete from classes where class_id=#{class_id}")
    fun deleteClass(class_id: String)

    @Update("update classes set \${field}=#{value} where class_id=#{classId}")
    fun updateClassById(classId: String, field: String, value: String)

    @Select("select count(*) from classes where username=#{username} and password=#{password}")
    fun verifyPassword(username: String, password: String): Boolean

    @Update("update classes set token=#{token} where username=#{username}")
    fun updateTokenByUsername(token: String, username: String)

    @Select("select * from classes where username=#{username}")
    fun getClassInfoByUsername(username: String): Classes

    @Update("update classes set password=#{password} where username=#{username}")
    fun updatePasswordByUsername(username: String, password: String)

    @Select("select count(*) from classes where username=#{username} and token=#{token}")
    fun verifyToken(username: String,token: String):Boolean

}