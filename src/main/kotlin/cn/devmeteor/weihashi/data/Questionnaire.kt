package cn.devmeteor.weihashi.data

import java.sql.Timestamp

data class Questionnaire(var obj_id: String?, var title: String, var des: String, var questions: String, var sender: String?,var receiver:String?, var type:Int?,var timestamp: Timestamp?, var top_level: Int?)