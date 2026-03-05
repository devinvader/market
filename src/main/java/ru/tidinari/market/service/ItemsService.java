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
import java.util.stream.Collectors;

@Service
public class ItemsService {

    @Autowired
    private ItemRepository itemRepository;

    public List<List<ItemDto>> getItems(String search, SortTypeDto sortType, Integer page, Integer size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Item> itemPage = itemRepository.findByTitleContainingIgnoreCase(search, pageable);

        List<ItemDto> items = itemPage.getContent().stream()
                .map(item -> new ItemDto(item.getId(), item.getTitle(), item.getDescription(), item.getImgPath(), item.getPrice(), 0))
                .collect(Collectors.toList());

        // Разбиваем на группы по 3 элемента
        List<List<ItemDto>> groups = new java.util.ArrayList<>();
        for (int i = 0; i < items.size(); i += 3) {
            List<ItemDto> group = new java.util.ArrayList<>();
            for (int j = i; j < i + 3 && j < items.size(); j++) {
                group.add(items.get(j));
            }
            // Дополняем до 3 пустыми элементами, если нужно
            while (group.size() < 3) {
                group.add(ItemDto.empty());
            }
            groups.add(group);
        }

        return groups;
    }

    public ItemDto getItem(long id) {
        Item item = itemRepository.findById(id).orElseThrow(() -> new RuntimeException("Item not found"));
        return new ItemDto(item.getId(), item.getTitle(), item.getDescription(), item.getImgPath(), item.getPrice(), 0);
    }

    public ItemDto actOnItem(long id, ActionTypeDto action) {
        Item item = itemRepository.findById(id).orElseThrow(() -> new RuntimeException("Item not found"));
        return new ItemDto(item.getId(), item.getTitle(), item.getDescription(), item.getImgPath(), item.getPrice(), 1);
    }
}