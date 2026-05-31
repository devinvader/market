package ru.devinvader.market.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuple2;
import ru.devinvader.market.domain.Item;
import ru.devinvader.market.repository.ItemRepository;
import ru.devinvader.market.web.dto.ItemDto;
import ru.devinvader.market.web.dto.PagedListItemDto;
import ru.devinvader.market.web.dto.PagingDto;
import ru.devinvader.market.web.dto.SortTypeDto;
import ru.devinvader.market.mapper.ItemMapper;

import org.springframework.data.redis.core.ReactiveRedisTemplate;
import ru.devinvader.market.utils.CacheConstants;
import ru.devinvader.market.service.dto.ListQueryResult;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.security.MessageDigest;
import java.nio.charset.StandardCharsets;

@Service
@RequiredArgsConstructor
public class ItemsService {

    private final ItemRepository itemRepository;
    private final CartService cartService;
    private final ItemMapper itemMapper;
    private final ReactiveRedisTemplate<String, Item> itemRedisTemplate;
    private final ReactiveRedisTemplate<String, ListQueryResult> itemListRedisTemplate;
    private final ReactiveRedisTemplate<String, String> marketStringRedisTemplate;

    public Mono<PagedListItemDto> getItems(String search, SortTypeDto sortType, Integer page, Integer size, Long userId) {
        Pageable pageable = PageRequest.of(page, size, sortType.getSort());

        return getListVersion()
                .flatMap(version -> {
                    String cacheKey = buildListCacheKey(version, search, sortType, page, size);
                    return itemListRedisTemplate.opsForValue().get(cacheKey)
                            .switchIfEmpty(Mono.defer(() -> fetchItemsAndTotal(search, pageable)
                                    .map(tuple -> new ListQueryResult(tuple.getT1(), tuple.getT2()))
                                    .flatMap(result -> {
                                        Duration ttl = (search == null || search.isBlank()) ? Duration.ofMinutes(10)
                                                : Duration.ofMinutes(1);
                                        return itemListRedisTemplate.opsForValue().set(cacheKey, result, ttl)
                                                .thenReturn(result);
                                    })));
                })
                .flatMap(result -> {
                    List<Item> items = result.items();
                    Long total = result.total();
                    return toItemDtosWithCounts(items, userId)
                            .map(itemDtos -> {
                                List<List<ItemDto>> rows = groupIntoRows(itemDtos, 3);
                                PagingDto paging = buildPagingDto(total, page, size);
                                return new PagedListItemDto(paging, rows);
                            });
                });
    }

    public Mono<ItemDto> getItem(long id, Long userId) {
        String cacheKey = "item:card:" + id;
        return itemRedisTemplate.opsForValue().get(cacheKey)
                .switchIfEmpty(Mono.defer(() -> itemRepository.findById(id)
                        .switchIfEmpty(Mono.error(new RuntimeException("Item not found")))
                        .flatMap(item -> itemRedisTemplate.opsForValue().set(cacheKey, item, Duration.ofMinutes(10))
                                .thenReturn(item))))
                .flatMap(item -> {
                    if (userId == null) {
                        return Mono.just(itemMapper.toDto(item, 0));
                    }
                    return cartService.getItemCounts(userId, List.of(id))
                            .map(counts -> {
                                int count = counts.getOrDefault(id, 0);
                                return itemMapper.toDto(item, count);
                            });
                });
    }

    private Mono<String> getListVersion() {
        return marketStringRedisTemplate.opsForValue().get(CacheConstants.LIST_VERSION_KEY)
                .switchIfEmpty(Mono.just("1"));
    }

    private String buildListCacheKey(String version, String search, SortTypeDto sortType, Integer page, Integer size) {
        String searchHash = "none";
        if (search != null && !search.isBlank()) {
            try {
                MessageDigest digest = MessageDigest.getInstance("MD5");
                byte[] hash = digest.digest(search.toLowerCase().trim().getBytes(StandardCharsets.UTF_8));
                StringBuilder hexString = new StringBuilder();
                for (byte b : hash) {
                    String hex = Integer.toHexString(0xff & b);
                    if (hex.length() == 1)
                        hexString.append('0');
                    hexString.append(hex);
                }
                searchHash = hexString.toString();
            } catch (Exception e) {
                searchHash = String.valueOf(search.hashCode());
            }
        }
        return String.format("item:list:v%s:%s:%s:%d:%d", version, searchHash, sortType.name(), page, size);
    }

    public Mono<Void> invalidateProductCache(Long id) {
        return itemRedisTemplate.opsForValue().delete("item:card:" + id)
                .then(marketStringRedisTemplate.opsForValue().increment(CacheConstants.LIST_VERSION_KEY))
                .then();
    }

    private Mono<Tuple2<List<Item>, Long>> fetchItemsAndTotal(String search, Pageable pageable) {
        Flux<Item> itemsFlux = itemRepository.findByTitleContainingIgnoreCaseOrDescriptionContainingIgnoreCase(search,
                search, pageable);
        Mono<Long> totalMono = itemRepository.countByTitleContainingIgnoreCaseOrDescriptionContainingIgnoreCase(search,
                search);
        return Mono.zip(itemsFlux.collectList(), totalMono);
    }

    private Mono<List<ItemDto>> toItemDtosWithCounts(List<Item> items, Long userId) {
        if (items.isEmpty()) {
            return Mono.just(List.of());
        }
        List<Long> itemIds = items.stream()
                .map(Item::getId)
                .collect(Collectors.toList());
        if (userId == null) {
            return Mono.just(items.stream()
                    .map(item -> itemMapper.toDto(item, 0))
                    .collect(Collectors.toList()));
        }
        return cartService.getItemCounts(userId, itemIds)
                .map(counts -> items.stream()
                        .map(item -> itemMapper.toDto(item, counts.getOrDefault(item.getId(), 0)))
                        .collect(Collectors.toList()));
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

        if (rows.isEmpty()) {
            return rows;
        }

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
