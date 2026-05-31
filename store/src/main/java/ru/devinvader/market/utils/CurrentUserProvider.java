package ru.devinvader.market.utils;

import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import ru.devinvader.market.service.dto.MarketUserDetails;

@Component
public class CurrentUserProvider {

    public Mono<Long> getCurrentUserId() {
        return ReactiveSecurityContextHolder.getContext()
                .map(SecurityContext::getAuthentication)
                .filter(auth -> auth.isAuthenticated() && !(auth instanceof AnonymousAuthenticationToken))
                .flatMap(auth -> {
                    if (auth.getPrincipal() instanceof MarketUserDetails authUser) {
                        return Mono.just(authUser.getUserId());
                    }
                    return Mono.error(new IllegalArgumentException(
                            "Unsupported principal type: " + auth.getPrincipal().getClass().getName()));
                });
    }
}
