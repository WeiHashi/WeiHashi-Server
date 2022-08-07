package cn.devmeteor.weihashi.service

import cn.devmeteor.weihashi.data.Response

interface JwService {
   fun login(openid:String,username:String,password:String):Response
   fun getGrade(cookieString:String):Response
   fun getLessons(cookieString:String):Response
}