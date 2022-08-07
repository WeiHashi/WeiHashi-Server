package cn.devmeteor.weihashi.dao

import cn.devmeteor.weihashi.data.Notice
import org.apache.ibatis.annotations.*
import org.springframework.stereotype.Repository

@Mapper
@Repository
interface NoticeDao {
    @Select("SELECT COUNT(*) FROM notice")
    fun getNoticeCount(): Int

    @Select("select * from notice order by timestamp desc limit #{from},#{limit}")
    fun getNoticeList(from: Int, limit: Int): ArrayList<Notice>

    @Select("select * from notice where sender=#{sender} or (receiver like concat('%',#{classId},'%') and sender like '%\"id_type\":2%') order by timestamp desc limit #{from},#{limit}")
    fun getNoticeListBySender(sender: String,classId:String, from: Int, limit: Int): ArrayList<Notice>

    @Insert("insert into notice(obj_id,cate,title,content,detail,sender,receiver,type,top_level) values(#{obj_id},#{cate},#{title},#{content},#{detail},#{sender},#{receiver},#{type},#{top_level})")
    fun addNotice(notice: Notice)

    @Delete("delete from notice where sender=#{sender}")
    fun deleteNoticeBySender(sender: String)

    @Delete("delete from notice where obj_id=#{objId}")
    fun deleteNoticeByObjId(objId: String)

    @Deprecated("数据格式更新且有安全风险，已废弃")
    @Update("update notice set \${field}=#{value} where obj_id=#{objId}")
    fun updateNoticeById(objId: String, field: String, value: String)

    @Update("update notice set content=#{content},detail=#{detail} where obj_id=#{objId}")
    fun updateNoticeByIdV2(objId: String,content:String,detail:String)

    @Select("select count(*) from notice where sender=#{sender} or (receiver like concat('%',#{classId},'%') and sender like '%\"id_type\":2%')")
    fun getNoticeCountBySender(sender: String,classId: String): Int

    @Update("update notice set top_level=#{topLevel} where obj_id=#{objId}")
    fun setTop(objId: String,topLevel:Int)

    @Select("select count(*) from notice where sender=#{sender} and top_level!=0")
    fun getTopCount(sender: String):Int

    @Deprecated("数据映射有问题，已废弃",ReplaceWith("getMessagesV2(String,Int)"))
    @Select("select obj_id,cate,title,detail,sender,receiver,type,timestamp,top_level,0 as source from notice where (receiver like concat('%',#{receiver},'%') or type=1) union select obj_id,title,des,questions,sender,receiver,type,timestamp,top_level,1 as source from questionnaire where (receiver like concat('%',#{receiver},'%') or type=1) order by timestamp desc limit #{from},10")
    fun getMessages(receiver:String,from: Int):ArrayList<MutableMap<String,Any>>?

    @Select("select obj_id,cate,title,content,null as des,null as questions,detail,sender,receiver,type,timestamp,top_level,0 as source from notice where (receiver like concat('%',#{receiver},'%') or type=1) union select obj_id,null as cate,title,null as content,des,questions,null as detail,sender,receiver,type,timestamp,top_level,1 as source from questionnaire where (receiver like concat('%',#{receiver},'%') or type=1) order by timestamp desc limit #{from},10")
    fun getMessagesV2(receiver:String,from: Int):ArrayList<MutableMap<String,Any>>?

    @Deprecated("数据映射有问题，已废弃",ReplaceWith("getTopsV2(String,Int)"))
    @Select("select obj_id,cate,title,detail,sender,receiver,type,timestamp,top_level,0 as source from notice where (receiver like concat('%',#{receiver},'%') or type=1) and top_level!=0 union select obj_id,title,des,questions,sender,receiver,type,timestamp,top_level,1 as source from questionnaire where (receiver like concat('%',#{receiver},'%') or type=1) and top_level!=0 order by timestamp desc")
    fun getTops(receiver: String):ArrayList<MutableMap<String,Any>>?

    @Select("select obj_id,cate,title,content,null as des,null as questions,detail,sender,receiver,type,timestamp,top_level,0 as source from notice where (receiver like concat('%',#{receiver},'%') or type=1) and top_level!=0 union select obj_id,null as cate,title,null as content,des,questions,null as detail,sender,receiver,type,timestamp,top_level,1 as source from questionnaire where (receiver like concat('%',#{receiver},'%') or type=1) and top_level!=0 order by timestamp desc")
    fun getTopsV2(receiver: String):ArrayList<MutableMap<String,Any>>?

    @Select("select qs+ns from((select count(*) as ns from notice where (receiver like concat('%',#{receiver},'%') or type=1))n,(select count(*) as qs from questionnaire where (receiver like concat('%',#{receiver},'%') or type=1))q)")
    fun getMessageTotal(receiver: String):Int

    @Select("select obj_id,cate,title,detail,sender,receiver,type,timestamp,top_level,0 as source from notice where type=1 union select obj_id,title,des,questions,sender,receiver,type,timestamp,top_level,1 as source from questionnaire where type=1 order by timestamp desc")
    fun getSystemMessages():ArrayList<MutableMap<String,Any>>?

    @Select("select obj_id,cate,title,detail,sender,receiver,type,timestamp,top_level,0 as source from notice where type=1 and top_level!=0 union select obj_id,title,des,questions,sender,receiver,type,timestamp,top_level,1 as source from questionnaire where type=1 and top_level!=0 order by timestamp desc")
    fun getSystemTops():ArrayList<MutableMap<String,Any>>?

    @Select("select * from notice where obj_id=#{objId}")
    fun getNoticeById(objId: String):Notice?
}