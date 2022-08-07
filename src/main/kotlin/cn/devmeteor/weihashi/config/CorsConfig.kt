package cn.devmeteor.weihashi.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.cors.CorsConfiguration
import org.springframework.web.cors.UrlBasedCorsConfigurationSource
import org.springframework.web.filter.CorsFilter

@Configuration
class CorsConfig {
    fun buildConfig(): CorsConfiguration {
        val configuration = CorsConfiguration()
        configuration.addAllowedHeader("*")
        configuration.addAllowedMethod("*")
        configuration.addAllowedOrigin("*")
        return configuration
    }

    @Bean
    fun corsFilter(): CorsFilter {
        val source = UrlBasedCorsConfigurationSource()
        source.registerCorsConfiguration("/**", buildConfig())
        return CorsFilter(source)
    }
}