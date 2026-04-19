package ru.devinvader.market.integration.controller;

import com.github.tomakehurst.wiremock.WireMockServer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.wiremock.spring.InjectWireMock;
import reactor.test.StepVerifier;
import ru.devinvader.market.domain.CartItem;
import ru.devinvader.market.repository.CartItemRepository;
import ru.devinvader.market.web.dto.ActionTypeDto;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.hamcrest.Matchers.equalTo;

class CartControllerIntegrationTest extends IntegrationBaseTest {

    @Autowired
    private WebTestClient webTestClient;

    @Autowired
    private CartItemRepository cartItemRepository;

    @InjectWireMock("payment-service")
    private WireMockServer wireMock;

    @BeforeEach
    void setupPaymentService() {
        wireMock.stubFor(get(urlEqualTo("/api/payment/balance"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"balance\": 1000000}")));

        wireMock.stubFor(post(urlEqualTo("/api/payment/pay"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"success\": true, \"newBalance\": 990000, \"message\": \"Оплата прошла успешно\"}")));
    }

    @Test
    void getItems_shouldReturnCartView() {
        webTestClient.get().uri("/cart/items")
                .exchange()
                .expectStatus().isOk();
    }

    @Test
    void actOnItems_increaseCount_shouldUpdateCart() {
        // given
        long itemId = 1L;
        CartItem existing = cartItemRepository.findByCartIdAndItemId(1L, itemId).block();
        assertThat(existing, notNullValue());
        int initialCount = existing.getCount();

        // when
        webTestClient.post().uri(uriBuilder -> uriBuilder
                        .path("/cart/items")
                        .queryParam("id", itemId)
                        .queryParam("action", ActionTypeDto.PLUS.name())
                        .build())
                .exchange()
                .expectStatus().is3xxRedirection();

        // then
        StepVerifier.create(cartItemRepository.findByCartIdAndItemId(1L, itemId))
                .assertNext(updated -> {
                    assertThat(updated, notNullValue());
                    assertThat(updated.getCount(), equalTo(initialCount + 1));
                })
                .verifyComplete();
    }

    @Test
    void actOnItems_decreaseCount_shouldUpdateCart() {
        // given
        long itemId = 1L;
        CartItem existing = cartItemRepository.findByCartIdAndItemId(1L, itemId).block();
        assertThat(existing, notNullValue());
        int initialCount = existing.getCount();
        assertThat(initialCount, greaterThan(0));

        // when
        webTestClient.post().uri(uriBuilder -> uriBuilder
                        .path("/cart/items")
                        .queryParam("id", itemId)
                        .queryParam("action", ActionTypeDto.MINUS.name())
                        .build())
                .exchange()
                .expectStatus().is3xxRedirection();

        // then
        StepVerifier.create(cartItemRepository.findByCartIdAndItemId(1L, itemId))
                .assertNext(updated ->
                        assertThat(updated.getCount(), equalTo(initialCount - 1)))
                .verifyComplete();
    }

    @Test
    void actOnItems_removeAll_shouldDeleteCartItem() {
        // given
        long itemId = 2L;
        CartItem existing = cartItemRepository.findByCartIdAndItemId(1L, itemId).block();
        assertThat(existing, notNullValue());

        // when
        webTestClient.post().uri(uriBuilder -> uriBuilder
                        .path("/cart/items")
                        .queryParam("id", itemId)
                        .queryParam("action", ActionTypeDto.DELETE.name())
                        .build())
                .exchange()
                .expectStatus().is3xxRedirection();

        // then
        StepVerifier.create(cartItemRepository.findByCartIdAndItemId(1L, itemId))
                .expectNextCount(0)
                .verifyComplete();
    }

    @Test
    void getItems_paymentServiceAvailable_cartPageContainsBalance() {
        // when
        webTestClient.get().uri("/cart/items")
                .exchange()
        // then
                .expectStatus().isOk()
                .expectBody(String.class)
                .value(body -> assertThat(body, containsString("Баланс:")));
    }

    @Test
    void getItems_paymentServiceUnavailable_showsWarning() {
        // given
        wireMock.stubFor(get(urlEqualTo("/api/payment/balance"))
                .willReturn(aResponse().withStatus(503)));

        // when
        webTestClient.get().uri("/cart/items")
                .exchange()
        // then
                .expectStatus().isOk()
                .expectBody(String.class)
                .value(body -> assertThat(body, containsString("Сервис платежей недоступен")));
    }

    @Test
    void buy_paymentSuccessful_redirectsToOrder() {
        // when
        webTestClient.post().uri("/buy")
                .exchange()
        // then
                .expectStatus().is3xxRedirection()
                .expectHeader().valueMatches("Location", ".*/orders/.*\\?newOrder=true");
    }

    @Test
    void buy_paymentFailed_redirectsToCartWithError() {
        // given
        wireMock.stubFor(post(urlEqualTo("/api/payment/pay"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"success\": false, \"newBalance\": 100, \"message\": \"Недостаточно средств на счёте\"}")));

        // when
        webTestClient.post().uri("/buy")
                .exchange()
        // then
                .expectStatus().is3xxRedirection()
                .expectHeader().valueMatches("Location", ".*/cart/items.*paymentError=.*");
    }

    @Test
    void buy_paymentServiceUnavailable_redirectsToCartWithError() {
        // given
        wireMock.stubFor(post(urlEqualTo("/api/payment/pay"))
                .willReturn(aResponse().withStatus(503)));

        // when
        webTestClient.post().uri("/buy")
                .exchange()
        // then
                .expectStatus().is3xxRedirection()
                .expectHeader().valueMatches("Location", ".*/cart/items.*paymentError=.*");
    }
}
