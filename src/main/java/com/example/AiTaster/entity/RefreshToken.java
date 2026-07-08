package com.example.AiTaster.entity;

import lombok.*;
import lombok.experimental.FieldDefaults;
import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;
import org.springframework.data.redis.core.TimeToLive;

import java.time.Instant;

@RedisHash("refreshToken") //Nghĩa là refresh token được lưu ở Redis, không phải database SQL.
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Builder
public class RefreshToken {

    @Id String token; // id của refreshToken

    long userId;

    Instant issueAt;

    boolean inRevoked; // token này đã bị thu hồi hay chưa

    @TimeToLive Long ttl; // Redis tự xóa token sau TTL
}
