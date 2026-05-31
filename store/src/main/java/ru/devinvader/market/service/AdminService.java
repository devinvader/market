package ru.devinvader.market.service;

import lombok.RequiredArgsConstructor;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import ru.devinvader.market.domain.Item;
import ru.devinvader.market.mapper.ItemMapper;
import ru.devinvader.market.repository.ItemRepository;
import ru.devinvader.market.web.dto.ItemDto;

@Service
@RequiredArgsConstructor
public class AdminService {

    private final ItemRepository itemRepository;
    private final ImageService imageService;
    private final ItemMapper itemMapper;
    private final ItemsService itemsService;

    public Mono<Item> findItemById(Long id) {
        return itemRepository.findById(id)
                .switchIfEmpty(Mono.error(new RuntimeException("Item not found")));
    }

    public Mono<ItemDto> getItemDtoById(Long id) {
        return findItemById(id)
                .map(item -> itemMapper.toDto(item, 0));
    }

    public Mono<Item> saveItem(Item item) {
        return itemRepository.save(item)
                .flatMap(savedItem -> itemsService.invalidateProductCache(savedItem.getId()).thenReturn(savedItem));
    }

    public Mono<Void> deleteItemById(Long id) {
        return itemRepository.deleteById(id)
                .then(itemsService.invalidateProductCache(id));
    }

    public Mono<Item> createItem(String title, String description, Long price, FilePart imageFile) {
        Item item = new Item();
        item.setTitle(title);
        item.setDescription(description);
        item.setPrice(price);
        return saveItem(item)
                .flatMap(savedItem -> {
                    if (imageFile != null && imageFile.filename() != null && !imageFile.filename().isEmpty()) {
                        return imageService.saveImage(savedItem.getId(), imageFile)
                                .thenReturn(savedItem);
                    } else {
                        return Mono.just(savedItem);
                    }
                });
    }

    public Mono<Item> updateItem(Long id, String title, String description, Long price, FilePart imageFile) {
        return findItemById(id)
                .flatMap(item -> {
                    item.setTitle(title);
                    item.setDescription(description);
                    item.setPrice(price);
                    return saveItem(item)
                            .flatMap(savedItem -> {
                                if (imageFile != null && imageFile.filename() != null && !imageFile.filename().isEmpty()) {
                                    return imageService.saveImage(id, imageFile)
                                            .thenReturn(savedItem);
                                } else {
                                    return Mono.just(savedItem);
                                }
                            });
                });
    }
}