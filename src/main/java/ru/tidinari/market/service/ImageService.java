package ru.tidinari.market.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import ru.tidinari.market.domain.Image;
import ru.tidinari.market.domain.Item;
import ru.tidinari.market.repository.ImageRepository;
import ru.tidinari.market.repository.ItemRepository;

import java.io.IOException;

@Service
@RequiredArgsConstructor
public class ImageService {

    private final ImageRepository imageRepository;
    private final ItemRepository itemRepository;

    public Image saveImage(Long itemId, MultipartFile file) throws IOException {
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new RuntimeException("Item not found"));

        Image existingImage = imageRepository.findByItemId(itemId);
        if (existingImage != null) {
            // Обновляем существующее изображение
            existingImage.setData(file.getBytes());
            existingImage.setContentType(file.getContentType());
            return imageRepository.save(existingImage);
        } else {
            // Создаем новое изображение
            Image image = new Image();
            image.setData(file.getBytes());
            image.setContentType(file.getContentType());
            image.setItem(item);
            return imageRepository.save(image);
        }
    }

    public Image getImageByItemId(Long itemId) {
        return imageRepository.findByItemId(itemId);
    }

    public void deleteImageByItemId(Long itemId) {
        Image image = imageRepository.findByItemId(itemId);
        if (image != null) {
            imageRepository.delete(image);
        }
    }

    public String getImageUrl(Long itemId) {
        Image image = imageRepository.findByItemId(itemId);
        if (image != null) {
            return "/images/" + image.getId();
        }
        return null;
    }
}