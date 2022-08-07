package cn.devmeteor.weihashi.dao

import cn.devmeteor.weihashi.data.QuestionnaireCollection
import org.apache.ibatis.annotations.*
import org.springframework.stereotype.Repository

@Mapper
@Repository
interface QuestionnaireCollectionDao {
    @Delete("delete from questionnaire_collection where questionnaire_id=#{qid}")
    fun deleteQuestionnaireCollectionByQid(qid: String)

    @Select("select * from questionnaire_collection where questionnaire_id=#{qid} limit #{from},#{limit}")
    fun getQuestionnaireCollectionByQid(qid: String, from: Int, limit: Int): ArrayList<MutableMap<String,Any>>

    @Delete("delete from questionnaire_collection where obj_id=#{objId}")
    fun deleteQuestionnaireCollectionByObjId(objId: String)

    @Select("select count(*) from questionnaire_collection where questionnaire_id=#{qid}")
    fun countQuestionnaireCollectionByQid(qid: String):Int

    @Insert("insert into questionnaire_collection(obj_id,questionnaire_id,openid,content) values(#{obj_id},#{questionnaire_id},#{openid},#{content})")
    fun submitQuestionnaire(questionnaireCollection: QuestionnaireCollection)

    @Select("select * from questionnaire_collection where openid=#{openid} and questionnaire_id=#{qid}")
    fun getSavedCollection(openid:String,qid: String):QuestionnaireCollection?

    @Update("update questionnaire_collection set content=#{content} where obj_id=#{objId}")
    fun coverCollection(objId: String,content:String)

}