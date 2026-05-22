package com.example.AiTaster.service.imp;

import com.example.AiTaster.entity.User;
import com.nimbusds.jwt.SignedJWT;

public interface IToken {

    String generateAccessToken(User user);


    SignedJWT verifyAccessToken(String token);
}
