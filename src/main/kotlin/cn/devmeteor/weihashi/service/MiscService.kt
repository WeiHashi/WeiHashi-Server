package cn.devmeteor.weihashi.service

import cn.devmeteor.weihashi.data.Response
import cn.devmeteor.weihashi.data.Task

interface MiscService {
    fun getBanner():Response
    fun addTask(task: Task)
    fun getBannerContent(type:Int,objId:String):Response
    fun sendMail(addr:String,subject:String,msg:String):Boolean
}