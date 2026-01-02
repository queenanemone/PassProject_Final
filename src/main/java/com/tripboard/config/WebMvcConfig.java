package com.tripboard.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Spring Boot Web MVC 설정
 * 정적 리소스 및 CORS 설정
 */
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {
    
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 정적 리소스는 Spring Boot가 자동으로 /static 폴더에서 제공
        // favicon.ico는 없어도 404로 처리되므로 별도 설정 불필요
    }
    
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        // REST API에 대한 CORS 설정
        registry.addMapping("/api/**")
                .allowedOrigins("*")
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH")
                .allowedHeaders("*")
                .exposedHeaders("Authorization")
                .maxAge(3600);
    }
    
    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
        // SPA 라우팅을 위한 설정 - 루트 경로를 index.html로 포워딩
        registry.addViewController("/").setViewName("forward:/index.html");
    }
}

