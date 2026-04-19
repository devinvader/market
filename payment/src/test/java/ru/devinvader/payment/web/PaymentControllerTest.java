package ru.devinvader.payment.web;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import ru.devinvader.payment.integration.PaymentIntegrationBaseTest;
import ru.devinvader.payment.service.PaymentService;

import java.lang.reflect.Field;
import java.util.concurrent.atomic.AtomicLong;

class PaymentControllerTest extends PaymentIntegrationBaseTest {

    @Autowired
    private WebTestClient webTestClient;

    @Autowired
    private PaymentService paymentService;

    @Value("${payment.initial-balance}")
    private Long initialBalance;

    @BeforeEach
    void resetBalance() throws Exception {
        Field balanceField = PaymentService.class.getDeclaredField("balance");
        balanceField.setAccessible(true);
        AtomicLong balance = (AtomicLong) balanceField.get(paymentService);
        balance.set(initialBalance);
    }

    @Test
    void getBalance_returnsBalanceJson() {
        // when
        webTestClient.get().uri("/api/payment/balance")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
        // then
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.balance").isEqualTo(1_000_000);
    }

    @Test
    void pay_sufficientBalance_returnsSuccess() {
        // when
        webTestClient.post().uri("/api/payment/pay")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("{\"amount\": 5000}")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.success").isEqualTo(true)
                .jsonPath("$.newBalance").isEqualTo(995_000)
                .jsonPath("$.message").isEqualTo("Оплата прошла успешно");
    }

    @Test
    void pay_insufficientBalance_returnsFailure() {
        // given
        webTestClient.post().uri("/api/payment/pay")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("{\"amount\": 900000}")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.success").isEqualTo(true)
                .jsonPath("$.newBalance").isEqualTo(100_000);
        // when
        webTestClient.post().uri("/api/payment/pay")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("{\"amount\": 999999}")
                .exchange()
        // then
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.success").isEqualTo(false)
                .jsonPath("$.newBalance").isEqualTo(100_000)
                .jsonPath("$.message").isEqualTo("Недостаточно средств на счёте");
    }

    @Test
    void pay_zeroAmount_returnsSuccessWithSameBalance() {
        // when
        webTestClient.post().uri("/api/payment/pay")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("{\"amount\": 0}")
                .exchange()
        // then
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.success").isEqualTo(true)
                .jsonPath("$.newBalance").isEqualTo(1_000_000)
                .jsonPath("$.message").isEqualTo("Оплата прошла успешно");
    }

    @Test
    void getBalance_afterPayment_showsUpdatedBalance() {
        // given 
        webTestClient.post().uri("/api/payment/pay")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("{\"amount\": 100000}")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.success").isEqualTo(true)
                .jsonPath("$.newBalance").isEqualTo(900_000);

        // when
        webTestClient.get().uri("/api/payment/balance")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
        // then
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.balance").isEqualTo(900_000);
    }

    @Test
    void pay_multiplePayments_accumulativelyDeductBalance() {
        // given
        webTestClient.post().uri("/api/payment/pay")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("{\"amount\": 100000}")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.newBalance").isEqualTo(900_000);
        webTestClient.post().uri("/api/payment/pay")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("{\"amount\": 200000}")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.newBalance").isEqualTo(700_000);
        webTestClient.post().uri("/api/payment/pay")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("{\"amount\": 300000}")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.newBalance").isEqualTo(400_000);

        // when
        webTestClient.get().uri("/api/payment/balance")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
        // then
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.balance").isEqualTo(400_000);
    }

    @Test
    void pay_exactBalance_returnsSuccessWithZeroBalance() {
        // given
        webTestClient.post().uri("/api/payment/pay")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("{\"amount\": 1000000}")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.success").isEqualTo(true)
                .jsonPath("$.newBalance").isEqualTo(0);

        // when
        webTestClient.get().uri("/api/payment/balance")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.balance").isEqualTo(0);

        webTestClient.post().uri("/api/payment/pay")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("{\"amount\": 1}")
                .exchange()
        // then
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.success").isEqualTo(false)
                .jsonPath("$.newBalance").isEqualTo(0)
                .jsonPath("$.message").isEqualTo("Недостаточно средств на счёте");
    }
}
