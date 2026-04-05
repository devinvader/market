package ru.devinvader.market.integration.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.test.web.reactive.server.ExchangeResult;
import reactor.test.StepVerifier;
import ru.devinvader.market.repository.ImageRepository;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

class ImageControllerIntegrationTest extends IntegrationBaseTest {

    @Autowired
    private WebTestClient webTestClient;

    @Autowired
    private ImageRepository imageRepository;

    @Test
    void getImage_shouldReturnImage() {
        // given
        long itemId = 1L;

        // when
        ExchangeResult result = webTestClient.get().uri("/items/{id}/image", itemId)
                .exchange()
        // then
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.IMAGE_GIF)
                .expectHeader().exists("Content-Length")
                .expectBody()
                .consumeWith(entityResult -> {
                    byte[] body = entityResult.getResponseBodyContent();
                    assertThat(body, notNullValue());
                    assertThat(body.length, greaterThan(0));
                })
                .returnResult();

        byte[] responseBody = result.getResponseBodyContent();
        assertThat(responseBody, notNullValue());

        StepVerifier.create(imageRepository.findByItemId(itemId))
                .assertNext(image -> assertThat(responseBody, equalTo(image.getData())))
                .verifyComplete();
    }

    @Test
    void getImage_notFound_shouldReturn404() {
        // given
        long nonExistentItemId = 999L;
        // when
        webTestClient.get().uri("/items/{id}/image", nonExistentItemId)
        // then
                .exchange()
                .expectStatus().isNotFound();
    }
}