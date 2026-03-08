package ru.tidinari.market.integration.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;
import ru.tidinari.market.TestcontainersConfiguration;
import ru.tidinari.market.domain.Item;
import ru.tidinari.market.repository.ItemRepository;

import java.util.List;
import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@Import(TestcontainersConfiguration.class)
@Transactional
class AdminControllerIntegrationTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    private MockMvc mockMvc;

    @Autowired
    private ItemRepository itemRepository;

    @BeforeEach
    void setUp() {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
    }

    @Test
    void getAdminPage_shouldReturnAdminViewWithItems() throws Exception {
        // when & then
        mockMvc.perform(get("/admin"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin"))
                .andExpect(model().attributeExists("items"))
                .andExpect(model().attribute("search", ""))
                .andExpect(model().attribute("sort", "NO"));
    }

    @Test
    void showAddItemForm_shouldReturnAddItemView() throws Exception {
        mockMvc.perform(get("/admin/items/new"))
                .andExpect(status().isOk())
                .andExpect(view().name("add-item"))
                .andExpect(model().attributeExists("item"));
    }

    @Test
    void addItem_shouldSaveItemAndRedirect() throws Exception {
        // given
        String title = "New Test Item";
        String description = "Test Description";
        Long price = 5000L;
        long initialCount = itemRepository.count();

        // when
        mockMvc.perform(post("/admin/items")
                        .param("title", title)
                        .param("description", description)
                        .param("price", price.toString()))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name("redirect:/admin"));

        // then
        assertThat(itemRepository.count(), equalTo(initialCount + 1));
        List<Item> items = itemRepository.findAll();
        Optional<Item> savedItem = items.stream()
                .filter(item -> title.equals(item.getTitle()))
                .findFirst();
        assertThat(savedItem.isPresent(), is(true));
        assertThat(savedItem.get().getDescription(), equalTo(description));
        assertThat(savedItem.get().getPrice(), equalTo(price));
    }

    @Test
    void addItemWithImage_shouldSaveItemAndImage() throws Exception {
        // given
        String title = "Item with Image";
        String description = "Desc";
        Long price = 3000L;
        MockMultipartFile imageFile = new MockMultipartFile(
                "imageFile",
                "test.jpg",
                "image/jpeg",
                "fake image content".getBytes()
        );

        // when
        mockMvc.perform(multipart("/admin/items")
                        .file(imageFile)
                        .param("title", title)
                        .param("description", description)
                        .param("price", price.toString()))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name("redirect:/admin"));

        // then
        Optional<Item> savedItem = itemRepository.findAll().stream()
                .filter(item -> title.equals(item.getTitle()))
                .findFirst();
        assertThat(savedItem.isPresent(), is(true));
    }

    @Test
    void showEditItemForm_shouldReturnEditItemView() throws Exception {
        // given
        Item item = new Item();
        item.setTitle("Test Item");
        item.setDescription("Test Description");
        item.setPrice(1000L);
        Item saved = itemRepository.save(item);
        Long itemId = saved.getId();

        // when & then
        mockMvc.perform(get("/admin/items/{id}/edit", itemId))
                .andExpect(status().isOk())
                .andExpect(view().name("edit-item"))
                .andExpect(model().attributeExists("item"));
    }

    @Test
    void updateItem_shouldUpdateItemAndRedirect() throws Exception {
        // given
        Item item = new Item();
        item.setTitle("Original Title");
        item.setDescription("Original Description");
        item.setPrice(1000L);
        Item saved = itemRepository.save(item);
        Long itemId = saved.getId();

        String newTitle = "Updated Title";
        String newDescription = "Updated Description";
        Long newPrice = 9999L;

        // when
        mockMvc.perform(post("/admin/items/{id}", itemId)
                        .param("title", newTitle)
                        .param("description", newDescription)
                        .param("price", newPrice.toString()))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name("redirect:/admin"));

        // then
        Item updatedItem = itemRepository.findById(itemId).orElseThrow();
        assertThat(updatedItem.getTitle(), equalTo(newTitle));
        assertThat(updatedItem.getDescription(), equalTo(newDescription));
        assertThat(updatedItem.getPrice(), equalTo(newPrice));
    }

    @Test
    void deleteItem_shouldDeleteItemAndRedirect() throws Exception {
        // given
        Item item = new Item();
        item.setTitle("Item to delete");
        item.setDescription("Desc");
        item.setPrice(500L);
        Item saved = itemRepository.save(item);
        Long itemId = saved.getId();
        boolean existsBefore = itemRepository.existsById(itemId);
        assertThat(existsBefore, is(true));

        // when
        mockMvc.perform(post("/admin/items/{id}/delete", itemId))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name("redirect:/admin"));

        // then
        boolean existsAfter = itemRepository.existsById(itemId);
        assertThat(existsAfter, is(false));
    }
}