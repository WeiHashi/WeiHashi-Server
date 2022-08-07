package cn.devmeteor.weihashi.dao

import cn.devmeteor.weihashi.data.User
import org.apache.ibatis.annotations.Insert
import org.apache.ibatis.annotations.Mapper
import org.apache.ibatis.annotations.Select
import org.apache.ibatis.annotations.Update
import org.springframework.stereotype.Repository

@Mapper
@Repository
interface UserDao {
    @Select("SELECT COUNT(*) FROM user")
    fun getUserCount(): Int

    @Select("SELECT COUNT(*) FROM user WHERE name!=''")
    fun getUserCountBinded(): Int

    @Select("SELECT COUNT(*) FROM user WHERE TO_DAYS(latest_use) = TO_DAYS(NOW())")
    fun getUserCountToday(): Int

    @Select("select * from user order by latest_use desc limit #{from},#{limit}")
    fun getUserList(from: Int, limit: Int): ArrayList<User>

    @Select("select name,studentid,nickname from user where openid=#{openid}")
    fun getStudentInfoById(openid: String): Map<String, String>?

    @Select("select name,studentid,nickname from user where md5(md5(openid))=#{id}")
    fun getStudentInfoByHashedId(id: String): Map<String, String>?

    @Select("select openid from user where md5(md5(openid))=#{hashedId}")
    fun getIdByHashedId(hashedId: String): String

    @Update("update user set name=#{name},studentid=#{studentid} where openid=#{openid}")
    fun updateJwInfo(openid: String, name: String, studentid: String)

    @Select("select count(*) from user where openid=#{openid}")
    fun checkUser(openid: String): Boolean

    @Update("update user set latest_use=now(),nickname=#{nickname},head=#{head} where openid=#{openid}")
    fun updateUseInfo(openid: String, nickname: String, head: String)

    @Insert("insert into user(openid,nickname,head,platform) values(#{openid},#{nickname},#{head},#{platform})")
    fun createUser(openid: String, nickname: String, head: String, platform: Int)

    @Select("select email from user where openid=#{openid}")
    fun getUserInfo(openid: String): MutableMap<String, Any>

    @Update("update user set email=#{email} where openid=#{openid}")
    fun bindMail(openid: String, email: String)

    @Update("update user set email='' where openid=#{openid}")
    fun unbindMail(openid: String)

    @Select("select user.email,classes.class_id,class_member.is_cadre,classes.name as className,institute.name as instituteName from user,class_member,classes,institute where user.openid=#{openid} and user.openid=class_member.openid and class_member.class_id=classes.class_id and classes.institute_id=institute.institute_id")
    fun getInfoById(openid: String): MutableMap<String, Any>?

}