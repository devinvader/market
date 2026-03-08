package ru.tidinari.market.unit.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import ru.tidinari.market.domain.Item;
import ru.tidinari.market.repository.ImageRepository;
import ru.tidinari.market.service.AdminService;
import ru.tidinari.market.service.ImageService;
import ru.tidinari.market.service.ItemsService;
import ru.tidinari.market.web.controller.AdminController;
import ru.tidinari.market.web.dto.ItemDto;
import ru.tidinari.market.web.dto.PagedListItemDto;
import ru.tidinari.market.web.dto.PagingDto;
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

    @MockitoBean
    private ImageService imageService;

    @Autowired
    private MockMvc mockMvc;

    @Test
    public void getAdminPage_shouldReturnAdminView() throws Exception {
        // given
        List<List<ItemDto>> items = List.of(
                List.of(
                        new ItemDto(1L, "Item 1", "Description 1", 1000, 0),
                        new ItemDto(2L, "Item 2", "Description 2", 2000, 0)
                )
        );
        PagingDto pagingDto = new PagingDto(10, 0, false, false);
        PagedListItemDto expectedResult = new PagedListItemDto(pagingDto, items);
        when(itemsService.getItems("", SortTypeDto.NO, 0, 10))
                .thenReturn(expectedResult);

        // when & then
        mockMvc.perform(get("/admin"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin"))
                .andExpect(model().attribute("search", ""))
                .andExpect(model().attribute("sort", "NO"))
                .andExpect(model().attribute("paging", pagingDto))
                .andExpect(model().attribute("items", items));

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
        // given
        Item savedItem = new Item();
        savedItem.setId(99L);
        savedItem.setTitle("New Item");
        savedItem.setDescription("New Description");
        savedItem.setPrice(5000L);
        when(adminService.createItem(eq("New Item"), eq("New Description"), eq(5000L), eq(null)))
                .thenReturn(savedItem);

        // when
        mockMvc.perform(post("/admin/items")
                        .param("title", "New Item")
                        .param("description", "New Description")
                        .param("price", "5000"))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name("redirect:/admin"));

        // then
        verify(adminService).createItem(eq("New Item"), eq("New Description"), eq(5000L), eq(null));
    }

    @Test
    public void showEditItemForm_shouldReturnEditItemView() throws Exception {
        // given
        ItemDto itemDto = new ItemDto(1L, "Item", "Desc", 1000L, 0);
        when(adminService.getItemDtoById(1L)).thenReturn(itemDto);

        // when & then
        mockMvc.perform(get("/admin/items/1/edit"))
                .andExpect(status().isOk())
                .andExpect(view().name("edit-item"))
                .andExpect(model().attributeExists("item"));

        verify(adminService).getItemDtoById(1L);
    }

    @Test
    public void updateItem_shouldUpdateAndRedirect() throws Exception {
        // given
        Item existingItem = new Item();
        existingItem.setId(1L);
        existingItem.setTitle("Old");
        existingItem.setDescription("Old Desc");
        existingItem.setPrice(1000L);
        when(adminService.updateItem(eq(1L), eq("Updated"), eq("Updated Desc"), eq(2000L), eq(null)))
                .thenReturn(existingItem);

        // when
        mockMvc.perform(post("/admin/items/1")
                        .param("title", "Updated")
                        .param("description", "Updated Desc")
                        .param("price", "2000"))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name("redirect:/admin"));

        // then
        verify(adminService).updateItem(eq(1L), eq("Updated"), eq("Updated Desc"), eq(2000L), eq(null));
    }

    @Test
    public void deleteItem_shouldDeleteAndRedirect() throws Exception {
        mockMvc.perform(post("/admin/items/1/delete"))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name("redirect:/admin"));

        verify(adminService).deleteItemById(1L);
    }
}
