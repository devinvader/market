package ru.tidinari.market.unit.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import ru.tidinari.market.service.CartService;
import ru.tidinari.market.web.controller.CartController;
import ru.tidinari.market.web.dto.ActionTypeDto;
import ru.tidinari.market.web.dto.ItemDto;

import java.util.List;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CartController.class)
public class CartControllerTest {

    @MockitoBean
    private CartService cartService;

    @Autowired
    private MockMvc mockMvc;

    @Test
    public void getItems_shouldReturnCartView() throws Exception {
        // given
        List<ItemDto> expectedItems = List.of(
                new ItemDto(1L, "Item 1", "Description 1", 1000, 2),
                new ItemDto(2L, "Item 2", "Description 2", 2000, 1)
        );
        int expectedTotal = 4000; // 1000*2 + 2000*1
        when(cartService.getCartItems()).thenReturn(expectedItems);
        when(cartService.getTotal()).thenReturn(expectedTotal);

        // when
        mockMvc.perform(get("/cart/items"))
        // then
                .andExpect(status().isOk())
                .andExpect(view().name("cart"))
                .andExpect(model().attribute("items", expectedItems))
                .andExpect(model().attribute("total", expectedTotal));

        verify(cartService).getCartItems();
        verify(cartService).getTotal();
    }

    @Test
    public void actOnItems_shouldReturnCartView() throws Exception {
        // given
        List<ItemDto> expectedItems = List.of(
                new ItemDto(1L, "Item 1", "Description 1", 1000, 3)
        );
        int expectedTotal = 3000;
        when(cartService.actOnCartItems(1L, ActionTypeDto.PLUS)).thenReturn(expectedItems);
        when(cartService.getTotal()).thenReturn(expectedTotal);

        // when
        mockMvc.perform(post("/cart/items")
                .param("id", "1")
                .param("action", "PLUS"))
        // then
                .andExpect(status().isOk())
                .andExpect(view().name("cart"))
                .andExpect(model().attribute("items", expectedItems))
                .andExpect(model().attribute("total", expectedTotal));

        verify(cartService).actOnCartItems(1L, ActionTypeDto.PLUS);
        verify(cartService).getTotal();
    }
}