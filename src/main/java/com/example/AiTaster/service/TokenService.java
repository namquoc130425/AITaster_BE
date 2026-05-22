package com.example.AiTaster.service;

import com.example.AiTaster.entity.User;
import com.example.AiTaster.exception.GlobalException;
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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Base64;
import java.util.Date;
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

    //hàm tạo token
    @Override
    public String generateAccessToken(User user) {
        byte[] keyBytes = Base64.getDecoder().decode(SIGNING_KEY);

        JWSHeader header = new JWSHeader(JWSAlgorithm.HS512);

        JWTClaimsSet clams = new JWTClaimsSet.Builder()
                .subject(Long.toString(user.getId()))
                .issuer("NVQN")
                .audience("NVQ_CUTO")
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
            throw new GlobalException(e.getMessage());
        }
    }

    //hàm kiểm tra token

    public SignedJWT verifyAccessToken(String token) {
        return null;
    }
}
