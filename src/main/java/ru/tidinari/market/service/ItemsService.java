package ru.tidinari.market.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import ru.tidinari.market.domain.Item;
import ru.tidinari.market.repository.ItemRepository;
import ru.tidinari.market.web.dto.ItemDto;
import ru.tidinari.market.web.dto.PagedListItemDto;
import ru.tidinari.market.web.dto.PagingDto;
import ru.tidinari.market.web.dto.SortTypeDto;
import ru.tidinari.market.mapper.ItemMapper;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ItemsService {
    private final ItemRepository itemRepository;
    private final ImageService imageService;
    private final CartService cartService;
    private final ItemMapper itemMapper;

    public PagedListItemDto getItems(String search, SortTypeDto sortType, Integer page, Integer size) {
        Pageable pageable = PageRequest.of(page, size, sortType.getSort());
        Page<Item> itemPage = itemRepository.searchByTitleOrDescription(search, pageable);
        PagingDto pagingDto = new PagingDto(size, page, itemPage.hasPrevious(), itemPage.hasNext());
        List<Item> itemList = itemPage.getContent();
        
        List<Long> itemIds = itemList.stream()
                .map(Item::getId)
                .collect(Collectors.toList());
        Map<Long, Integer> counts = cartService.getItemCounts(itemIds);

        List<ItemDto> items = itemList.stream()
                .map(item -> itemMapper.toDto(
                        item,
                        counts.getOrDefault(item.getId(), 0)
                ))
                .toList();

        // Разбиваем на группы по 3 элемента
        List<List<ItemDto>> groups = new ArrayList<>();
        for (int i = 0; i < items.size(); i += 3) {
            List<ItemDto> group = new ArrayList<>();
            for (int j = i; j < i + 3 && j < items.size(); j++) {
                group.add(items.get(j));
            }
            groups.add(group);
        }
        if (groups.isEmpty()) {
            return new PagedListItemDto(pagingDto, groups);
        }
        // В последней группе может быть недобор
        List<ItemDto> lastGroup = groups.getLast();
        while (lastGroup.size() < 3) {
            lastGroup.add(ItemDto.empty());
        }

        return new PagedListItemDto(pagingDto, groups);
    }

    public ItemDto getItem(long id) {
        Item item = itemRepository.findById(id).orElseThrow(() -> new RuntimeException("Item not found"));
        Map<Long, Integer> counts = cartService.getItemCounts(List.of(id));
        int count = counts.getOrDefault(id, 0);
        return itemMapper.toDto(item, count);
    }
}