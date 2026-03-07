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
    private final Long IMAGE_ID = 100L;

    @Test
    void saveImage_newItemAndNoExistingImage_createsAndReturnsImage() throws IOException {
        // given
        Item item = new Item(ITEM_ID, "Test", "Desc", 1000L, null);
        MultipartFile file = mock(MultipartFile.class);
        byte[] fileData = "test data".getBytes();
        String contentType = "image/jpeg";

        when(itemRepository.findById(ITEM_ID)).thenReturn(Optional.of(item));
        when(imageRepository.findByItemId(ITEM_ID)).thenReturn(null);
        when(file.getBytes()).thenReturn(fileData);
        when(file.getContentType()).thenReturn(contentType);

        when(imageRepository.save(any(Image.class))).thenAnswer(inv -> {
            Image img = inv.getArgument(0);
            img.setId(IMAGE_ID);
            return img;
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
        verify(imageRepository).findByItemId(ITEM_ID);
        verify(imageRepository).save(any(Image.class));
    }

    @Test
    void saveImage_existingImage_updatesAndReturnsImage() throws IOException {
        // given
        Item item = new Item(ITEM_ID, "Test", "Desc", 1000L, null);
        MultipartFile file = mock(MultipartFile.class);
        byte[] fileData = "new data".getBytes();
        String contentType = "image/png";

        Image existingImage = new Image(IMAGE_ID, "old data".getBytes(), "image/jpeg", item);

        when(itemRepository.findById(ITEM_ID)).thenReturn(Optional.of(item));
        when(imageRepository.findByItemId(ITEM_ID)).thenReturn(existingImage);
        when(file.getBytes()).thenReturn(fileData);
        when(file.getContentType()).thenReturn(contentType);
        when(imageRepository.save(existingImage)).thenReturn(existingImage);

        // when
        Image result = imageService.saveImage(ITEM_ID, file);

        // then
        assertSame(existingImage, result);
        assertArrayEquals(fileData, existingImage.getData());
        assertEquals(contentType, existingImage.getContentType());

        verify(imageRepository).save(existingImage);
    }

    @Test
    void saveImage_itemNotFound_throwsException() throws IOException {
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
        when(imageRepository.findByItemId(ITEM_ID)).thenReturn(image);

        // when
        Image result = imageService.getImageByItemId(ITEM_ID);

        // then
        assertSame(image, result);
        verify(imageRepository).findByItemId(ITEM_ID);
    }

    @Test
    void getImageByItemId_notFound_returnsNull() {
        // given
        when(imageRepository.findByItemId(ITEM_ID)).thenReturn(null);

        // when
        Image result = imageService.getImageByItemId(ITEM_ID);

        // then
        assertNull(result);
        verify(imageRepository).findByItemId(ITEM_ID);
    }

    @Test
    void deleteImageByItemId_existing_deletesImage() {
        // given
        Image image = new Image(IMAGE_ID, new byte[0], "image/jpeg", new Item());
        when(imageRepository.findByItemId(ITEM_ID)).thenReturn(image);

        // when
        imageService.deleteImageByItemId(ITEM_ID);

        // then
        verify(imageRepository).delete(image);
    }

    @Test
    void deleteImageByItemId_notFound_doesNothing() {
        // given
        when(imageRepository.findByItemId(ITEM_ID)).thenReturn(null);

        // when
        imageService.deleteImageByItemId(ITEM_ID);

        // then
        verify(imageRepository, never()).delete(any());
    }

    @Test
    void getImageUrl_existing_returnsUrl() {
        // given
        Image image = new Image(IMAGE_ID, new byte[0], "image/jpeg", new Item());
        when(imageRepository.findByItemId(ITEM_ID)).thenReturn(image);

        // when
        String url = imageService.getImageUrl(ITEM_ID);

        // then
        assertEquals("/images/" + IMAGE_ID, url);
    }
}