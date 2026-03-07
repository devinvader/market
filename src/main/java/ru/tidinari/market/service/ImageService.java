package ru.tidinari.market.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import ru.tidinari.market.domain.Image;
import ru.tidinari.market.domain.Item;
import ru.tidinari.market.repository.ImageRepository;
import ru.tidinari.market.repository.ItemRepository;

import java.io.IOException;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ImageService {

    private final ImageRepository imageRepository;
    private final ItemRepository itemRepository;

    @Transactional
    public Image saveImage(Long itemId, MultipartFile file) throws IOException {
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new RuntimeException("Item not found"));

        Optional<Image> image = imageRepository.findByItemId(itemId);
        if (image.isPresent()) {
            // Обновляем существующее изображение
            Image existingImage = image.get();
            existingImage.setData(file.getBytes());
            existingImage.setContentType(file.getContentType());
            return imageRepository.save(existingImage);
        } else {
            // Создаем новое изображение
            Image newImage = new Image();
            newImage.setData(file.getBytes());
            newImage.setContentType(file.getContentType());
            newImage.setItem(item);
            return imageRepository.save(newImage);
        }
    }

    @Transactional
    public Image getImageByItemId(Long itemId) {
        return imageRepository.findByItemId(itemId).orElse(null);
    }

    public void deleteImageByItemId(Long itemId) {
        Long imageId = imageRepository.findIdByItemId(itemId);
        if (imageId != null) {
            imageRepository.deleteById(imageId);
        }
    }

    public String getImageUrl(Long itemId) {
        Long imageId = imageRepository.findIdByItemId(itemId);
        if (imageId != null) {
            return "/images/" + imageId;
        }
        return null;
    }
}