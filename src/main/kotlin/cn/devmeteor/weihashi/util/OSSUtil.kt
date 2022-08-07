package cn.devmeteor.weihashi.util

import cn.devmeteor.weihashi.getEnv
import com.qcloud.cos.COSClient
import com.qcloud.cos.ClientConfig
import com.qcloud.cos.auth.BasicCOSCredentials
import com.qcloud.cos.http.HttpProtocol
import com.qcloud.cos.model.PutObjectRequest
import com.qcloud.cos.model.PutObjectResult
import com.qcloud.cos.region.Region
import org.springframework.web.multipart.MultipartFile
import java.io.File
import kotlin.random.Random

object OSSUtil {

    private val secretId = getEnv("COS_SECRET_ID")
    private val secretKey = getEnv("COS_SECRET_KEY")
    private val region = getEnv("COS_REGION")
    private val bucket = getEnv("COS_BUCKET")

    private fun createClient(): COSClient {
        val cred = BasicCOSCredentials(secretId, secretKey)
        val clientConfig = ClientConfig(Region(region))
        clientConfig.httpProtocol = HttpProtocol.https
        return COSClient(cred, clientConfig)
    }

    fun add(path: String, file: MultipartFile): PutObjectResult? = try {
        val tmpFilePrefix = "${file.name}${Random(System.currentTimeMillis())}"
        val tmpFile = File.createTempFile(tmpFilePrefix, "tmp")
        file.transferTo(tmpFile)
        add(path, tmpFile)
    } catch (e: Exception) {
        null
    }

    fun add(path: String, file: File): PutObjectResult? =
        with(createClient()) {
            try {
                val request = PutObjectRequest(bucket, path, file)
                putObject(request)
            } catch (e: Exception) {
                e.printStackTrace()
                null
            } finally {
                shutdown()
            }
        }

    fun delete(path: String): Boolean =
        with(createClient()) {
            try {
                deleteObject(bucket, path)
                true
            } catch (e: Exception) {
                false
            } finally {
                shutdown()
            }
        }
}