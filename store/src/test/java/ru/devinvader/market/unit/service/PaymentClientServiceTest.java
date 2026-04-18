package ru.devinvader.market.unit.service;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.reactive.function.client.WebClient;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import reactor.test.StepVerifier;
import ru.devinvader.market.service.PaymentClientService;
import ru.devinvader.market.utils.ClientConstants;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.junit.jupiter.api.Assertions.*;

class PaymentClientServiceTest {

    private WireMockServer wireMock;
    private PaymentClientService paymentClientService;

    @BeforeEach
    void setUp() {
        wireMock = new WireMockServer(WireMockConfiguration.options().dynamicPort());
        wireMock.start();
        WebClient webClient = WebClient.builder()
                .baseUrl("http://localhost:" + wireMock.port())
                .build();
        paymentClientService = new PaymentClientService(webClient);
    }

    @AfterEach
    void tearDown() {
        wireMock.stop();
    }

    @Test
    void getBalance_returnsBalanceFromApi() {
        wireMock.stubFor(get(urlEqualTo("/api/payment/balance"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"balance\": 500000}")));

        StepVerifier.create(paymentClientService.getBalance())
                .expectNext(500_000L)
                .verifyComplete();
    }

    @Test
    void getBalance_whenApiUnavailable_returnsServiceUnavailableBalance() {
        wireMock.stubFor(get(urlEqualTo("/api/payment/balance"))
                .willReturn(aResponse().withStatus(503)));

        StepVerifier.create(paymentClientService.getBalance())
                .expectNext(ClientConstants.BALANCE_SERVICE_UNAVAILABLE)
                .verifyComplete();
    }

    @Test
    void isServiceAvailable_whenApiAvailable_returnsTrue() {
        wireMock.stubFor(get(urlEqualTo("/api/payment/balance"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"balance\": 100}")));

        StepVerifier.create(paymentClientService.isServiceAvailable())
                .expectNext(true)
                .verifyComplete();
    }

    @Test
    void isServiceAvailable_whenApiUnavailable_returnsFalse() {
        wireMock.stubFor(get(urlEqualTo("/api/payment/balance"))
                .willReturn(aResponse().withStatus(503)));

        StepVerifier.create(paymentClientService.isServiceAvailable())
                .expectNext(false)
                .verifyComplete();
    }

    @Test
    void pay_successfulPayment_returnsSuccessResponse() {
        wireMock.stubFor(post(urlEqualTo("/api/payment/pay"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"success\": true, \"newBalance\": 495000, \"message\": \"Оплата прошла успешно\"}")));

        StepVerifier.create(paymentClientService.pay(5000L))
                .assertNext(response -> {
                    assertTrue(response.success());
                    assertEquals(495_000L, response.newBalance());
                })
                .verifyComplete();
    }

    @Test
    void pay_insufficientBalance_returnsFailureResponse() {
        wireMock.stubFor(post(urlEqualTo("/api/payment/pay"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"success\": false, \"newBalance\": 1000, \"message\": \"Недостаточно средств на счёте\"}")));

        StepVerifier.create(paymentClientService.pay(9_999_999L))
                .assertNext(response -> assertFalse(response.success()))
                .verifyComplete();
    }

    @Test
    void pay_whenApiUnavailable_returnsFallbackFailureResponse() {
        wireMock.stubFor(post(urlEqualTo("/api/payment/pay"))
                .willReturn(aResponse().withStatus(503)));

        StepVerifier.create(paymentClientService.pay(5000L))
                .assertNext(response -> {
                    assertFalse(response.success());
                    assertEquals(ClientConstants.BALANCE_SERVICE_UNAVAILABLE, response.newBalance());
                    assertNotNull(response.message());
                })
                .verifyComplete();
    }
}
