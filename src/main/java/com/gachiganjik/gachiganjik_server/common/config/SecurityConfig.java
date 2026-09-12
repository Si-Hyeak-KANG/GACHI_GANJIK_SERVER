package com.gachiganjik.gachiganjik_server.common.config;

import com.gachiganjik.gachiganjik_server.common.security.CustomAccessDeniedHandler;
import com.gachiganjik.gachiganjik_server.common.security.CustomAuthenticationEntryPoint;
import com.gachiganjik.gachiganjik_server.common.security.JwtAuthenticationFilter;
import com.gachiganjik.gachiganjik_server.common.security.JwtProvider;
import com.gachiganjik.gachiganjik_server.common.security.UserDetailsServiceImpl;
import com.gachiganjik.gachiganjik_server.domain.guest.repository.GuestInfoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import tools.jackson.databind.ObjectMapper;

import java.util.List;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtProvider jwtProvider;
    private final UserDetailsServiceImpl userDetailsService;
    private final GuestInfoRepository guestInfoRepository;
    private final ObjectMapper objectMapper;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .exceptionHandling(exceptions -> exceptions
                        .authenticationEntryPoint(new CustomAuthenticationEntryPoint(objectMapper))
                        .accessDeniedHandler(new CustomAccessDeniedHandler(objectMapper))
                )
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.POST,
                                "/api/v1/auth/signup",
                                "/api/v1/auth/login",
                                "/api/v1/auth/token/refresh",
                                "/api/v1/auth/social/**",
                                "/api/v1/auth/link/email",
                                "/api/v1/auth/email/send",
                                "/api/v1/auth/email/verify",
                                "/api/v1/guests/register",
                                "/api/v1/guests/restore"
                        ).permitAll()
                        .requestMatchers(HttpMethod.GET,
                                "/api/v1/albums/verify"
                        ).permitAll()
                        .requestMatchers("/ws/**").permitAll()
                        // 계약상 bearerAuth만 선언되고 guestKeyAuth는 선언되지 않은 회원 전용
                        // 엔드포인트. 게스트(GuestPrincipal, 빈 authorities)는 hasRole("USER")를
                        // 통과하지 못해 CustomAccessDeniedHandler가 GUEST_NOT_ALLOWED(403)로
                        // 응답한다(ADR-020, ADR-021).
                        .requestMatchers(HttpMethod.POST,
                                "/api/v1/auth/logout",
                                "/api/v1/users/me/profile-image",
                                "/api/v1/albums",
                                "/api/v1/albums/join",
                                "/api/v1/albums/{albumId}/members/{memberId}/ownership"
                        ).hasRole("USER")
                        .requestMatchers(HttpMethod.DELETE,
                                "/api/v1/auth/withdraw",
                                "/api/v1/albums/{albumId}",
                                "/api/v1/albums/{albumId}/members/{memberId}",
                                "/api/v1/albums/{albumId}/members/me"
                        ).hasRole("USER")
                        .requestMatchers(HttpMethod.GET,
                                "/api/v1/users/me",
                                "/api/v1/albums",
                                "/api/v1/albums/{albumId}",
                                "/api/v1/albums/{albumId}/members"
                        ).hasRole("USER")
                        .requestMatchers(HttpMethod.PATCH,
                                "/api/v1/users/me",
                                "/api/v1/albums/{albumId}",
                                "/api/v1/albums/{albumId}/members/{memberId}/role"
                        ).hasRole("USER")
                        .anyRequest().authenticated()
                )
                .addFilterBefore(
                        new JwtAuthenticationFilter(jwtProvider, userDetailsService, guestInfoRepository),
                        UsernamePasswordAuthenticationFilter.class
                );

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOriginPatterns(List.of("*"));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("*"));
        config.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}