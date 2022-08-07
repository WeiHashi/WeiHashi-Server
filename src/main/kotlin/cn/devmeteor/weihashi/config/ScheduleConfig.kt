package cn.devmeteor.weihashi.config

import cn.devmeteor.weihashi.controller.SuControllerV2
import cn.devmeteor.weihashi.dao.CodeDao
import cn.devmeteor.weihashi.dao.TaskDao
import cn.devmeteor.weihashi.dao.UserDao
import cn.devmeteor.weihashi.data.DateValueRecord
import cn.devmeteor.weihashi.getEnv
import cn.devmeteor.weihashi.service.MiscService
import cn.devmeteor.weihashi.service.impl.UserImpl
import cn.devmeteor.weihashi.util.RedisUtil
import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory
import org.json.JSONObject
import org.jsoup.Connection
import org.jsoup.Jsoup
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.scheduling.annotation.Async
import org.springframework.scheduling.annotation.EnableAsync
import org.springframework.scheduling.annotation.EnableScheduling
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import java.text.SimpleDateFormat
import java.util.*

@Component
@EnableScheduling
@EnableAsync
class ScheduleConfig {
    private var accessToken = ""
    private val appid = getEnv("QP_APP_ID")
    private val appSecret= getEnv("QP_APP_SECRET")
    private val log: Log = LogFactory.getLog(ScheduleConfig::class.java)

    @Autowired
    lateinit var taskDao: TaskDao

    @Autowired
    lateinit var userDao: UserDao

    @Autowired
    lateinit var codeDao: CodeDao

    @Autowired
    lateinit var miscService: MiscService

    @Autowired
    lateinit var redisUtil: RedisUtil

    @Async
    @Scheduled(cron = "0 * * * * ?")
    fun test() {
        val tasks = taskDao.getCurrentTask(System.currentTimeMillis())
        if (tasks.isNotEmpty()) {
            for (task in tasks) {
                if (task.push) {
                    log.info("prepare to push to:${task.openid}")
                    try {
                        val res =
                            Jsoup.connect("https://api.q.qq.com/api/json/subscribe/SendSubscriptionMessage?access_token=$accessToken")
                                .header("content-type", "application/json")
                                .requestBody(
                                    """
                                    {
                                      "touser": "${task.openid}",
                                      "template_id": "4ce15fc3964bbd9583ad07761ba36ea0",
                                      "page":"pages/task/task",
                                      "data": {
                                        "keyword1": {
                                          "value": "${task.detail}"
                                        },
                                        "keyword2": {
                                          "value": "${
                                        SimpleDateFormat("yyyy年MM月dd日 HH:mm", Locale.CHINESE).format(
                                            Date(
                                                task.timestamp
                                            )
                                        )
                                    }"
                                        }
                                      }
                                    }
                                """.trimIndent()
                            )
                            .method(Connection.Method.POST)
                            .ignoreContentType(true)
                            .execute()
                        val resObj = JSONObject(res.body())
                        log.info(resObj.toString())
                        if (resObj.getInt("errcode") != 0)
                            log.error("推送失败：${task.openid}")
                    } catch (e: Exception) {
                        e.printStackTrace()
                        log.error("推送失败：${task.openid}")
                    }
                }
                if (task.mail) {
                    println("prepare to mail to:${task.openid}")
                    val addr = userDao.getUserInfo(task.openid)["email"] as String
                    val success = miscService.sendMail(
                        addr,
                        "微哈师-任务提醒",
                        """<div style="display: flex;flex-direction: column;align-items: center;width: 100%;"><img style="width: 100px;height:100px;" src="https://devmeteor.cn/whs/img/whs.png"><text style="font-size: 30px;margin-top: 20px;">微哈师-任务提醒</text><div style="padding: 20px;max-width: 80%;font-size: 20px;"><p style="font-weight: bold;">任务名称：</p><p style="text-indent: 2em;">${task.detail}</p><p style="font-weight: bold;">时间：</p><p style="text-indent: 2em;">${
                            SimpleDateFormat(
                                "yyyy年MM月dd日 HH:mm",
                                Locale.CHINESE
                            ).format(Date(task.timestamp))
                        }</p><p style="font-size: 16px;">扫描下方小程序码进入小程序管理任务：</p><img style="width: 150px;height: 150px;margin: 0px auto;display: block;" src="https://devmeteor.cn/whs/img/whs_code.png"><div style="height: 1px;background-color:#f2f2f2;width: 100%;margin-top: 20px"></div><p style="color: #888888;font-size: 16px;margin: 0px;padding: 0px;padding-top: 10px;">如果这不是您设置的提醒邮件请联系：2633979287@qq.com</p></div></div>"""
                    )
                    if (!success)
                        log.error("邮件发送失败：${task.openid}，${addr}")
                }
                taskDao.deleteTask(task.obj_id!!)
            }
        }
    }

    @Async
    @Scheduled(fixedRate = 7200000)
    fun getToken() {
        val res =
            Jsoup.connect("https://api.q.qq.com/api/getToken?grant_type=client_credential&appid=$appid&secret=$appSecret")
                .method(Connection.Method.GET)
                .ignoreContentType(true)
                .execute()
        val resObj = JSONObject(res.body())
        if (resObj.getInt("errcode") == 0)
            accessToken = resObj.getString("access_token")
        else
            log.error("获取access_token失败")
    }

    @Async
    @Scheduled(fixedRate = 60000)
    fun deleteExpiredCode() {
        codeDao.deleteCodeByTime(System.currentTimeMillis())
    }

    @Async
    @Scheduled(cron = "0 0 0 ? * *")
    fun saveStatistics() {
        val date = SimpleDateFormat("yyyy-MM-dd", Locale.CHINA).format(Date(System.currentTimeMillis() - 60 * 1000))
        log.info("===========统计${date}数据==================")
        if (getEnv("PORT") != "15002") {
            return
        }
        val uv = redisUtil.pfCount(UserImpl.KEY_UV)
        redisUtil.del(UserImpl.KEY_UV)
        val du = redisUtil.trySetDefaultAndGet(UserImpl.KEY_DU, 0)
        redisUtil.del(UserImpl.KEY_DU)
        redisUtil.lPush(SuControllerV2.KEY_UV_RECORD, DateValueRecord(date, uv))
        redisUtil.lPush(SuControllerV2.KEY_DU_RECORD, DateValueRecord(date, du))
    }
}