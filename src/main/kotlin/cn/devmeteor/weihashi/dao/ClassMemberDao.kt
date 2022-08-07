package cn.devmeteor.weihashi.dao

import cn.devmeteor.weihashi.data.Student
import org.apache.ibatis.annotations.*
import org.springframework.stereotype.Repository

@Repository
interface ClassMemberDao {
    @Select("SELECT * FROM class_member WHERE class_id=#{classId}")
    fun getStudentList(@Param("classId") classId: String): ArrayList<Student>

    @Insert("INSERT INTO class_member(openid,class_id) VALUES(#{openid},#{class_id})")
    fun addMember(@Param("openid") openid: String, @Param("class_id") classId: String)

    @Delete("delete from class_member where openid=#{openid} and class_id=#{class_id}")
    fun removeMember(@Param("openid") openid: String, @Param("class_id") classId: String)

    @Select("select count(*) from class_member where openid=#{openid} and class_id=#{class_id}")
    fun memberExists(@Param("openid")openid: String,@Param("class_id")classId: String):Boolean

    @Select("select count(*) from class_member where openid=#{openid} and class_id!=#{class_id}")
    fun memberInOtherClass(@Param("openid")openid: String,@Param("class_id")classId: String):Boolean

    @Delete("delete from class_member where class_id=#{class_id}")
    fun deleteMemberAfterClassDeleted(class_id:String)

    @Select("select count(*) from class_member where class_id=#{classId}")
    fun getClassMemberCount(classId: String):Int

    @Select("select * from class_member where class_id=#{classId} limit #{from},#{limit}")
    fun getClassMemberById(classId: String,from:Int,limit:Int):ArrayList<MutableMap<String,String>>

    @Update("update class_member set is_cadre=#{isCadre} where md5(md5(openid))=#{openid}")
    fun setCadre(openid: String,isCadre:Boolean)

    @Delete("delete from class_member where md5(md5(openid))=#{openid}")
    fun deleteClassMemberById(openid: String)

    @Select("select class_id from class_member where openid=#{openid}")
    fun getClassIdById(openid: String):String?

    @Select("select count(*) from class_member where class_id=#{class_id} and is_cadre=true")
    fun getCadreCount(classId: String):Int

}