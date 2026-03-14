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
import ru.devinvader.market.repository.CartRepository;
import ru.devinvader.market.repository.OrderRepository;
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
class ItemsControllerIntegrationTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    private MockMvc mockMvc;

    @Autowired
    private CartItemRepository cartItemRepository;

    @Autowired
    private CartRepository cartRepository;

    @Autowired
    private OrderRepository orderRepository;

    @BeforeEach
    void setUp() {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
    }

    @Test
    void getItems_shouldReturnItemsView() throws Exception {
        mockMvc.perform(get("/items"))
                .andExpect(status().isOk())
                .andExpect(view().name("items"))
                .andExpect(model().attributeExists("items"))
                .andExpect(model().attributeExists("paging"));
    }

    @Test
    void actOnItems_addToCart_shouldIncreaseCartItemCount() throws Exception {
        // given
        long itemId = 1L;
        CartItem existing = cartItemRepository.findByCartIdAndItemId(1L, itemId);
        int initialCount = existing != null ? existing.getCount() : 0;

        // when
        mockMvc.perform(post("/items")
                        .param("id", String.valueOf(itemId))
                        .param("action", ActionTypeDto.PLUS.name()))
        // then
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name("redirect:/items"));

        CartItem updated = cartItemRepository.findByCartIdAndItemId(1L, itemId);
        assertThat(updated, notNullValue());
        assertThat(updated.getCount(), equalTo(initialCount + 1));
    }

    @Test
    void getItem_shouldReturnItemView() throws Exception {
        mockMvc.perform(get("/items/1"))
                .andExpect(status().isOk())
                .andExpect(view().name("item"))
                .andExpect(model().attributeExists("item"));
    }

    @Test
    void actOnItem_removeFromCart_shouldDecreaseCartItemCount() throws Exception {
        // given
        long itemId = 1L;
        CartItem existing = cartItemRepository.findByCartIdAndItemId(1L, itemId);
        int initialCount = existing.getCount();
        assertThat(initialCount, greaterThan(0));

        // when
        mockMvc.perform(post("/items/{id}", itemId)
                        .param("action", ActionTypeDto.MINUS.name()))
        // then
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name("redirect:/items/" + itemId));

        CartItem updated = cartItemRepository.findByCartIdAndItemId(1L, itemId);
        assertThat(updated.getCount(), equalTo(initialCount - 1));
    }

    @Test
    void buyItems_shouldCreateOrderAndRedirect() throws Exception {
        // when
        mockMvc.perform(post("/buy"))
        // then
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name(containsString("/orders/")));
    }
}