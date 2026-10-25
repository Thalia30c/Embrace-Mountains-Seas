package com.cardgame.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * 跨域配置。
 *
 * <p>小程序本身不受同源策略限制，但开发者工具的 H5 预览、以及用浏览器调接口时需要放开。</p>
 */
@Configuration
public class CorsConfig implements WebMvcConfigurer {

    @Value("${card.cors.allowed-origin-patterns:*}")
    private String allowedOriginPatterns;

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOriginPatterns(allowedOriginPatterns)
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(true)
                .maxAge(3600);
    }
}
