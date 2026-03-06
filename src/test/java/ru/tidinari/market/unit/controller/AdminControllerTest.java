package ru.tidinari.market.unit.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import ru.tidinari.market.domain.Item;
import ru.tidinari.market.service.AdminService;
import ru.tidinari.market.service.ItemsService;
import ru.tidinari.market.web.controller.AdminController;
import ru.tidinari.market.web.dto.ItemDto;
import ru.tidinari.market.web.dto.SortTypeDto;

import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AdminController.class)
public class AdminControllerTest {

    @MockitoBean
    private ItemsService itemsService;

    @MockitoBean
    private AdminService adminService;

    @Autowired
    private MockMvc mockMvc;

    @Test
    public void getAdminPage_shouldReturnAdminView() throws Exception {
        // given
        List<List<ItemDto>> expectedItems = List.of(
                List.of(
                        new ItemDto(1L, "Item 1", "Description 1", "/img1.jpg", 1000, 0),
                        new ItemDto(2L, "Item 2", "Description 2", "/img2.jpg", 2000, 0)
                )
        );
        when(itemsService.getItems("", SortTypeDto.NO, 0, 10))
                .thenReturn(expectedItems);

        // when & then
        mockMvc.perform(get("/admin"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin"))
                .andExpect(model().attribute("search", ""))
                .andExpect(model().attribute("sort", "NO"))
                .andExpect(model().attributeExists("paging"))
                .andExpect(model().attribute("items", expectedItems));

        verify(itemsService).getItems("", SortTypeDto.NO, 0, 10);
    }

    @Test
    public void showAddItemForm_shouldReturnAddItemView() throws Exception {
        mockMvc.perform(get("/admin/items/new"))
                .andExpect(status().isOk())
                .andExpect(view().name("add-item"))
                .andExpect(model().attributeExists("item"));
    }

    @Test
    public void addItem_shouldSaveAndRedirect() throws Exception {
        // when
        mockMvc.perform(post("/admin/items")
                        .param("title", "New Item")
                        .param("description", "New Description")
                        .param("imgPath", "/new.jpg")
                        .param("price", "5000"))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name("redirect:/admin"));

        // then
        verify(adminService).saveItem(argThat(item ->
                item.getTitle().equals("New Item") &&
                item.getDescription().equals("New Description") &&
                item.getImgPath().equals("/new.jpg") &&
                item.getPrice() == 5000
        ));
    }

    @Test
    public void showEditItemForm_shouldReturnEditItemView() throws Exception {
        // given
        Item item = new Item();
        item.setId(1L);
        item.setTitle("Item");
        item.setDescription("Desc");
        item.setImgPath("/img.jpg");
        item.setPrice(1000L);
        when(adminService.findItemById(1L)).thenReturn(item);

        // when & then
        mockMvc.perform(get("/admin/items/1/edit"))
                .andExpect(status().isOk())
                .andExpect(view().name("edit-item"))
                .andExpect(model().attributeExists("item"));

        verify(adminService).findItemById(1L);
    }

    @Test
    public void updateItem_shouldUpdateAndRedirect() throws Exception {
        // given
        Item existingItem = new Item();
        existingItem.setId(1L);
        existingItem.setTitle("Old");
        existingItem.setDescription("Old Desc");
        existingItem.setImgPath("/old.jpg");
        existingItem.setPrice(1000L);
        when(adminService.findItemById(1L)).thenReturn(existingItem);

        // when
        mockMvc.perform(post("/admin/items/1")
                        .param("title", "Updated")
                        .param("description", "Updated Desc")
                        .param("imgPath", "/updated.jpg")
                        .param("price", "2000"))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name("redirect:/admin"));

        // then
        verify(adminService).findItemById(1L);
        verify(adminService).saveItem(argThat(item ->
                item.getId().equals(1L) &&
                item.getTitle().equals("Updated") &&
                item.getDescription().equals("Updated Desc") &&
                item.getImgPath().equals("/updated.jpg") &&
                item.getPrice() == 2000
        ));
    }

    @Test
    public void deleteItem_shouldDeleteAndRedirect() throws Exception {
        mockMvc.perform(post("/admin/items/1/delete"))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name("redirect:/admin"));

        verify(adminService).deleteItemById(1L);
    }
}