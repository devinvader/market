package ru.devinvader.payment.integration;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webtestclient.autoconfigure.AutoConfigureWebTestClient;
import org.springframework.test.context.TestPropertySource;

/**
 * Базовый класс для интеграционных тестов payment-сервиса.
 * Запускает полный Spring Boot контекст с реальным PaymentService.
 */
@SpringBootTest
@AutoConfigureWebTestClient
@TestPropertySource(properties = {
        "payment.initial-balance=1000000"
})
public abstract class PaymentIntegrationBaseTest {
}
