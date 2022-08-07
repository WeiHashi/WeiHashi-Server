package cn.devmeteor.weihashi

fun getEnv(key: String): String = try {
    System.getenv(key)
} catch (e: Exception) {
    ""
}