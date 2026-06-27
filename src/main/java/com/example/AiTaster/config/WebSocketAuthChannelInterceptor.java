package com.example.AiTaster.config;

import com.example.AiTaster.constant.ErrorCode;
import com.example.AiTaster.entity.User;
import com.example.AiTaster.exception.GlobalException;
import com.example.AiTaster.service.TokenService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
@RequiredArgsConstructor
@Slf4j
public class WebSocketAuthChannelInterceptor implements ChannelInterceptor {

    private final TokenService tokenService;

    @Override
    public Message<?> preSend(
            Message<?> message,
            MessageChannel channel
    ) {
        /*
         * Phải lấy accessor gốc của message.
         *
         * Không dùng:
         * StompHeaderAccessor.wrap(message)
         *
         * vì wrap tạo accessor bằng cách copy header và Principal có thể
         * không được lưu cho toàn bộ WebSocket session.
         */
        StompHeaderAccessor accessor =
                MessageHeaderAccessor.getAccessor(
                        message,
                        StompHeaderAccessor.class
                );

        if (accessor == null) {
            return message;
        }

        /*
         * Chỉ cần authenticate tại frame CONNECT.
         * Sau đó Spring lưu Principal cho các frame tiếp theo
         * trong cùng WebSocket session.
         */
        if (StompCommand.CONNECT.equals(accessor.getCommand())) {
            String authorization = getAuthorizationHeader(accessor);

            if (!StringUtils.hasText(authorization)
                    || !authorization.startsWith("Bearer ")) {

                log.warn(
                        "WebSocket CONNECT rejected: missing Authorization header, sessionId={}",
                        accessor.getSessionId()
                );

                throw new GlobalException(ErrorCode.INVALID_TOKEN);
            }

            String token = authorization.substring(7).trim();

            if (!StringUtils.hasText(token)) {
                throw new GlobalException(ErrorCode.INVALID_TOKEN);
            }

            User user = tokenService.verifyAccessToken(token);

            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(
                            user,
                            null,
                            user.getAuthorities()
                    );

            /*
             * Quan trọng nhất:
             * gắn Authentication vào STOMP session.
             */
            accessor.setUser(authentication);

            log.info(
                    "WebSocket authenticated successfully: sessionId={}, userId={}, username={}",
                    accessor.getSessionId(),
                    user.getUserId(),
                    user.getUsername()
            );
        }

        return message;
    }

    private String getAuthorizationHeader(
            StompHeaderAccessor accessor
    ) {
        String authorization =
                accessor.getFirstNativeHeader("Authorization");

        /*
         * Hỗ trợ cả trường hợp frontend gửi lowercase.
         */
        if (!StringUtils.hasText(authorization)) {
            authorization =
                    accessor.getFirstNativeHeader("authorization");
        }

        return authorization;
    }
}