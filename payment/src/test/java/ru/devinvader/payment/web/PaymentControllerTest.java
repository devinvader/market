package ru.devinvader.payment.web;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.reactive.server.WebTestClient;
import ru.devinvader.payment.integration.PaymentIntegrationBaseTest;
import ru.devinvader.payment.service.PaymentService;

import java.lang.reflect.Field;
import java.util.concurrent.atomic.AtomicLong;

class PaymentControllerTest extends PaymentIntegrationBaseTest {
    @Autowired
    protected WebTestClient webTestClient;
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
    void getBalance_withoutToken_returns401() {
        webTestClient.get().uri("/api/payment/balance")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isUnauthorized();
    }

    @Test
    void pay_withoutToken_returns401() {
        webTestClient.post().uri("/api/payment/pay")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("{\"amount\": 5000}")
                .exchange()
                .expectStatus().isUnauthorized();
    }

    @Test
    @WithMockUser(roles = {"BILLING"})
    void getBalance_returnsBalanceJson() {
        webTestClient
                .get().uri("/api/payment/balance")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.balance").isEqualTo(1_000_000);
    }

    @Test
    @WithMockUser(roles = {"BILLING"})
    void pay_sufficientBalance_returnsSuccess() {
        webTestClient
                .post().uri("/api/payment/pay")
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
    @WithMockUser(roles = {"BILLING"})
    void pay_insufficientBalance_returnsFailure() {
        webTestClient
                .post().uri("/api/payment/pay")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("{\"amount\": 900000}")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.success").isEqualTo(true)
                .jsonPath("$.newBalance").isEqualTo(100_000);

        webTestClient
                .post().uri("/api/payment/pay")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("{\"amount\": 999999}")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.success").isEqualTo(false)
                .jsonPath("$.newBalance").isEqualTo(100_000)
                .jsonPath("$.message").isEqualTo("Недостаточно средств на счёте");
    }

    @Test
    @WithMockUser(roles = {"BILLING"})
    void pay_zeroAmount_returnsSuccessWithSameBalance() {
        webTestClient
                .post().uri("/api/payment/pay")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("{\"amount\": 0}")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.success").isEqualTo(true)
                .jsonPath("$.newBalance").isEqualTo(1_000_000)
                .jsonPath("$.message").isEqualTo("Оплата прошла успешно");
    }

    @Test
    @WithMockUser(roles = {"BILLING"})
    void getBalance_afterPayment_showsUpdatedBalance() {
        webTestClient
                .post().uri("/api/payment/pay")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("{\"amount\": 100000}")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.success").isEqualTo(true)
                .jsonPath("$.newBalance").isEqualTo(900_000);

        webTestClient
                .get().uri("/api/payment/balance")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.balance").isEqualTo(900_000);
    }

    @Test
    @WithMockUser(roles = {"BILLING"})
    void pay_multiplePayments_accumulativelyDeductBalance() {
        webTestClient
                .post().uri("/api/payment/pay")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("{\"amount\": 100000}")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.newBalance").isEqualTo(900_000);

        webTestClient
                .post().uri("/api/payment/pay")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("{\"amount\": 200000}")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.newBalance").isEqualTo(700_000);

        webTestClient
                .post().uri("/api/payment/pay")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("{\"amount\": 300000}")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.newBalance").isEqualTo(400_000);

        webTestClient
                .get().uri("/api/payment/balance")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.balance").isEqualTo(400_000);
    }

    @Test
    @WithMockUser
    void getBalance_withUnauthorizedClient_returns403() {
        webTestClient
                .get().uri("/api/payment/balance")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isForbidden();
    }

    @Test
    @WithMockUser
    void pay_withUnauthorizedClient_returns403() {
        webTestClient
                .post().uri("/api/payment/pay")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("{\"amount\": 5000}")
                .exchange()
                .expectStatus().isForbidden();
    }

    @Test
    @WithMockUser(roles = {"BILLING"})
    void pay_exactBalance_returnsSuccessWithZeroBalance() {
        webTestClient
                .post().uri("/api/payment/pay")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("{\"amount\": 1000000}")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.success").isEqualTo(true)
                .jsonPath("$.newBalance").isEqualTo(0);

        webTestClient
                .get().uri("/api/payment/balance")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.balance").isEqualTo(0);

        webTestClient
                .post().uri("/api/payment/pay")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("{\"amount\": 1}")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.success").isEqualTo(false)
                .jsonPath("$.newBalance").isEqualTo(0)
                .jsonPath("$.message").isEqualTo("Недостаточно средств на счёте");
    }
}
