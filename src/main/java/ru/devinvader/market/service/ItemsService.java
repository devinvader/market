package ru.devinvader.market.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuple2;
import reactor.util.function.Tuples;
import ru.devinvader.market.domain.Item;
import ru.devinvader.market.repository.ItemRepository;
import ru.devinvader.market.web.dto.ItemDto;
import ru.devinvader.market.web.dto.PagedListItemDto;
import ru.devinvader.market.web.dto.PagingDto;
import ru.devinvader.market.web.dto.SortTypeDto;
import ru.devinvader.market.mapper.ItemMapper;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ItemsService {

    private final ItemRepository itemRepository;
    private final CartService cartService;
    private final ItemMapper itemMapper;

    public Mono<PagedListItemDto> getItems(String search, SortTypeDto sortType, Integer page, Integer size) {
        Pageable pageable = PageRequest.of(page, size, sortType.getSort());

        return fetchItemsAndTotal(search, pageable)
                .flatMap(tuple -> {
                    List<Item> items = tuple.getT1();
                    Long total = tuple.getT2();
                    return toItemDtosWithCounts(items)
                            .map(itemDtos -> {
                                List<List<ItemDto>> rows = groupIntoRows(itemDtos, 3);
                                PagingDto paging = buildPagingDto(total, page, size);
                                return new PagedListItemDto(paging, rows);
                            });
                });
    }

    public Mono<ItemDto> getItem(long id) {
        return itemRepository.findById(id)
                .switchIfEmpty(Mono.error(new RuntimeException("Item not found")))
                .flatMap(item -> cartService.getItemCounts(List.of(id))
                        .map(counts -> {
                            int count = counts.getOrDefault(id, 0);
                            return itemMapper.toDto(item, count);
                        })
                );
    }

    private Mono<Tuple2<List<Item>, Long>> fetchItemsAndTotal(String search, Pageable pageable) {
        Flux<Item> itemsFlux = itemRepository.searchByTitleOrDescription(search, pageable);
        Mono<Long> totalMono = itemRepository.countByTitleOrDescription(search);
        return Mono.zip(itemsFlux.collectList(), totalMono);
    }

    private Mono<List<ItemDto>> toItemDtosWithCounts(List<Item> items) {
        if (items.isEmpty()) {
            return Mono.just(List.of());
        }
        List<Long> itemIds = items.stream()
                .map(Item::getId)
                .collect(Collectors.toList());
        return cartService.getItemCounts(itemIds)
                .map(counts -> items.stream()
                        .map(item -> itemMapper.toDto(item, counts.getOrDefault(item.getId(), 0)))
                        .collect(Collectors.toList())
                );
    }

    private List<List<ItemDto>> groupIntoRows(List<ItemDto> items, int itemsPerRow) {
        List<List<ItemDto>> rows = new ArrayList<>();
        for (int i = 0; i < items.size(); i += itemsPerRow) {
            List<ItemDto> row = new ArrayList<>();
            for (int j = i; j < i + itemsPerRow && j < items.size(); j++) {
                row.add(items.get(j));
            }
            rows.add(row);
        }

        // Если список пуст, возвращаем пустой список строк
        if (rows.isEmpty()) {
            return rows;
        }

        // Дополняем последнюю строку пустыми DTO
        List<ItemDto> lastRow = rows.get(rows.size() - 1);
        while (lastRow.size() < itemsPerRow) {
            lastRow.add(ItemDto.empty());
        }
        return rows;
    }

    private PagingDto buildPagingDto(Long total, Integer page, Integer size) {
        boolean hasPrevious = page > 0;
        boolean hasNext = total > (long) (page + 1) * size;
        return new PagingDto(size, page, hasPrevious, hasNext);
    }
}