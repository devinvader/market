package ru.tidinari.market.unit.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.tidinari.market.domain.Item;
import ru.tidinari.market.repository.ItemRepository;
import ru.tidinari.market.service.AdminService;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AdminServiceTest {

    @Mock
    private ItemRepository itemRepository;

    @InjectMocks
    private AdminService adminService;

    @Test
    void findItemById_ExistingId_ReturnsItem() {
        // given
        Long id = 1L;
        Item expectedItem = new Item(id, "Test Item", "Description", 1000L, null);
        when(itemRepository.findById(id)).thenReturn(Optional.of(expectedItem));

        // when
        Item actualItem = adminService.findItemById(id);

        // then
        assertNotNull(actualItem);
        assertEquals(expectedItem.getId(), actualItem.getId());
        verify(itemRepository).findById(id);
    }

    @Test
    void findItemById_NonExistingId_ThrowsRuntimeException() {
        // given
        Long id = 999L;
        when(itemRepository.findById(id)).thenReturn(Optional.empty());

        // when
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> adminService.findItemById(id));

        // then
        assertEquals("Item not found", exception.getMessage());
        verify(itemRepository).findById(id);
    }

    @Test
    void saveItem_ValidItem_ReturnsSavedItem() {
        // given
        Item itemToSave = new Item(null, "New Item", "Desc", 5000L, null);
        Item savedItem = new Item(1L, "New Item", "Desc", 5000L, null);
        when(itemRepository.save(itemToSave)).thenReturn(savedItem);

        // when
        Item result = adminService.saveItem(itemToSave);

        // then
        assertNotNull(result);
        assertEquals(savedItem.getId(), result.getId());
        verify(itemRepository).save(itemToSave);
    }

    @Test
    void deleteItemById_ValidId_DeletesItem() {
        // given
        Long id = 1L;
        doNothing().when(itemRepository).deleteById(id);

        // when
        adminService.deleteItemById(id);

        // then
        verify(itemRepository).deleteById(id);
    }
}