package com.example.AiTaster.service;

import com.example.AiTaster.constant.ErrorCode;
import com.example.AiTaster.dto.response.GoogleLoginUserInfoResponse;
import com.example.AiTaster.exception.GlobalException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class SupabaseService {

    private final JwtDecoder supabaseJwtDecoder;

    public SupabaseService(
            @Qualifier("supabaseJwtDecoder") JwtDecoder supabaseJwtDecoder
    ) {
        this.supabaseJwtDecoder = supabaseJwtDecoder;
    }

    public GoogleLoginUserInfoResponse verifyGoogleAccessToken(String accessToken) {
        try {
            Jwt jwt =
                    supabaseJwtDecoder.decode(
                            cleanBearerToken(accessToken)
                    );

            checkAuthenticatedUser(jwt);
            checkGoogleProvider(jwt);

            String email =
                    jwt.getClaimAsString("email");

            if (!StringUtils.hasText(email)) {
                throw new GlobalException(ErrorCode.SUPABASE_EMAIL_REQUIRED);
            }

            Map<String, Object> userMetadata =
                    jwt.getClaimAsMap("user_metadata");

            String fullName =
                    extractString(
                            userMetadata,
                            "full_name",
                            "name"
                    );

            String avatarUrl =
                    extractString(
                            userMetadata,
                            "avatar_url",
                            "picture"
                    );

            return GoogleLoginUserInfoResponse.builder()
                    .supabaseUserId(jwt.getSubject())
                    .email(email)
                    .fullName(fullName)
                    .avatarUrl(avatarUrl)
                    .build();

        } catch (JwtException exception) {
            log.error(
                    "Invalid Supabase token: {}",
                    exception.getMessage()
            );

            throw new GlobalException(ErrorCode.SUPABASE_TOKEN_INVALID);
        }
    }

    private void checkAuthenticatedUser(Jwt jwt) {
        List<String> audience =
                jwt.getAudience();

        if (audience == null
                || !audience.contains("authenticated")) {
            throw new GlobalException(
                    ErrorCode.SUPABASE_ACCOUNT_NOT_AUTHENTICATED
            );
        }

        String role =
                jwt.getClaimAsString("role");

        if (!"authenticated".equals(role)) {
            throw new GlobalException(
                    ErrorCode.SUPABASE_ACCOUNT_NOT_AUTHENTICATED
            );
        }
    }

    private void checkGoogleProvider(Jwt jwt) {
        Map<String, Object> appMetadata =
                jwt.getClaimAsMap("app_metadata");

        if (appMetadata == null) {
            throw new GlobalException(
                    ErrorCode.SUPABASE_GOOGLE_PROVIDER_REQUIRED
            );
        }

        Object providerObj =
                appMetadata.get("provider");

        Object providersObj =
                appMetadata.get("providers");

        boolean isGoogleProvider =
                "google".equalsIgnoreCase(
                        String.valueOf(providerObj)
                );

        if (providersObj instanceof List<?> providers) {
            isGoogleProvider =
                    isGoogleProvider
                            || providers
                            .stream()
                            .anyMatch(provider ->
                                    "google".equalsIgnoreCase(
                                            String.valueOf(provider)
                                    )
                            );
        }

        if (!isGoogleProvider) {
            throw new GlobalException(
                    ErrorCode.SUPABASE_GOOGLE_PROVIDER_REQUIRED
            );
        }
    }

    private String cleanBearerToken(String accessToken) {
        if (!StringUtils.hasText(accessToken)) {
            throw new GlobalException(
                    ErrorCode.SUPABASE_TOKEN_INVALID
            );
        }

        String token =
                accessToken.trim();

        if (token.startsWith("Bearer ")) {
            return token
                    .substring(7)
                    .trim();
        }

        return token;
    }

    private String extractString(
            Map<String, Object> map,
            String... keys
    ) {
        if (map == null || keys == null) {
            return null;
        }

        for (String key : keys) {
            Object value =
                    map.get(key);

            if (value != null
                    && StringUtils.hasText(String.valueOf(value))) {
                return String.valueOf(value);
            }
        }

        return null;
    }
}