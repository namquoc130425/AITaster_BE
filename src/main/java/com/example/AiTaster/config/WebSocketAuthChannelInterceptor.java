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

import java.security.Principal;

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
        StompHeaderAccessor accessor =
                MessageHeaderAccessor.getAccessor(
                        message,
                        StompHeaderAccessor.class
                );

        if (accessor == null) {
            return message;
        }

        if (StompCommand.CONNECT.equals(accessor.getCommand())) {
            authenticateConnect(accessor);
        }

        if (StompCommand.SUBSCRIBE.equals(accessor.getCommand())) {
            guardPrivateTopicSubscribe(accessor);
        }

        return message;
    }

    private void authenticateConnect(
            StompHeaderAccessor accessor
    ) {
        String authorization =
                getAuthorizationHeader(accessor);

        if (!StringUtils.hasText(authorization)
                || !authorization.startsWith("Bearer ")) {
            log.warn(
                    "WebSocket CONNECT rejected: missing token, sessionId={}",
                    accessor.getSessionId()
            );

            throw new GlobalException(ErrorCode.INVALID_TOKEN);
        }

        String token =
                authorization.substring(7).trim();

        if (!StringUtils.hasText(token)) {
            throw new GlobalException(ErrorCode.INVALID_TOKEN);
        }

        User user =
                tokenService.verifyAccessToken(token);

        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(
                        user,
                        null,
                        user.getAuthorities()
                );

        /*
         * Quan trọng:
         * Spring dùng Principal.getName() cho convertAndSendToUser.
         * Với UserDetails, getName() thường là username.
         */
        accessor.setUser(authentication);

        log.info(
                "WebSocket authenticated: sessionId={}, userId={}, username={}",
                accessor.getSessionId(),
                user.getUserId(),
                user.getUsername()
        );
    }

    /*
     * Nếu FE chuyển hẳn sang /user/queue/notifications
     * thì đoạn chặn này ít dùng.
     *
     * Nhưng giữ lại để chặn các topic cũ:
     * /topic/users/{userId}/notifications
     * /topic/users/{userId}/messages
     */
    private void guardPrivateTopicSubscribe(
            StompHeaderAccessor accessor
    ) {
        String destination =
                accessor.getDestination();

        if (!StringUtils.hasText(destination)) {
            return;
        }

        boolean isPrivateUserTopic =
                destination.startsWith("/topic/users/")
                        && (
                        destination.endsWith("/notifications")
                                || destination.endsWith("/messages")
                );

        if (!isPrivateUserTopic) {
            return;
        }

        Principal principal =
                accessor.getUser();

        if (!(principal instanceof UsernamePasswordAuthenticationToken authentication)
                || !(authentication.getPrincipal() instanceof User currentUser)) {
            throw new GlobalException(ErrorCode.INVALID_TOKEN);
        }

        String expectedPrefix =
                "/topic/users/" + currentUser.getUserId() + "/";

        if (!destination.startsWith(expectedPrefix)) {
            log.warn(
                    "Blocked illegal subscribe: userId={}, destination={}",
                    currentUser.getUserId(),
                    destination
            );

            throw new GlobalException(ErrorCode.INVALID_ROLE);
        }
    }

    private String getAuthorizationHeader(
            StompHeaderAccessor accessor
    ) {
        String authorization =
                accessor.getFirstNativeHeader("Authorization");

        if (!StringUtils.hasText(authorization)) {
            authorization =
                    accessor.getFirstNativeHeader("authorization");
        }

        return authorization;
    }
}
