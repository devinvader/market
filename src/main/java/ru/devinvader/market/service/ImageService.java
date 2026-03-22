package ru.devinvader.market.service;

import lombok.RequiredArgsConstructor;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import ru.devinvader.market.domain.Image;
import ru.devinvader.market.repository.ImageRepository;
import ru.devinvader.market.repository.ItemRepository;

@Service
@RequiredArgsConstructor
public class ImageService {

    private final ImageRepository imageRepository;
    private final ItemRepository itemRepository;

    public Mono<Image> saveImage(Long itemId, FilePart filePart) {
        return itemRepository.findById(itemId)
                .switchIfEmpty(Mono.error(new RuntimeException("Item not found")))
                .flatMap(item -> DataBufferUtils.join(filePart.content())
                        .map(dataBuffer -> {
                            byte[] bytes = new byte[dataBuffer.readableByteCount()];
                            dataBuffer.read(bytes);
                            DataBufferUtils.release(dataBuffer);
                            return bytes;
                        })
                        .flatMap(data -> {
                            String contentType = filePart.headers().getContentType() != null
                                    ? filePart.headers().getContentType().toString()
                                    : "application/octet-stream";
                            if (item.getImageId() != null) {
                                // Обновляем существующее изображение
                                return imageRepository.findById(item.getImageId())
                                        .flatMap(existingImage -> {
                                            existingImage.setData(data);
                                            existingImage.setContentType(contentType);
                                            return imageRepository.save(existingImage);
                                        });
                            } else {
                                // Создаем новое изображение
                                return createNewImage(data, contentType)
                                        .flatMap(savedImage -> {
                                            item.setImageId(savedImage.getId());
                                            return itemRepository.save(item).thenReturn(savedImage);
                                        });
                            }
                        })
                );
    }

    private Mono<Image> createNewImage(byte[] data, String contentType) {
        Image newImage = new Image();
        newImage.setData(data);
        newImage.setContentType(contentType);
        return imageRepository.save(newImage);
    }

    public Mono<Image> getImageByItemId(Long itemId) {
        return imageRepository.findByItemId(itemId);
    }

    public Mono<Void> deleteImageByItemId(Long itemId) {
        return imageRepository.findIdByItemId(itemId)
                .flatMap(imageRepository::deleteById)
                .then();
    }
}