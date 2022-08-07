package cn.devmeteor.weihashi.dao

import org.apache.ibatis.annotations.Insert
import org.apache.ibatis.annotations.Mapper
import org.apache.ibatis.annotations.Select
import org.apache.ibatis.annotations.Update
import org.springframework.stereotype.Repository

@Deprecated("因验证策略修改，已废弃")
@Mapper
@Repository
interface SuDeviceDao {
    @Select("select count(*) from su_device where mac=#{mac}")
    fun verifyMac(mac: String): Boolean

    @Insert("insert into su_device(mac,host_name,ip) values(#{mac},#{hostName},#{ip})")
    fun addDevice(mac: String, hostName: String, ip: String?)

    @Select("select allow from su_device where mac=#{mac}")
    fun getAllowByMac(mac: String): Boolean

    @Update("update su_device set host_name=#{hostName},ip=#{ip} where mac=#{mac}")
    fun updateDeviceInfo(mac: String, hostName: String, ip: String?)

    @Select("select count(*) from su_device where ip=#{ip} and allow=1")
    fun verifyIp(ip: String): Boolean

    @Select("select mac,host_name,allow from su_device order by timestamp desc")
    fun getDeviceList(): ArrayList<Map<String, Any>>

    @Update("update su_device set allow=#{allow} where mac=#{mac}")
    fun allowDevice(mac:String,allow:Boolean)
}