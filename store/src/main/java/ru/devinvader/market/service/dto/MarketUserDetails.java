package ru.devinvader.market.service.dto;

import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;

import java.util.Collection;
import java.util.List;

public class MarketUserDetails extends User {
    @Getter
    private final Long userId;

    public MarketUserDetails(Long userId, String username, String password, String role) {
        super(username, password, buildAuthorities(role));
        this.userId = userId;
    }

    private static Collection<? extends GrantedAuthority> buildAuthorities(String role) {
        if (role != null && !role.startsWith("ROLE_")) {
            role = "ROLE_" + role;
        }
        return List.of(new SimpleGrantedAuthority(role != null ? role : "ROLE_USER"));
    }
}
