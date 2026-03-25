package ru.devinvader.market.integration.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.test.StepVerifier;
import ru.devinvader.market.domain.CartItem;
import ru.devinvader.market.repository.CartItemRepository;
import ru.devinvader.market.web.dto.ActionTypeDto;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

class CartControllerIntegrationTest extends IntegrationBaseTest {

    @Autowired
    private WebTestClient webTestClient;

    @Autowired
    private CartItemRepository cartItemRepository;

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
        int initialCount = existing != null ? existing.getCount() : 0;

        // when
        webTestClient.post().uri(uriBuilder -> uriBuilder
                        .path("/cart/items")
                        .queryParam("id", itemId)
                        .queryParam("action", ActionTypeDto.PLUS.name())
                        .build())
                .exchange()
                .expectStatus().isOk();

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
                .expectStatus().isOk();

        // then
        StepVerifier.create(cartItemRepository.findByCartIdAndItemId(1L, itemId))
                .assertNext(updated -> {
                    assertThat(updated.getCount(), equalTo(initialCount - 1));
                })
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
                .expectStatus().isOk();

        // then
        StepVerifier.create(cartItemRepository.findByCartIdAndItemId(1L, itemId))
                .verifyComplete();
    }
}