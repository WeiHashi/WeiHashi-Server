package cn.devmeteor.weihashi.data

import java.io.Serializable

data class FormattedLesson(var term:String,var week:String,var name:String,var weekDay:String,var start:Int,var end:Int?,var teacher:String,var place:String,var date:String,var identifier:String):Serializable