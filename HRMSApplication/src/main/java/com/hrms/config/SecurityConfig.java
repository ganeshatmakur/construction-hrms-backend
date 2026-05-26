package com.hrms.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

 
@Slf4j
@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    @Value("${app.cors.allowed-origins}")
    private String allowedOrigins;

    @Value("${app.cors.allowed-methods}")
    private String allowedMethods;

    @Value("${app.cors.allowed-headers}")
    private String allowedHeaders;

    @Value("${app.cors.allow-credentials:true}")
    private boolean allowCredentials;

    @Value("${app.cors.max-age:3600}")
    private long maxAge;
 
    @Bean
      CorsConfigurationSource corsConfigurationSource() {
        log.info("[v0] Configuring CORS: origins={}, methods={}, headers={}", 
            allowedOrigins, allowedMethods, allowedHeaders);

        CorsConfiguration configuration = new CorsConfiguration();
        
        // Parse allowed origins from environment variable
        List<String> origins = Arrays.asList(allowedOrigins.split(","));
        configuration.setAllowedOrigins(origins);
        
        // Parse allowed methods
        List<String> methods = Arrays.asList(allowedMethods.split(","));
        configuration.setAllowedMethods(methods);
        
        // Parse allowed headers
        List<String> headers = Arrays.asList(allowedHeaders.split(","));
        configuration.setAllowedHeaders(headers);
        
        // Allow credentials (cookies, auth headers)
        configuration.setAllowCredentials(allowCredentials);
        
        // Cache preflight requests
        configuration.setMaxAge(maxAge);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        
        return source;
    }

     
    @Bean
      SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            // Enable CORS with bean above
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            
            // Disable CSRF for simplicity (can be enabled with token-based approach)
            .csrf(csrf -> csrf.disable())
            
            // Use stateless session (JWT or OAuth2 token based)
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            
            // Configure authorization
            .authorizeHttpRequests(authorize -> authorize
                // Public endpoints (no auth required)
                .requestMatchers("/api/attendance/**").permitAll()
                .requestMatchers("/api/overtime/**").permitAll()
                .requestMatchers("/actuator/health").permitAll()
                
                // All other requests require authentication (can be enhanced with @PreAuthorize)
                .anyRequest().authenticated()
            );

        return http.build();
    }
}
