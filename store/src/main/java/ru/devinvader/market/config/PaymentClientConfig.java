package ru.devinvader.market.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class PaymentClientConfig {

    @Bean
    public WebClient paymentWebClient(
            @Value("${integration.payment-service.url:http://localhost:8081}") String paymentServiceUrl
    ) {
        return WebClient.builder()
                .baseUrl(paymentServiceUrl)
                .build();
    }
}
