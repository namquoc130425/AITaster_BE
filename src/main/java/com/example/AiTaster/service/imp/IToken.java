package com.example.AiTaster.service.imp;

import com.example.AiTaster.entity.User;
import com.nimbusds.jwt.SignedJWT;

public interface IToken {

    String generateAccessToken(User user);


    User verifyAccessToken(String token);
}
