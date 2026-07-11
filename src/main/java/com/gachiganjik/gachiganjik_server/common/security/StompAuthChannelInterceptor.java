package com.gachiganjik.gachiganjik_server.common.security;

import com.gachiganjik.gachiganjik_server.domain.guest.entity.GuestStatus;
import com.gachiganjik.gachiganjik_server.domain.guest.repository.GuestInfoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.security.Principal;

@Slf4j
@Component
@RequiredArgsConstructor
public class StompAuthChannelInterceptor implements ChannelInterceptor {

    private final JwtProvider jwtProvider;
    private final UserDetailsServiceImpl userDetailsService;
    private final GuestInfoRepository guestInfoRepository;

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor =
                MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

        if (accessor == null || accessor.getCommand() != StompCommand.CONNECT) {
            return message;
        }

        Principal principal = resolveAuth(accessor);
        if (principal == null) {
            log.warn("[WS] STOMP CONNECT rejected — no valid auth header");
            throw new IllegalArgumentException("WebSocket 인증 실패: 유효한 토큰이 없습니다.");
        }

        accessor.setUser(principal);
        return message;
    }

    private Principal resolveAuth(StompHeaderAccessor accessor) {
        // 1. JWT Bearer Token
        String authHeader = accessor.getFirstNativeHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            try {
                jwtProvider.validate(token);
                Long userId = jwtProvider.getUserId(token);
                UserDetails userDetails = userDetailsService.loadUserByUsername(String.valueOf(userId));
                return new UsernamePasswordAuthenticationToken(
                        userDetails, null, userDetails.getAuthorities());
            } catch (Exception e) {
                log.warn("[WS] JWT validation failed: {}", e.getMessage());
            }
        }

        // 2. Guest Key
        String guestKey = accessor.getFirstNativeHeader("X-Guest-Key");
        if (guestKey != null && !guestKey.isBlank()) {
            return guestInfoRepository.findByGuestKeyAndStatus(guestKey, GuestStatus.ACTIVE)
                    .map(guest -> new GuestPrincipal(guest.getGuestId(), guest.getGuestKey(), guest.getNickname()))
                    .orElse(null);
        }

        return null;
    }
}