package cn.devmeteor.weihashi.dao

import cn.devmeteor.weihashi.data.Banner
import org.apache.ibatis.annotations.*
import org.springframework.stereotype.Repository

@Mapper
@Repository
interface BannerDao {
    @Select("select * from banner where enabled=true order by prior desc")
    fun getBanner():ArrayList<Banner>

    @Select("select * from banner order by enabled desc,prior desc limit #{from},#{limit}")
    fun getBannerList(from:Int,limit:Int):ArrayList<Banner>

    @Select("select count(*) from banner")
    fun getBannerCount():Int

    @Delete("delete from banner where obj_id=#{id}")
    fun deleteBanner(id:String)

    @Update("update banner set \${field}=#{value} where obj_id=#{id}")
    fun updateBannerById(id: String,field:String,value:String)

    @Update("update banner set enabled=#{enabled} where obj_id=#{id}")
    fun enableBanner(id:String,enabled:Boolean)

    @Insert("insert into banner values(#{obj_id},#{url},#{type},#{content_id},#{prior},#{enabled})")
    fun addBanner(banner: Banner)

}