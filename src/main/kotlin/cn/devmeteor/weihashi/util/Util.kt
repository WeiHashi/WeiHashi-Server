package cn.devmeteor.weihashi.util

import cn.devmeteor.weihashi.getEnv
import com.ndktools.javamd5.Mademd5
import org.json.JSONObject
import org.springframework.util.DigestUtils
import java.math.BigInteger
import java.text.SimpleDateFormat
import java.util.*
import javax.script.ScriptEngineManager

object Util {
    val constructHeader = hashMapOf(
        "Accept" to "text/html, application/xhtml+xml, application/xmlq=0.9,*/*;q=08,application/json; charset=utf-8",
        "User-Agent" to "Mozilla/5.0( X11;Ubuntu;Linux x86_64 ;rv:61.0) Gecko20100101Firefox/61.0",
        "Cache-Control" to "no-cache",
        "Referer" to "jwpt.hrbnu.edu.cn",
        "Host" to "jwpt.hrbnu.edu.cn",
        "Accept-Encoding" to "gzip, deflate",
        "Connection" to "keep-alive",
        "Content-Type" to "application/x-www-form-urlencoded"
    )

    fun date2String(date: Date): String = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.CHINESE).format(date)

    fun constructRes(code: Int, msg: String, data: Any? = null): String =
        JSONObject().put("code", code)
            .put("msg", msg)
            .apply {
                if (data != null) {
                    put("data", data)
                }
            }
            .toString()

    fun createRandomColor(randomColors: ArrayList<String>): String {
        var rs: String
        var gs: String
        var bs: String
        val r: Int;
        val g: Int;
        var b: Int
        val random = Random()
        r = random.nextInt(256)
        g = random.nextInt(256)
        b = random.nextInt(256)
        if (r * 0.299 + g * 0.578 + b * 0.114 < 115) return createRandomColor(randomColors)
        rs = Integer.toHexString(r).toUpperCase()
        gs = Integer.toHexString(g).toUpperCase()
        bs = Integer.toHexString(b).toUpperCase()
        rs = if (rs.length == 1) "0$rs" else rs
        gs = if (gs.length == 1) "0$gs" else gs
        bs = if (bs.length == 1) "0$bs" else bs
        val color = "#$rs$gs$bs"
        for (s in randomColors)
            if (s == color) return createRandomColor(randomColors)
        randomColors.add(color)
        return color
    }

    fun createCode(): String {
        var code = ""
        for (i in 0 until 6)
            code += Random().nextInt(10)
        return code
    }

    fun createObjId(): String = UUID.randomUUID().toString().substring(24)

    fun createUUID(): String = UUID.randomUUID().toString()

    fun createAccountId(): Map<String, String> {
        val map: MutableMap<String, String> = HashMap()
        val uuid = UUID.randomUUID().toString().replace("-", "")
        val id = StringBuilder()
        for (i in 0..7) {
            id.append(uuid[Random().nextInt(uuid.length)])
        }
        val username = BigInteger(id.toString(), 16).toString()
        val pwd = StringBuilder()
        for (i in 0..5)
            pwd.append(Random().nextInt(10).toString())
        map["id"] = id.toString()
        map["username"] = username
        map["password"] = pwd.toString()
        return map
    }

    fun getSaltedMd5(string: String): String = Mademd5().toMd5(string + getEnv("SALT"))
    fun getMd5(string: String) = DigestUtils.md5DigestAsHex(string.toByteArray())
    fun jwEncode(username: String, password: String): String {
        val account = Base64.getEncoder().encodeToString(username.toByteArray())
        val passwd = Base64.getEncoder().encodeToString(password.toByteArray())
        return "$account%%%$passwd"
    }


    fun escape(src: String): String =
        ScriptEngineManager().getEngineByExtension("js").eval("escape('$src')").toString()

    fun unescape(src: String): String =
        ScriptEngineManager().getEngineByExtension("js").eval("unescape('$src')").toString()
}