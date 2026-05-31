package ru.devinvader.market.integration.controller;

import com.github.tomakehurst.wiremock.WireMockServer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.BodyInserters;
import org.wiremock.spring.InjectWireMock;
import reactor.test.StepVerifier;
import ru.devinvader.market.domain.CartItem;
import ru.devinvader.market.repository.CartItemRepository;
import ru.devinvader.market.web.dto.ActionTypeDto;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers.csrf;

class ItemsControllerIntegrationTest extends IntegrationBaseTest {

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
    void getItems_shouldReturnItemsView() {
        webTestClient.get().uri("/items")
                .exchange()
                .expectStatus().isOk();
    }

    @Test
    void actOnItems_addToCart_shouldIncreaseCartItemCount() {
        // given
        long itemId = 1L;
        CartItem existing = cartItemRepository.findByCartIdAndItemId(100L, itemId)
                .defaultIfEmpty(new CartItem())
                .block();
        int initialCount = existing.getCount() != null ? existing.getCount() : 0;

        // when
        MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
        formData.add("id", String.valueOf(itemId));
        formData.add("action", ActionTypeDto.PLUS.name());

        webTestClient.mutateWith(csrf())
                .post().uri("/items")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(BodyInserters.fromFormData(formData))
                .exchange()
        // then
                .expectStatus().is3xxRedirection()
                .expectHeader().value("Location", location -> assertThat(location, startsWith("/items")));

        StepVerifier.create(cartItemRepository.findByCartIdAndItemId(100L, itemId))
                .assertNext(updated -> {
                    assertThat(updated, notNullValue());
                    assertThat(updated.getCount(), equalTo(initialCount + 1));
                })
                .verifyComplete();
    }

    @Test
    void getItem_shouldReturnItemView() {
        webTestClient.get().uri("/items/1")
                .exchange()
                .expectStatus().isOk();
    }

    @Test
    void actOnItem_removeFromCart_shouldDecreaseCartItemCount() {
        // given
        long itemId = 1L;
        CartItem existing = cartItemRepository.findByCartIdAndItemId(100L, itemId).block();
        assertThat(existing, notNullValue());
        int initialCount = existing.getCount();
        assertThat(initialCount, greaterThan(0));

        // when
        MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
        formData.add("action", ActionTypeDto.MINUS.name());

        webTestClient.mutateWith(csrf())
                .post().uri("/items/{id}", itemId)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(BodyInserters.fromFormData(formData))
                .exchange()
        // then
                .expectStatus().is3xxRedirection()
                .expectHeader().value("Location", location -> assertThat(location, is("/items/" + itemId)));

        StepVerifier.create(cartItemRepository.findByCartIdAndItemId(100L, itemId))
                .assertNext(updated ->
                        assertThat(updated.getCount(), equalTo(initialCount - 1)))
                .verifyComplete();
    }

    @Test
    void buyItems_shouldCreateOrderAndRedirect() {
        // when
        webTestClient.mutateWith(csrf())
                .post().uri("/buy")
                .exchange()
        // then
                .expectStatus().is3xxRedirection()
                .expectHeader().value("Location", location -> {
                    assertThat(location, startsWith("/orders/"));
                    assertThat(location, containsString("?newOrder=true"));
                });
    }
}