package com.example.AiTaster.config;

import com.example.AiTaster.Security.Filter;
import com.example.AiTaster.dto.response.APIResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
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
import org.springframework.web.cors.CorsConfigurationSource;

import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
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
    ObjectMapper objectMapper;
    private static final String[] SWAGGER = {
            "/swagger-ui.html",
            "/swagger-ui/**",
            "/v3/api-docs",
            "/v3/api-docs/**",
            "/v3/api-docs.yaml",
            "/webjars/**"
    };

    private static final String [] ADMIN = {

    };


    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }


    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception {
        return configuration.getAuthenticationManager();
    }



    // Filter thứ 2 .
    // Cấu hình rule phân quyền: API nào public, API nào cần login, API nào cần role.
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http, CorsConfigurationSource corsConfigurationSource) throws Exception {
        List<String> publicEndpoints = securityProperties.getPublicEndpoints();
        log.info(publicEndpoints.toString());

        http
                .csrf(AbstractHttpConfigurer::disable)
                .cors(cors -> cors.configurationSource(corsConfigurationSource))
                .exceptionHandling(exception -> exception
                        .authenticationEntryPoint((request, response, authException) ->
                                writeJsonError(response, HttpStatus.UNAUTHORIZED, "You need to log in to perform this action")
                        )
                        .accessDeniedHandler((request, response, accessDeniedException) ->
                                writeJsonError(response, HttpStatus.FORBIDDEN, "You do not have permission to perform this action")
                        )
                )
                .addFilterBefore(filter, UsernamePasswordAuthenticationFilter.class)   // chạy trước kiểm tra token , lấy user , set vào Authentication vào SecurityContext
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

    private void writeJsonError(
            HttpServletResponse response,
            HttpStatus status,
            String message
    ) throws IOException {
        response.setStatus(status.value());
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.getWriter().write(
                objectMapper.writeValueAsString(
                        APIResponse.response(status.value(), message, null)
                )
        );
    }
}
