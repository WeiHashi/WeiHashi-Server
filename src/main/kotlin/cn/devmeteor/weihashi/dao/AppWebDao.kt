package cn.devmeteor.weihashi.dao

import cn.devmeteor.weihashi.data.PoiReportForm
import cn.devmeteor.weihashi.data.TestApplication
import org.apache.ibatis.annotations.Insert
import org.apache.ibatis.annotations.Mapper
import org.apache.ibatis.annotations.Select
import org.springframework.stereotype.Repository

@Mapper
@Repository
interface AppWebDao {
    @Insert("insert into test_application(openid,name,studentid,email) values(#{openid},#{name},#{studentid},#{email})")
    fun addTestApplication(testApplication: TestApplication)

    @Select("select count(*) from test_application where openid=#{openid}")
    fun checkSubmit(openid: String): Boolean

    @Insert("insert into poi_report(obj_id,openid,name,location) values(#{obj_id},#{openid},#{name},#{location})")
    fun addPoiRecord(poiReportForm: PoiReportForm)

    @Select("SELECT COUNT(*)<5 FROM poi_report WHERE TO_DAYS(timestamp) = TO_DAYS(NOW()) AND openid=#{openid}")
    fun checkFreqValid(openid: String): Boolean
}