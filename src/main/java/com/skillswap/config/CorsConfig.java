package com.skillswap.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;

@Configuration
public class CorsConfig {

    @Bean
    public CorsConfiguration frontendCorsConfiguration(
            @Value("${app.cors.frontend-url}") String frontendUrl) {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(List.of(frontendUrl.trim()));
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("Content-Type", "Authorization"));
        configuration.setAllowCredentials(false);
        configuration.setMaxAge(3600L);
        return configuration;
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource(CorsConfiguration frontendCorsConfiguration) {
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", frontendCorsConfiguration);
        return source;
    }

    @Bean
    public WebMvcConfigurer corsMvcConfigurer(CorsConfiguration frontendCorsConfiguration) {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/**")
                        .allowedOrigins(frontendCorsConfiguration.getAllowedOrigins().toArray(String[]::new))
                        .allowedMethods(frontendCorsConfiguration.getAllowedMethods().toArray(String[]::new))
                        .allowedHeaders(frontendCorsConfiguration.getAllowedHeaders().toArray(String[]::new))
                        .allowCredentials(Boolean.TRUE.equals(frontendCorsConfiguration.getAllowCredentials()))
                        .maxAge(frontendCorsConfiguration.getMaxAge());
            }
        };
    }
}
