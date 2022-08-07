package cn.devmeteor.weihashi.dao

import org.apache.ibatis.annotations.*
import org.springframework.stereotype.Repository

@Mapper
@Repository
interface SuDeviceDaoV2 {

    @Insert("insert into su_device_v2(device_id,host_name) values(#{deviceId},#{hostName})")
    fun addDevice(deviceId: String, hostName: String)

    @Update("update su_device_v2 set host_name=#{hostName} where device_id=#{device_id}")
    fun updateDeviceInfo(deviceId: String, hostName: String)

    @Select("select count(*) from su_device_v2 where device_id=#{deviceId}")
    fun deviceExists(deviceId: String): Boolean

    @Select("select device_id,host_name from su_device_v2 order by timestamp desc")
    fun getDeviceList(): ArrayList<HashMap<String, Any>>

    @Delete("delete from su_device_v2 where device_Id=#{deviceId}")
    fun removeDevice(deviceId: String)

}