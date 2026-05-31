package ru.devinvader.market.integration.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.reactive.server.WebTestClient;

class OrderControllerIntegrationTest extends IntegrationBaseTest {

    @Autowired
    private WebTestClient webTestClient;

    @Test
    void getOrders_shouldReturnOrdersView() {
        webTestClient.get().uri("/orders")
                .exchange()
                .expectStatus().isOk();
    }

    @Test
    void getOrder_shouldReturnOrderView() {
        webTestClient.get().uri("/orders/100")
                .exchange()
                .expectStatus().isOk();
    }

    @Test
    void getOrder_withNewOrderFlag_shouldReturnOrderViewWithNewOrderFlag() {
        webTestClient.get().uri("/orders/100?newOrder=true")
                .exchange()
                .expectStatus().isOk();
    }

    @Test
    @WithMockMarketUser(userId = 101)
    void getOrder_ofAnotherUser_shouldReturnError() {
        webTestClient.get().uri("/orders/100")
                .exchange()
                .expectStatus().isForbidden();
    }
}
