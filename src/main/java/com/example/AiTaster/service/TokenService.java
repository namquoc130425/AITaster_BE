package com.example.AiTaster.service;

import com.example.AiTaster.constant.ErrorCode;
import com.example.AiTaster.entity.User;
import com.example.AiTaster.exception.GlobalException;
import com.example.AiTaster.repository.UserRepo;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.JWSObject;
import com.nimbusds.jose.Payload;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import lombok.experimental.NonFinal;
import lombok.extern.slf4j.Slf4j;
import com.example.AiTaster.service.imp.IToken;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.security.oauth2.jwt.JwtValidationException;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Base64;
import java.util.Date;
import java.util.Locale;
import java.util.UUID;

@Service
@Slf4j
public class TokenService implements IToken {
    @Value("${jwt.signing-key}")
    @NonFinal
    private String SIGNING_KEY;
    @Value("${jwt.valid-duration}")
    @NonFinal
    private int VALID_DURATION;
    @Autowired
    JwtDecoder jwtDecoder;
    @Autowired
    UserRepo userRepo;

    //hàm tạo token
    @Override
    public String generateAccessToken(User user) {
        byte[] keyBytes = Base64.getDecoder().decode(SIGNING_KEY);

        JWSHeader header = new JWSHeader(JWSAlgorithm.HS512); // header

        JWTClaimsSet clams = new JWTClaimsSet.Builder()  // clmas
                .subject(Long.toString(user.getUserId()))
                .issuer("AITasker")
                .audience("AITasker")
                .issueTime((new Date()))
                .expirationTime(new Date(Instant.now().plus(VALID_DURATION, ChronoUnit.SECONDS).toEpochMilli()))
                .jwtID(UUID.randomUUID().toString())
                .claim("scope", "ROLE_"+ user.getRole().name())
                .build();

        JWSObject jwsObject = new JWSObject(header,new Payload(clams.toJSONObject()));
        try {
            jwsObject.sign(new MACSigner(keyBytes));
            return jwsObject.serialize();
        }catch (Exception e) {
            log.error(e.getMessage());
            throw new GlobalException("Không thể tạo token");
        }
    }
    //hàm kiểm tra token
    public User verifyAccessToken(String token) {
        //DECODE token..
        //check chũ ký ...
        //check hết hạn...
        // lấy subject -> userId
        //tìm user dưới Database
        try{
            //1 Decode
            Jwt jwt = jwtDecoder.decode(token);
            String subject = jwt.getSubject();
            if(subject == null){
                throw new GlobalException(
                        ErrorCode.INVALID_TOKEN.getCode(),
                        ErrorCode.INVALID_TOKEN.getMessage()
                );
            }
            long userId = Long.parseLong(subject);
            return userRepo.findById(userId).orElseThrow(() -> new GlobalException(
                    ErrorCode.INVALID_TOKEN.getCode(),
                    ErrorCode.INVALID_TOKEN.getMessage()
            ));

        } catch (JwtValidationException e) {
            if (isExpiredJwt(e)) {
                log.warn("Access token expired");
                throw new GlobalException(
                        ErrorCode.ACCESS_TOKEN_EXPIRED.getCode(),
                        ErrorCode.ACCESS_TOKEN_EXPIRED.getMessage()
                );
            }
            log.warn("Invalid JWT validation: {}", e.getMessage());
            throw new GlobalException(
                    ErrorCode.INVALID_TOKEN.getCode(),
                    ErrorCode.INVALID_TOKEN.getMessage()
            );
        } catch (JwtException e) {
            log.warn("Invalid JWT: {}", e.getMessage());
            throw new GlobalException(
                    ErrorCode.INVALID_TOKEN.getCode(),
                    ErrorCode.INVALID_TOKEN.getMessage()
            );
        } catch (NumberFormatException e) {
            log.warn("Invalid JWT subject: {}", e.getMessage());
            throw new GlobalException(
                    ErrorCode.INVALID_TOKEN.getCode(),
                    ErrorCode.INVALID_TOKEN.getMessage()
            );
        }

    }

        private boolean isExpiredJwt(JwtValidationException exception) {
            return exception.getErrors().stream()
                    .anyMatch(error -> { //anyMatch: chỉ cần có 1 lỗi thỏa điều kiện thì trả về true.
                        String description = error.getDescription();
                        return description != null && description.toLowerCase(Locale.ROOT).contains("expired");
                    });
    }
}
