package ru.devinvader.payment.config;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.security.oauth2.jwt.BadJwtException;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.ReactiveJwtDecoder;
import reactor.core.publisher.Mono;

import java.time.Instant;

@TestConfiguration
public class MockJwtDecoderConfig {

    @Bean
    @Primary
    public ReactiveJwtDecoder mockJwtDecoder() {
        return token -> {
            if ("test-token".equals(token)) {
                return Mono.just(
                        Jwt.withTokenValue(token)
                                .header("alg", "none")
                                .claim("sub", "store-client")
                                .claim("aud", "payment-service")
                                .claim("scope", "openid")
                                .issuedAt(Instant.now())
                                .expiresAt(Instant.now().plusSeconds(3600))
                                .build()
                );
            } else if ("unauthorized-client-token".equals(token)) {
                return Mono.just(
                        Jwt.withTokenValue(token)
                                .header("alg", "none")
                                .claim("sub", "some-other-client")
                                .claim("aud", "payment-service")
                                .claim("scope", "openid")
                                .issuedAt(Instant.now())
                                .expiresAt(Instant.now().plusSeconds(3600))
                                .build()
                );
            }
            return Mono.error(new BadJwtException("Invalid token"));
        };
    }
}
