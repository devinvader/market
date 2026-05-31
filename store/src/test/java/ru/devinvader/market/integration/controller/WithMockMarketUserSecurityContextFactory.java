package ru.devinvader.market.integration.controller;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextImpl;
import org.springframework.security.test.context.support.WithSecurityContextFactory;
import ru.devinvader.market.service.dto.MarketUserDetails;

public class WithMockMarketUserSecurityContextFactory
        implements WithSecurityContextFactory<WithMockMarketUser> {

    @Override
    public SecurityContext createSecurityContext(WithMockMarketUser annotation) {
        MarketUserDetails principal = new MarketUserDetails(
                annotation.userId(),
                annotation.username(),
                "password",
                annotation.role()
        );
        var auth = new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities());
        return new SecurityContextImpl(auth);
    }
}
