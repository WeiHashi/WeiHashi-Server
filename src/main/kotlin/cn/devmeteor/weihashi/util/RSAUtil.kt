package cn.devmeteor.weihashi.util

import cn.devmeteor.weihashi.getEnv
import org.apache.commons.codec.binary.Base64
import java.nio.charset.StandardCharsets
import java.security.KeyFactory
import java.security.interfaces.RSAPrivateKey
import java.security.spec.PKCS8EncodedKeySpec
import javax.crypto.Cipher

object RSAUtil {
    private val PRIVATE_KEY = getEnv("RSA_PRIVATE_KEY")

    fun decrypt(str: String): Long {
        val inputByte = Base64.decodeBase64(str.toByteArray(StandardCharsets.UTF_8))
        val decoded = Base64.decodeBase64(PRIVATE_KEY)
        val priKey = KeyFactory.getInstance("RSA").generatePrivate(PKCS8EncodedKeySpec(decoded)) as RSAPrivateKey
        val cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding")
        cipher.init(Cipher.DECRYPT_MODE, priKey)
        return String(cipher.doFinal(inputByte)).toLong()
    }
}