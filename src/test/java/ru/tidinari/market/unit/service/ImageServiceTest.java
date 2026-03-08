package ru.tidinari.market.unit.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.multipart.MultipartFile;
import ru.tidinari.market.domain.Image;
import ru.tidinari.market.domain.Item;
import ru.tidinari.market.repository.ImageRepository;
import ru.tidinari.market.repository.ItemRepository;
import ru.tidinari.market.service.ImageService;

import java.io.IOException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ImageServiceTest {

    @Mock
    private ImageRepository imageRepository;

    @Mock
    private ItemRepository itemRepository;

    @InjectMocks
    private ImageService imageService;

    private final Long ITEM_ID = 1L;
    private final Long IMAGE_ID = 2L;

    @Test
    void saveImage_newItemAndNoExistingImage_createsAndReturnsImage() throws IOException {
        // given
        Item item = new Item(ITEM_ID, "Test", "Desc", 1000L, null);
        MultipartFile file = mock(MultipartFile.class);
        byte[] fileData = "test data".getBytes();
        String contentType = "image/jpeg";

        when(itemRepository.findById(ITEM_ID)).thenReturn(Optional.of(item));
        when(file.getBytes()).thenReturn(fileData);
        when(file.getContentType()).thenReturn(contentType);
        when(itemRepository.save(any(Item.class))).thenAnswer(inv -> {
            Item itm = inv.getArgument(0);
            itm.setImage(new Image(IMAGE_ID, fileData, contentType, itm));
            return itm;
        });

        // when
        Image result = imageService.saveImage(ITEM_ID, file);

        // then
        assertNotNull(result);
        assertEquals(IMAGE_ID, result.getId());
        assertArrayEquals(fileData, result.getData());
        assertEquals(contentType, result.getContentType());
        assertEquals(item, result.getItem());

        verify(itemRepository).findById(ITEM_ID);
    }

    @Test
    void saveImage_existingImage_updatesAndReturnsImage() throws IOException {
        // given
        MultipartFile file = mock(MultipartFile.class);
        byte[] fileData = "new data".getBytes();
        String contentType = "image/png";

        Image existingImage = new Image(IMAGE_ID, "old data".getBytes(), "image/jpeg", null);
        Item item = new Item(ITEM_ID, "Test", "Desc", 1000L, existingImage);
        existingImage.setItem(item);
        when(itemRepository.findById(ITEM_ID)).thenReturn(Optional.of(item));
        when(file.getBytes()).thenReturn(fileData);
        when(file.getContentType()).thenReturn(contentType);
        when(imageRepository.save(existingImage)).thenReturn(existingImage);
        // when
        Image result = imageService.saveImage(ITEM_ID, file);

        // then
        assertSame(existingImage, result);
        assertArrayEquals(fileData, existingImage.getData());
        assertEquals(contentType, existingImage.getContentType());

        verify(imageRepository).save(new Image(IMAGE_ID, existingImage.getData(), existingImage.getContentType(), item));
    }

    @Test
    void saveImage_itemNotFound_throwsException() {
        // given
        MultipartFile file = mock(MultipartFile.class);
        when(itemRepository.findById(ITEM_ID)).thenReturn(Optional.empty());

        // when / then
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> imageService.saveImage(ITEM_ID, file));
        assertEquals("Item not found", exception.getMessage());

        verify(itemRepository).findById(ITEM_ID);
        verifyNoInteractions(imageRepository);
    }

    @Test
    void getImageByItemId_existing_returnsImage() {
        // given
        Image image = new Image(IMAGE_ID, new byte[0], "image/jpeg", new Item());
        when(imageRepository.findByItemId(ITEM_ID)).thenReturn(Optional.of(image));

        // when
        Image result = imageService.getImageByItemId(ITEM_ID);

        // then
        assertSame(image, result);
        verify(imageRepository).findByItemId(ITEM_ID);
    }

    @Test
    void getImageByItemId_notFound_returnsNull() {
        // given
        when(imageRepository.findByItemId(ITEM_ID)).thenReturn(Optional.empty());

        // when
        Image result = imageService.getImageByItemId(ITEM_ID);

        // then
        assertNull(result);
        verify(imageRepository).findByItemId(ITEM_ID);
    }

    @Test
    void deleteImageByItemId_existing_deletesImage() {
        // given
        when(imageRepository.findIdByItemId(ITEM_ID)).thenReturn(IMAGE_ID);

        // when
        imageService.deleteImageByItemId(ITEM_ID);

        // then
        verify(imageRepository).deleteById(IMAGE_ID);
    }

    @Test
    void deleteImageByItemId_notFound_doesNothing() {
        // given
        when(imageRepository.findIdByItemId(ITEM_ID)).thenReturn(null);

        // when
        imageService.deleteImageByItemId(ITEM_ID);

        // then
        verify(imageRepository, never()).delete(any());
    }
}