package ru.devinvader.market.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.client.AuthorizedClientServiceReactiveOAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.client.ReactiveOAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.client.ReactiveOAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.registration.ReactiveClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.reactive.function.client.ServerOAuth2AuthorizedClientExchangeFilterFunction;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class PaymentClientConfig {

    @Bean
    public ReactiveOAuth2AuthorizedClientManager reactiveOAuth2AuthorizedClientManager(
            ReactiveClientRegistrationRepository clientRegistrationRepository,
            ReactiveOAuth2AuthorizedClientService authorizedClientService
    ) {
        return new AuthorizedClientServiceReactiveOAuth2AuthorizedClientManager(
                clientRegistrationRepository, authorizedClientService);
    }

    @Bean
    public WebClient paymentWebClient(
            @Value("${integration.payment-service.url:http://localhost:8081}") String paymentServiceUrl,
            ReactiveOAuth2AuthorizedClientManager authorizedClientManager
    ) {
        var filter = new ServerOAuth2AuthorizedClientExchangeFilterFunction(authorizedClientManager);
        filter.setDefaultClientRegistrationId("keycloak");
        return WebClient.builder()
                .baseUrl(paymentServiceUrl)
                .filter(filter)
                .build();
    }
}
