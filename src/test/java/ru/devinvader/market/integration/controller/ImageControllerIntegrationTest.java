package ru.devinvader.market.integration.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import ru.devinvader.market.domain.Image;
import ru.devinvader.market.repository.ImageRepository;
import ru.devinvader.market.repository.ItemRepository;

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

        Image image = imageRepository.findByItemId(itemId).block();
        // when
        webTestClient.get().uri("/items/{id}/image", itemId)
                .exchange()
        // then
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.IMAGE_GIF)
                .expectHeader().exists("Content-Length")
                .expectBody(ByteArrayResource.class)
                .consumeWith(result -> {
                    byte[] body = result.getResponseBodyContent();
                    assertThat(body, notNullValue());
                    assertThat(body.length, greaterThan(0));
                    assertThat(body, equalTo(image.getData()));
                });
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