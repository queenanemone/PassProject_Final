package com.tripboard.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    public SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/auth/**", "/", "/index.html", "/login.html", "/new-plan.html",
                                "/dashboard.html", "/api/plans/recommend-destinations",
                                "/board.html", "/record.html", "/share-plan.html", "/post-detail.html",
                                "/my-records.html", "/record-detail.html", "/mypage.html",
                                "/css/**", "/js/**", "/images/**", "/assets/**", "/favicon.ico")
                        .permitAll()
                        .requestMatchers("/api/plans/search/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/board/posts").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/board/posts/*").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/board/posts/*/comments").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/hotplaces").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/hotplaces/*").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/hotplaces/*/images").permitAll()
                        .requestMatchers(HttpMethod.PUT, "/api/board/posts/*").authenticated()
                        .requestMatchers(HttpMethod.DELETE, "/api/board/posts/*").authenticated()
                        .requestMatchers(HttpMethod.PUT, "/api/board/comments/**").authenticated()
                        .requestMatchers(HttpMethod.DELETE, "/api/board/comments/**").authenticated()
                        .anyRequest().authenticated());

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration
                .setAllowedOrigins(List.of("http://localhost:8080", "http://localhost:3000", "http://localhost:5173"));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
