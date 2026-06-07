package com.gachiganjik.gachiganjik_server.common.security;

import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

@Getter
public class GuestPrincipal implements UserDetails {

    private final Long guestId;
    private final String guestKey;
    private final String nickname;

    public GuestPrincipal(Long guestId, String guestKey, String nickname) {
        this.guestId = guestId;
        this.guestKey = guestKey;
        this.nickname = nickname;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of();
    }

    @Override
    public String getPassword() {
        return null;
    }

    // username 에 guestId 저장 — Controller에서 getUsername()으로 guestId 추출
    @Override
    public String getUsername() {
        return "GUEST:" + guestId;
    }
}