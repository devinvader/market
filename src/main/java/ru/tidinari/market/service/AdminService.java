package ru.tidinari.market.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import ru.tidinari.market.domain.Item;
import ru.tidinari.market.mapper.ItemMapper;
import ru.tidinari.market.repository.ItemRepository;
import ru.tidinari.market.web.dto.ItemDto;

import java.io.IOException;

@Service
@RequiredArgsConstructor
public class AdminService {

    private final ItemRepository itemRepository;
    private final ImageService imageService;
    private final ItemMapper itemMapper;

    public Item findItemById(Long id) {
        return itemRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Item not found"));
    }

    public ItemDto getItemDtoById(Long id) {
        Item item = findItemById(id);
        return itemMapper.toDto(item, 0);
    }

    public Item saveItem(Item item) {
        return itemRepository.save(item);
    }

    public void deleteItemById(Long id) {
        itemRepository.deleteById(id);
    }

    public Item createItem(String title, String description, Long price, MultipartFile imageFile) throws IOException {
        Item item = new Item();
        item.setTitle(title);
        item.setDescription(description);
        item.setPrice(price);
        Item savedItem = saveItem(item);

        if (imageFile != null && !imageFile.isEmpty()) {
            imageService.saveImage(savedItem.getId(), imageFile);
        }

        return savedItem;
    }

    public Item updateItem(Long id, String title, String description, Long price, MultipartFile imageFile) throws IOException {
        Item item = findItemById(id);
        item.setTitle(title);
        item.setDescription(description);
        item.setPrice(price);
        Item savedItem = saveItem(item);

        if (imageFile != null && !imageFile.isEmpty()) {
            imageService.saveImage(id, imageFile);
        }

        return savedItem;
    }
}