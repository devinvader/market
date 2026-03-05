package ru.tidinari.market.unit.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import ru.tidinari.market.service.CartService;
import ru.tidinari.market.service.ItemsService;
import ru.tidinari.market.web.controller.ItemsController;
import ru.tidinari.market.web.dto.ActionTypeDto;
import ru.tidinari.market.web.dto.ItemDto;
import ru.tidinari.market.web.dto.SortTypeDto;

import java.util.List;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ItemsController.class)
public class ItemsControllerTest {

    @MockitoBean
    private ItemsService itemsService;

    @MockitoBean
    private CartService cartService;

    @Autowired
    private MockMvc mockMvc;

    @Test
    public void getItems_shouldReturnItemsView() throws Exception {
        // given
        List<ItemDto> itemDtoList = List.of(
                new ItemDto(1L, "Item 1", "Description 1", "/img1.jpg", 1000, 0),
                new ItemDto(2L, "Item 2", "Description 2", "/img2.jpg", 2000, 0),
                ItemDto.empty()
        );
        List<List<ItemDto>> expectedItems = List.of(itemDtoList);
        when(itemsService.getItems("", SortTypeDto.NO, 0, 10))
                .thenReturn(expectedItems);

        // when
        mockMvc.perform(get("/items"))
        // then
                .andExpect(status().isOk())
                .andExpect(view().name("items"))
                .andExpect(model().attribute("search", ""))
                .andExpect(model().attribute("sort", "NO"))
                .andExpect(model().attributeExists("paging"))
                .andExpect(model().attribute("items", expectedItems));

        verify(itemsService).getItems("", SortTypeDto.NO, 0, 10);
    }

    @Test
    public void getItem_shouldReturnItemView() throws Exception {
        // given
        ItemDto expectedItem = new ItemDto(1L, "Item 1", "Description 1", "/img1.jpg", 1000, 0);
        when(itemsService.getItem(1L)).thenReturn(expectedItem);

        // when
        mockMvc.perform(get("/items/1"))
        // then
                .andExpect(status().isOk())
                .andExpect(view().name("item"))
                .andExpect(model().attribute("item", expectedItem));

        verify(itemsService).getItem(1L);
    }

    @Test
    public void actOnItem_shouldRedirectToItem() throws Exception {
        // given
        // cartService.actOnCartItems будет вызван, но не возвращает значение

        // when
        mockMvc.perform(post("/items/1")
                .param("action", "PLUS"))
        // then
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name("redirect:/items/1"));

        verify(cartService).actOnCartItems(1L, ActionTypeDto.PLUS);
    }

    @Test
    public void actOnItems_shouldRedirectToItems() throws Exception {
        // given
        // cartService.actOnCartItems будет вызван

        // when
        mockMvc.perform(post("/items")
                .param("id", "1")
                .param("action", "PLUS")
                .param("search", "")
                .param("sortType", "NO")
                .param("pageNumber", "0")
                .param("pageSize", "10"))
        // then
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name("redirect:/items"));

        verify(cartService).actOnCartItems(1L, ActionTypeDto.PLUS);
    }

    @Test
    public void buyItems_shouldRedirectToOrder() throws Exception {
        // when
        mockMvc.perform(post("/buy"))
        // then
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name("redirect:/orders/1"));
    }
}