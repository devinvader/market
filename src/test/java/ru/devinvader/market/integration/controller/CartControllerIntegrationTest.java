package ru.devinvader.market.integration.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;
import ru.devinvader.market.TestcontainersConfiguration;
import ru.devinvader.market.domain.CartItem;
import ru.devinvader.market.repository.CartItemRepository;
import ru.devinvader.market.web.dto.ActionTypeDto;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@Import(TestcontainersConfiguration.class)
@Transactional
@TestPropertySource(properties = {
    "spring.liquibase.change-log=classpath:/db/changelog/db.changelog-test-data.xml"
})
class CartControllerIntegrationTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    private MockMvc mockMvc;

    @Autowired
    private CartItemRepository cartItemRepository;

    @BeforeEach
    void setUp() {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
    }

    @Test
    void getItems_shouldReturnCartView() throws Exception {
        mockMvc.perform(get("/cart/items"))
                .andExpect(status().isOk())
                .andExpect(view().name("cart"))
                .andExpect(model().attributeExists("items"))
                .andExpect(model().attributeExists("total"));
    }

    @Test
    void actOnItems_increaseCount_shouldUpdateCart() throws Exception {
        // given
        long itemId = 1L;
        CartItem existing = cartItemRepository.findByCartIdAndItemId(1L, itemId);
        int initialCount = existing.getCount();

        // when
        mockMvc.perform(post("/cart/items")
                        .param("id", String.valueOf(itemId))
                        .param("action", ActionTypeDto.PLUS.name()))
                .andExpect(status().isOk())
                .andExpect(view().name("cart"))
                .andExpect(model().attributeExists("items"))
                .andExpect(model().attributeExists("total"));

        // then
        CartItem updated = cartItemRepository.findByCartIdAndItemId(1L, itemId);
        assertThat(updated.getCount(), equalTo(initialCount + 1));
    }

    @Test
    void actOnItems_decreaseCount_shouldUpdateCart() throws Exception {
        // given
        long itemId = 1L;
        CartItem existing = cartItemRepository.findByCartIdAndItemId(1L, itemId);
        int initialCount = existing.getCount();
        assertThat(initialCount, greaterThan(0));

        // when
        mockMvc.perform(post("/cart/items")
                        .param("id", String.valueOf(itemId))
                        .param("action", ActionTypeDto.MINUS.name()))
                .andExpect(status().isOk())
                .andExpect(view().name("cart"))
                .andExpect(model().attributeExists("items"))
                .andExpect(model().attributeExists("total"));

        // then
        CartItem updated = cartItemRepository.findByCartIdAndItemId(1L, itemId);
        assertThat(updated.getCount(), equalTo(initialCount - 1));
    }

    @Test
    void actOnItems_removeAll_shouldDeleteCartItem() throws Exception {
        // given
        long itemId = 2L;
        CartItem existing = cartItemRepository.findByCartIdAndItemId(1L, itemId);
        assertThat(existing, notNullValue());

        // when
        mockMvc.perform(post("/cart/items")
                        .param("id", String.valueOf(itemId))
                        .param("action", ActionTypeDto.DELETE.name()))
                .andExpect(status().isOk())
                .andExpect(view().name("cart"));

        // then
        CartItem deleted = cartItemRepository.findByCartIdAndItemId(1L, itemId);
        assertThat(deleted, nullValue());
    }
}