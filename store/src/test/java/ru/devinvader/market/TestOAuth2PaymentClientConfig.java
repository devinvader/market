package ru.devinvader.market;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.security.oauth2.client.OAuth2AuthorizeRequest;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.ReactiveOAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import reactor.core.publisher.Mono;

import java.time.Instant;

@TestConfiguration
public class TestOAuth2PaymentClientConfig {

    @Bean
    @Primary
    public ReactiveOAuth2AuthorizedClientManager mockOAuth2ClientManager() {
        return authorizeRequest -> {
            var registration = ClientRegistration
                    .withRegistrationId("keycloak")
                    .authorizationGrantType(AuthorizationGrantType.CLIENT_CREDENTIALS)
                    .clientId("store")
                    .clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_BASIC)
                    .tokenUri("http://localhost/mock")
                    .build();
            var token = new OAuth2AccessToken(
                    OAuth2AccessToken.TokenType.BEARER,
                    "mock-store-token",
                    Instant.now(),
                    Instant.now().plusSeconds(3600));
            return Mono.just(new OAuth2AuthorizedClient(registration, "store", token));
        };
    }
}
