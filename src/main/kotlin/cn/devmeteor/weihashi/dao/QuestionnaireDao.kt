package cn.devmeteor.weihashi.dao

import cn.devmeteor.weihashi.data.Questionnaire
import org.apache.ibatis.annotations.*
import org.springframework.stereotype.Repository

@Mapper
@Repository
interface QuestionnaireDao {
    @Select("SELECT COUNT(*) FROM questionnaire")
    fun getQuestionnaireCount(): Int

    @Select("SELECT COUNT(*) FROM questionnaire where sender=#{sender} or (receiver like concat('%',#{classId},'%') and sender like '%\"id_type\":2%')")
    fun getQuestionnaireCountBySender(sender: String,classId: String): Int

    @Select("select * from questionnaire where obj_id=#{objId}")
    fun getQuestionnaire(objId: String):Questionnaire?

    @Select("select * from questionnaire limit #{from},#{limit}")
    fun getQuestionnaireList(from: Int, limit: Int): ArrayList<Questionnaire>

    @Select("select * from questionnaire where sender=#{sender} or (receiver like concat('%',#{classId},'%') and sender like '%\"id_type\":2%') order by timestamp desc limit #{from},#{limit}")
    fun getQuestionnaireListBySender(sender: String,classId:String, from: Int, limit: Int): ArrayList<Questionnaire>

    @Insert("insert into questionnaire(obj_id,title,des,questions,sender,receiver,type,top_level) values(#{obj_id},#{title},#{des},#{questions},#{sender},#{receiver},#{type},#{top_level})")
    fun addQuestionnaire(questionnaire: Questionnaire)

    @Delete("delete from questionnaire where sender=#{sender}")
    fun deleteQuestionnaireBySender(sender: String)

    @Delete("delete from questionnaire where obj_id=#{objId}")
    fun deleteQuestionnaireByObjId(objId: String)

    @Update("update questionnaire set \${field}=#{value} where obj_id=#{objId}")
    fun updateQuestionnaireById(objId: String, field: String, value: String)

    @Update("update questionnaire set top_level=#{topLevel} where obj_id=#{objId}")
    fun setTop(objId: String, topLevel: Int)

    @Select("select count(*) from questionnaire where sender=#{sender} and top_level!=0")
    fun getTopCount(sender: String):Int
}