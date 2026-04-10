package ru.devinvader.market.unit.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.codec.multipart.FilePart;
import reactor.core.publisher.Mono;
import ru.devinvader.market.domain.Item;
import ru.devinvader.market.mapper.ItemMapper;
import ru.devinvader.market.repository.ItemRepository;
import ru.devinvader.market.service.AdminService;
import ru.devinvader.market.service.ImageService;
import ru.devinvader.market.web.dto.ItemDto;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AdminServiceTest {

    @Mock
    private ItemRepository itemRepository;

    @Mock
    private ImageService imageService;

    @Spy
    private ItemMapper itemMapper = new ItemMapper();

    @InjectMocks
    private AdminService adminService;

    @Test
    void findItemById_existingId_returnsItem() {
        // given
        Long id = 1L;
        Item expectedItem = new Item(id, "Test Item", "Description", 1000L, null);
        when(itemRepository.findById(id)).thenReturn(Mono.just(expectedItem));

        // when
        Item actualItem = adminService.findItemById(id).block();

        // then
        assertNotNull(actualItem);
        assertEquals(expectedItem.getId(), actualItem.getId());
        verify(itemRepository).findById(id);
    }

    @Test
    void findItemById_nonExistingId_throwsRuntimeException() {
        // given
        Long id = 999L;
        when(itemRepository.findById(id)).thenReturn(Mono.empty());

        // when
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> adminService.findItemById(id).block());

        // then
        assertEquals("Item not found", exception.getMessage());
        verify(itemRepository).findById(id);
    }

    @Test
    void saveItem_validItem_returnsSavedItem() {
        // given
        Item itemToSave = new Item(null, "Item", "Desc", 5000L, null);
        Item savedItem = new Item(1L, "Item", "Desc", 5000L, null);
        when(itemRepository.save(itemToSave)).thenReturn(Mono.just(savedItem));

        // when
        Item result = adminService.saveItem(itemToSave).block();

        // then
        assertNotNull(result);
        assertEquals(savedItem.getId(), result.getId());
        verify(itemRepository).save(itemToSave);
    }

    @Test
    void deleteItemById_validId_deletesItem() {
        // given
        Long id = 1L;
        when(itemRepository.deleteById(id)).thenReturn(Mono.empty());

        // when
        adminService.deleteItemById(id).block();

        // then
        verify(itemRepository).deleteById(id);
    }

    @Test
    void getItemDtoById_existingId_returnsItemDto() {
        // given
        Long id = 1L;
        Item item = new Item(id, "Item", "Desc", 100L, null);
        ItemDto expectedDto = new ItemDto(id, "Item", "Desc", 100L, 0);
        when(itemRepository.findById(id)).thenReturn(Mono.just(item));

        // when
        ItemDto result = adminService.getItemDtoById(id).block();

        // then
        assertNotNull(result);
        assertEquals(expectedDto, result);
        verify(itemRepository).findById(id);
    }

    @Test
    void getItemDtoById_nonExistingId_throwsRuntimeException() {
        // given
        Long id = 999L;
        when(itemRepository.findById(id)).thenReturn(Mono.empty());

        // when
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> adminService.getItemDtoById(id).block());

        // then
        assertEquals("Item not found", exception.getMessage());
        verify(itemRepository).findById(id);
    }

    @Test
    void createItem_withoutImage_savesItemAndNoImage() {
        // given
        String title = "New Item";
        String description = "Description";
        Long price = 5000L;
        FilePart imageFile = null;
        Item unsavedItem = new Item(null, title, description, price, null);
        Item savedItem = new Item(1L, title, description, price, null);
        when(itemRepository.save(any(Item.class))).thenReturn(Mono.just(savedItem));

        // when
        Item result = adminService.createItem(title, description, price, imageFile).block();

        // then
        assertNotNull(result);
        assertEquals(savedItem.getId(), result.getId());
        verify(itemRepository).save(unsavedItem);
        verify(imageService, never()).saveImage(anyLong(), any());
    }

    @Test
    void createItem_withImage_savesItemAndImage() {
        // given
        String title = "New Item";
        String description = "Description";
        Long price = 5000L;
        FilePart imageFile = mock(FilePart.class);
        when(imageFile.filename()).thenReturn("image.jpg");
        Item unsavedItem = new Item(null, title, description, price, null);
        Item savedItem = new Item(1L, title, description, price, null);
        when(itemRepository.save(any(Item.class))).thenReturn(Mono.just(savedItem));
        when(imageService.saveImage(eq(1L), eq(imageFile))).thenReturn(Mono.empty());

        // when
        Item result = adminService.createItem(title, description, price, imageFile).block();

        // then
        assertNotNull(result);
        assertEquals(savedItem.getId(), result.getId());
        verify(itemRepository).save(unsavedItem);
        verify(imageService).saveImage(1L, imageFile);
    }

    @Test
    void createItem_withEmptyFilename_doesNotCallImageService() {
        // given
        String title = "New Item";
        String description = "Description";
        Long price = 5000L;
        FilePart imageFile = mock(FilePart.class);
        when(imageFile.filename()).thenReturn("");
        Item savedItem = new Item(1L, title, description, price, null);
        when(itemRepository.save(any(Item.class))).thenReturn(Mono.just(savedItem));

        // when
        Item result = adminService.createItem(title, description, price, imageFile).block();

        // then
        assertNotNull(result);
        verify(itemRepository).save(any(Item.class));
        verify(imageService, never()).saveImage(anyLong(), any());
    }

    @Test
    void updateItem_withoutImage_updatesItemOnly() {
        // given
        Long id = 1L;
        String title = "Updated Title";
        String description = "Updated Desc";
        Long price = 2000L;
        FilePart imageFile = null;
        Item existingItem = new Item(id, "Old Title", "Old Desc", 1000L, null);
        Item updatedItem = new Item(id, title, description, price, null);
        when(itemRepository.findById(id)).thenReturn(Mono.just(existingItem));
        when(itemRepository.save(any(Item.class))).thenReturn(Mono.just(updatedItem));

        // when
        Item result = adminService.updateItem(id, title, description, price, imageFile).block();

        // then
        assertNotNull(result);
        assertEquals(updatedItem, result);
        verify(itemRepository).findById(id);
        verify(itemRepository).save(existingItem);
        verify(imageService, never()).saveImage(anyLong(), any());
    }

    @Test
    void updateItem_nonExistingId_throwsRuntimeException() {
        // given
        Long id = 999L;
        String title = "Title";
        String description = "Desc";
        Long price = 1000L;
        when(itemRepository.findById(id)).thenReturn(Mono.empty());

        // when
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> adminService.updateItem(id, title, description, price, null).block());

        // then
        assertEquals("Item not found", exception.getMessage());
        verify(itemRepository).findById(id);
        verify(itemRepository, never()).save(any());
        verify(imageService, never()).saveImage(anyLong(), any());
    }
}