package ru.devinvader.market.integration.controller;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webtestclient.autoconfigure.AutoConfigureWebTestClient;
import org.springframework.context.annotation.Import;

import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.wiremock.spring.ConfigureWireMock;
import org.wiremock.spring.EnableWireMock;
import ru.devinvader.market.TestOAuth2PaymentClientConfig;
import ru.devinvader.market.TestcontainersConfiguration;

@SpringBootTest
@Import({TestcontainersConfiguration.class, TestOAuth2PaymentClientConfig.class})
@AutoConfigureWebTestClient
@TestPropertySource(properties = {
        "spring.liquibase.change-log=classpath:/db/changelog/db.changelog-test-data.xml"
})
@ActiveProfiles("test")
@EnableWireMock({
        @ConfigureWireMock(name = "payment-service",
                baseUrlProperties = "integration.payment-service.url")
})
@WithMockMarketUser
public abstract class IntegrationBaseTest {
}
