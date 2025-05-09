package com.duri.config.websocket;

import com.duri.config.JwtConfig;
import com.duri.domain.auth.jwt.service.JwtTokenService;
import java.util.Objects;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class JwtChannelInterceptor implements ChannelInterceptor {

    private final JwtConfig jwtConfig;
    private final JwtTokenService jwtTokenService;

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message,
            StompHeaderAccessor.class);

        if (!StompCommand.CONNECT.equals(Objects.requireNonNull(accessor).getCommand())) {
            return message;
        }

        Optional<String> jwtTokenOptional = Optional.ofNullable(
            accessor.getFirstNativeHeader(jwtConfig.getHeaderAuthorization()));

        String accessToken = jwtTokenOptional
            .filter(token -> token.startsWith(jwtConfig.getAccessTokenPrefix()))
            .map(token -> token.substring(jwtConfig.getAccessTokenPrefix().length()))
            .filter(jwtTokenService::isValidAccessToken)
            .orElseThrow(() -> new RuntimeException("Invalid token"));

        Authentication authentication = jwtTokenService.getAuthentication(accessToken);
        accessor.setUser(authentication);

        return message;
    }

}
