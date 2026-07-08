package com.example.AiTaster.service;

import com.example.AiTaster.entity.RefreshToken;
import com.example.AiTaster.entity.User;
import com.example.AiTaster.repository.RefreshTokenRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class RefreshTokenService {

    final RefreshTokenRepository refreshTokenRepository;
    final AuthenticationService authenticationService;

    @Value("30")
    long DURATION;

    // createRefreshToken(): tạo token ngẫu nhiên và lưu Redis.
    public RefreshToken createRefreshToken(User user) {
        RefreshToken refreshTokenEntity = new RefreshToken();

        refreshTokenEntity.setToken(UUID.randomUUID() + UUID.randomUUID().toString());
        refreshTokenEntity.setUserId(user.getUserId());
        refreshTokenEntity.setIssueAt(Instant.now());
        refreshTokenEntity.setInRevoked(false);
        refreshTokenEntity.setTtl(DURATION * 24 * 60 * 60);

        return refreshTokenRepository.save(refreshTokenEntity);

    }

    //verifyToken(): token phải tồn tại trong Redis và chưa bị revoke
    public Optional<RefreshToken> verifyToken(String token) {
        return refreshTokenRepository.findById(token)
                .filter(refreshToken -> !refreshToken.isInRevoked());
    }

    //revokeToken(): logout hoặc rotate thì set isRevoked = true;
    public void revokeToken(String token) {
        refreshTokenRepository.findById(token).ifPresent(rt -> {
            rt.setInRevoked(true);
            refreshTokenRepository.save(rt);

        });
    }


    public void revokeAllTokens(Long userId) {
        List<RefreshToken> tokens = refreshTokenRepository.findAllByUserId(userId);
        tokens.forEach(rt -> rt.setInRevoked(true));
        refreshTokenRepository.saveAll(tokens);
    }


    //rotateToken(): mỗi lần refresh thành công, thì token cũ bị hủy, token mới được cấp
    public RefreshToken rotateToken(String oldToken, User user) {

        refreshTokenRepository.findById(oldToken).ifPresent(rt -> {

            rt.setInRevoked(true);
            refreshTokenRepository.save(rt);
        });
        return createRefreshToken(user);
    }

}
