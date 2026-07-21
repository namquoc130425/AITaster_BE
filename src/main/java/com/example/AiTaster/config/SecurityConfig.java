package com.example.AiTaster.config;

import com.example.AiTaster.Security.Filter;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import java.util.List;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE,makeFinal = true)
@Slf4j
public class SecurityConfig {
    SecurityProperties securityProperties;

    Filter filter;
    private static final String[] SWAGGER = {
            "/swagger-ui.html",
            "/swagger-ui/**",
            "/v3/api-docs",
            "/v3/api-docs/**",
            "/v3/api-docs.yaml",
            "/webjars/**"
    };

    @Configuration
    @SecurityScheme(
            name = "api",
            type = SecuritySchemeType.HTTP,
            scheme = "bearer",
            bearerFormat = "JWT"
    )
    public class OpenApiConfig {
    }


    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }


    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception {
        return configuration.getAuthenticationManager();
    }




    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        List<String> publicEndpoints = securityProperties.getPublicEndpoints();
        log.info(publicEndpoints.toString());

        http
                .csrf(AbstractHttpConfigurer::disable)
                .addFilterBefore(filter, UsernamePasswordAuthenticationFilter.class)
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(SWAGGER).permitAll()
                        .requestMatchers(HttpMethod.GET, "/invoice-email-payment-test.html").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/local-test/invoice-email/**").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/webhooks/sepay", "/api/webhooks/sepay/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/category", "/api/category/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/skill", "/api/skill/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/expert-profiles/public/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/expert-Service/public/**").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/expert-Service/public/filter").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/ratings/filter").permitAll()
                        .requestMatchers(ADMIN).hasRole("ADMIN")
                        .requestMatchers(publicEndpoints.toArray(new String[0])).permitAll() // chuyển qua mảng tring
                        .anyRequest().authenticated()
                )

                .httpBasic(AbstractHttpConfigurer::disable);


        return http.build();
    }
}
