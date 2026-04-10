package ru.devinvader.market.integration.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.web.reactive.function.BodyInserters;
import reactor.test.StepVerifier;
import ru.devinvader.market.domain.Item;
import ru.devinvader.market.repository.ItemRepository;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

class AdminControllerIntegrationTest extends IntegrationBaseTest {

    @Autowired
    private WebTestClient webTestClient;

    @Autowired
    private ItemRepository itemRepository;

    @Test
    void getAdminPage_shouldReturnAdminViewWithItems() {
        webTestClient.get().uri("/admin")
                .exchange()
                .expectStatus().isOk();
    }

    @Test
    void showAddItemForm_shouldReturnAddItemView() {
        webTestClient.get().uri("/admin/items/new")
                .exchange()
                .expectStatus().isOk();
    }

    @Test
    void addItem_shouldSaveItemAndRedirect() {
        // given
        String title = "New Test Item";
        String description = "Test Description";
        Long price = 5000L;

        long initialCount = itemRepository.count().block();

        LinkedMultiValueMap<String, String> multipartData = new LinkedMultiValueMap<>();
        multipartData.add("title", title);
        multipartData.add("description", description);
        multipartData.add("price", price.toString());

        // when
        webTestClient.post().uri("/admin/items")
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .body(BodyInserters.fromMultipartData(multipartData))
                .exchange()
                .expectStatus().is3xxRedirection()
                .expectHeader().location("/admin");

        // then
        StepVerifier.create(itemRepository.count())
                .assertNext(finalCount -> assertThat(finalCount, equalTo(initialCount + 1)))
                .verifyComplete();

        StepVerifier.create(itemRepository.findAll()
                        .filter(item -> title.equals(item.getTitle()))
                        .collectList())
                .assertNext(items -> {
                    assertThat(items.size(), is(1));
                    Item savedItem = items.get(0);
                    assertThat(savedItem.getDescription(), equalTo(description));
                    assertThat(savedItem.getPrice(), equalTo(price));
                })
                .verifyComplete();
    }

    @Test
    void addItemWithImage_shouldSaveItemAndImage() {
        // given
        String title = "Item with Image";
        String description = "Desc";
        long price = 3000L;
        MockMultipartFile imageFile = new MockMultipartFile(
                "imageFile",
                "test.jpg",
                "image/jpeg",
                "fake image content".getBytes()
        );

        LinkedMultiValueMap<String, Object> multipartData = new LinkedMultiValueMap<>();
        multipartData.add("imageFile", imageFile.getResource());
        multipartData.add("title", title);
        multipartData.add("description", description);
        multipartData.add("price", String.valueOf(price));

        // when
        webTestClient.post().uri("/admin/items")
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .body(BodyInserters.fromMultipartData(multipartData))
                .exchange()
                .expectStatus().is3xxRedirection()
                .expectHeader().location("/admin");

        // then
        StepVerifier.create(itemRepository.findAll()
                        .filter(item -> title.equals(item.getTitle()))
                        .collectList())
                .assertNext(items -> assertThat(items.isEmpty(), is(false)))
                .verifyComplete();
    }

    @Test
    void showEditItemForm_shouldReturnEditItemView() {
        // given
        Item item = new Item();
        item.setTitle("Test Item");
        item.setDescription("Test Description");
        item.setPrice(1000L);

        Item saved = itemRepository.save(item).block();

        // when
        webTestClient.get().uri("/admin/items/{id}/edit", saved.getId())
                .exchange()
        // then
                .expectStatus().isOk();

        StepVerifier.create(itemRepository.findById(saved.getId()))
                .assertNext(found -> {
                    assertThat(found.getTitle(), equalTo(item.getTitle()));
                    assertThat(found.getDescription(), equalTo(item.getDescription()));
                    assertThat(found.getPrice(), equalTo(item.getPrice()));
                })
                .verifyComplete();
    }

    @Test
    void updateItem_shouldUpdateItemAndRedirect() {
        // given
        Item item = new Item();
        item.setTitle("Original Title");
        item.setDescription("Original Description");
        item.setPrice(1000L);

        Item saved = itemRepository.save(item).block();

        String newTitle = "Updated Title";
        String newDescription = "Updated Description";
        Long newPrice = 9999L;

        LinkedMultiValueMap<String, String> multipartData = new LinkedMultiValueMap<>();
        multipartData.add("title", newTitle);
        multipartData.add("description", newDescription);
        multipartData.add("price", newPrice.toString());
        // when
        webTestClient.post().uri("/admin/items/{id}", saved.getId())
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .body(BodyInserters.fromMultipartData(multipartData))
                .exchange()
        // then
                .expectStatus().is3xxRedirection()
                .expectHeader().location("/admin");

        StepVerifier.create(itemRepository.findById(saved.getId()))
                .assertNext(updated -> {
                    assertThat(updated.getTitle(), equalTo(newTitle));
                    assertThat(updated.getDescription(), equalTo(newDescription));
                    assertThat(updated.getPrice(), equalTo(newPrice));
                })
                .verifyComplete();
    }

    @Test
    void deleteItem_shouldDeleteItemAndRedirect() {
        // given
        Item item = new Item();
        item.setTitle("Item to delete");
        item.setDescription("Desc");
        item.setPrice(500L);

        Item saved = itemRepository.save(item).block();
        // when
        webTestClient.post().uri("/admin/items/{id}/delete", saved.getId())
                .exchange()
        // then
                .expectStatus().is3xxRedirection()
                .expectHeader().location("/admin");

        StepVerifier.create(itemRepository.existsById(saved.getId()))
                .assertNext(exists -> assertThat(exists, is(false)))
                .verifyComplete();
    }
}