package ru.tidinari.market.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import ru.tidinari.market.domain.Item;
import ru.tidinari.market.repository.ItemRepository;
import ru.tidinari.market.web.dto.ActionTypeDto;
import ru.tidinari.market.web.dto.ItemDto;
import ru.tidinari.market.web.dto.SortTypeDto;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ItemsService {

    @Autowired
    private ItemRepository itemRepository;

    @Autowired
    private ImageService imageService;

    @Autowired
    private CartService cartService;

    public List<List<ItemDto>> getItems(String search, SortTypeDto sortType, Integer page, Integer size) {
        Pageable pageable = PageRequest.of(page, size, sortType.getSort());
        Page<Item> itemPage = itemRepository.findByTitleContainingIgnoreCase(search, pageable);
        List<Item> itemList = itemPage.getContent();
        
        // Собираем IDs товаров
        List<Long> itemIds = itemList.stream()
                .map(Item::getId)
                .collect(Collectors.toList());
        Map<Long, Integer> counts = cartService.getItemCounts(itemIds);

        List<ItemDto> items = itemList.stream()
                .map(item -> {
                    int count = counts.getOrDefault(item.getId(), 0);
                    return new ItemDto(item.getId(), item.getTitle(), item.getDescription(), imageService.getImageUrl(item.getId()), item.getPrice(), count);
                })
                .collect(Collectors.toList());

        // Разбиваем на группы по 3 элемента
        List<List<ItemDto>> groups = new java.util.ArrayList<>();
        for (int i = 0; i < items.size(); i += 3) {
            List<ItemDto> group = new java.util.ArrayList<>();
            for (int j = i; j < i + 3 && j < items.size(); j++) {
                group.add(items.get(j));
            }
            groups.add(group);
        }
        if (groups.isEmpty()) {
            return groups;
        }
        // В последней группе может быть недобор
        List<ItemDto> lastGroup = groups.getLast();
        while (lastGroup.size() < 3) {
            lastGroup.add(ItemDto.empty());
        }

        return groups;
    }

    public ItemDto getItem(long id) {
        Item item = itemRepository.findById(id).orElseThrow(() -> new RuntimeException("Item not found"));
        Map<Long, Integer> counts = cartService.getItemCounts(List.of(id));
        int count = counts.getOrDefault(id, 0);
        return new ItemDto(item.getId(), item.getTitle(), item.getDescription(), imageService.getImageUrl(id), item.getPrice(), count);
    }

    public ItemDto actOnItem(long id, ActionTypeDto action) {
        Item item = itemRepository.findById(id).orElseThrow(() -> new RuntimeException("Item not found"));
        Map<Long, Integer> counts = cartService.getItemCounts(List.of(id));
        int count = counts.getOrDefault(id, 0);
        return new ItemDto(item.getId(), item.getTitle(), item.getDescription(), imageService.getImageUrl(id), item.getPrice(), count);
    }
}