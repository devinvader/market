package ru.devinvader.market.unit.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DefaultDataBufferFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.codec.multipart.FilePart;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import ru.devinvader.market.domain.Image;
import ru.devinvader.market.domain.Item;
import ru.devinvader.market.repository.ImageRepository;
import ru.devinvader.market.repository.ItemRepository;
import ru.devinvader.market.service.ImageService;
import ru.devinvader.market.service.ItemsService;

import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ImageServiceTest {

    @Mock
    private ImageRepository imageRepository;

    @Mock
    private ItemRepository itemRepository;

    @Mock
    private ItemsService itemsService;

    @InjectMocks
    private ImageService imageService;

    @Captor
    private ArgumentCaptor<Image> imageCaptor;

    private final Long ITEM_ID = 1L;
    private final Long IMAGE_ID = 2L;
    private final byte[] IMAGE_DATA = "image data".getBytes();
    private final String CONTENT_TYPE = "image/jpeg";

    @Test
    void saveImage_itemNotFound_throwsError() {
        // given
        FilePart filePart = mock(FilePart.class);
        when(itemRepository.findById(ITEM_ID)).thenReturn(Mono.empty());

        // when
        Mono<Image> result = imageService.saveImage(ITEM_ID, filePart);

        // then
        StepVerifier.create(result)
                .expectErrorMatches(e ->
                        e instanceof RuntimeException && e.getMessage().equals("Item not found"))
                .verify();

        verify(itemRepository).findById(ITEM_ID);
        verifyNoInteractions(imageRepository);
    }

    @Test
    void saveImage_newItemWithoutImage_createsNewImageAndLinks() {
        // given
        Item item = new Item(ITEM_ID, "Item", "Desc", 1000L, null);
        FilePart filePart = mock(FilePart.class);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.IMAGE_JPEG);
        when(filePart.headers()).thenReturn(headers);

        DataBuffer dataBuffer = new DefaultDataBufferFactory().wrap(IMAGE_DATA);
        when(filePart.content()).thenReturn(Flux.just(dataBuffer));

        when(itemRepository.findById(ITEM_ID)).thenReturn(Mono.just(item));
        when(imageRepository.save(any(Image.class))).thenAnswer(inv -> {
            Image img = inv.getArgument(0);
            img.setId(IMAGE_ID);
            return Mono.just(img);
        });
        when(itemRepository.save(any(Item.class))).thenReturn(Mono.just(item));
        when(itemsService.invalidateProductCache(anyLong())).thenReturn(Mono.empty());

        // when
        Mono<Image> result = imageService.saveImage(ITEM_ID, filePart);

        // then
        StepVerifier.create(result)
                .assertNext(savedImage -> {
                    assertNotNull(savedImage);
                    assertEquals(IMAGE_ID, savedImage.getId());
                    assertArrayEquals(IMAGE_DATA, savedImage.getData());
                    assertEquals(CONTENT_TYPE, savedImage.getContentType());
                })
                .verifyComplete();

        verify(imageRepository).save(imageCaptor.capture());
        Image capturedImage = imageCaptor.getValue();
        assertArrayEquals(IMAGE_DATA, capturedImage.getData());
        assertEquals(CONTENT_TYPE, capturedImage.getContentType());

        verify(itemRepository).save(item);
        assertEquals(IMAGE_ID, item.getImageId());
        verify(itemsService).invalidateProductCache(ITEM_ID);
    }

    @Test
    void saveImage_existingItemWithImage_updatesImage() {
        // given
        Item item = new Item(ITEM_ID, "Item", "Desc", 1000L, IMAGE_ID);
        Image existingImage = new Image(IMAGE_ID, "old".getBytes(), CONTENT_TYPE);
        FilePart filePart = mock(FilePart.class);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.IMAGE_JPEG);
        when(filePart.headers()).thenReturn(headers);

        DataBuffer dataBuffer = new DefaultDataBufferFactory().wrap(IMAGE_DATA);
        when(filePart.content()).thenReturn(Flux.just(dataBuffer));

        when(itemRepository.findById(ITEM_ID)).thenReturn(Mono.just(item));
        when(imageRepository.findById(IMAGE_ID)).thenReturn(Mono.just(existingImage));
        when(imageRepository.save(any(Image.class))).thenReturn(Mono.just(existingImage));

        // when
        Mono<Image> result = imageService.saveImage(ITEM_ID, filePart);

        // then
        StepVerifier.create(result)
                .assertNext(savedImage -> {
                    assertSame(existingImage, savedImage);
                    assertArrayEquals(IMAGE_DATA, existingImage.getData());
                    assertEquals(CONTENT_TYPE, existingImage.getContentType());
                })
                .verifyComplete();

        verify(imageRepository).findById(IMAGE_ID);
        verify(imageRepository).save(existingImage);
        verify(itemRepository, never()).save(any());
        verify(itemsService, never()).invalidateProductCache(anyLong());
    }

    @Test
    void getImageByItemId_existing_returnsImage() {
        // given
        Image image = new Image(IMAGE_ID, IMAGE_DATA, CONTENT_TYPE);
        when(imageRepository.findByItemId(ITEM_ID)).thenReturn(Mono.just(image));

        // when
        Mono<Image> result = imageService.getImageByItemId(ITEM_ID);

        // then
        StepVerifier.create(result)
                .expectNext(image)
                .verifyComplete();

        verify(imageRepository).findByItemId(ITEM_ID);
    }

    @Test
    void getImageByItemId_notFound_returnsEmptyMono() {
        // given
        when(imageRepository.findByItemId(ITEM_ID)).thenReturn(Mono.empty());

        // when
        Mono<Image> result = imageService.getImageByItemId(ITEM_ID);

        // then
        StepVerifier.create(result)
                .verifyComplete();

        verify(imageRepository).findByItemId(ITEM_ID);
    }

    @Test
    void deleteImageByItemId_existing_deletesImage() {
        // given
        when(imageRepository.findImageIdByItemId(ITEM_ID)).thenReturn(Mono.just(IMAGE_ID));
        when(imageRepository.deleteById(IMAGE_ID)).thenReturn(Mono.empty());
        when(itemsService.invalidateProductCache(ITEM_ID)).thenReturn(Mono.empty());

        // when
        Mono<Void> result = imageService.deleteImageByItemId(ITEM_ID);

        // then
        StepVerifier.create(result)
                .verifyComplete();

        verify(imageRepository).findImageIdByItemId(ITEM_ID);
        verify(imageRepository).deleteById(IMAGE_ID);
        verify(itemsService).invalidateProductCache(ITEM_ID);
    }

    @Test
    void deleteImageByItemId_notFound_completesWithoutError() {
        // given
        when(imageRepository.findImageIdByItemId(ITEM_ID)).thenReturn(Mono.empty());
        when(itemsService.invalidateProductCache(ITEM_ID)).thenReturn(Mono.empty());

        // when
        Mono<Void> result = imageService.deleteImageByItemId(ITEM_ID);

        // then
        StepVerifier.create(result)
                .verifyComplete();

        verify(imageRepository).findImageIdByItemId(ITEM_ID);
        verify(imageRepository, never()).deleteById(anyLong());
        verify(itemsService).invalidateProductCache(ITEM_ID);
    }
}