package cn.devmeteor.weihashi.dao

import cn.devmeteor.weihashi.data.Task
import org.apache.ibatis.annotations.Delete
import org.apache.ibatis.annotations.Insert
import org.apache.ibatis.annotations.Mapper
import org.apache.ibatis.annotations.Select
import org.springframework.stereotype.Repository

@Mapper
@Repository
interface TaskDao {

    @Select("select * from tasks where #{current}-timestamp<1000 and #{current}-timestamp>=0")
    fun getCurrentTask(current:Long):List<Task>

    @Insert("insert into tasks(obj_id,openid,detail,timestamp,push,mail) values(#{obj_id},#{openid},#{detail},#{timestamp},#{push},#{mail})")
    fun addTask(task: Task)

    @Delete("delete from tasks where obj_id=#{objId}")
    fun deleteTask(objId:String)
}