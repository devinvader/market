package ru.devinvader.market.unit.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import ru.devinvader.market.domain.Item;
import ru.devinvader.market.mapper.ItemMapper;
import ru.devinvader.market.repository.ItemRepository;
import ru.devinvader.market.service.CartService;
import ru.devinvader.market.service.ItemsService;
import ru.devinvader.market.web.dto.ItemDto;
import ru.devinvader.market.web.dto.PagedListItemDto;
import ru.devinvader.market.web.dto.PagingDto;
import ru.devinvader.market.web.dto.SortTypeDto;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.time.Duration;

import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.data.redis.core.ReactiveValueOperations;
import ru.devinvader.market.utils.CacheConstants;
import ru.devinvader.market.service.dto.ListQueryResult;
import org.junit.jupiter.api.BeforeEach;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ItemsServiceTest {

        @Mock
        private ItemRepository itemRepository;

        @Mock
        private CartService cartService;

        @Spy
        private ItemMapper itemMapper = new ItemMapper();

        @Mock
        private ReactiveRedisTemplate<String, Item> itemRedisTemplate;
        @Mock
        private ReactiveRedisTemplate<String, ListQueryResult> itemListRedisTemplate;
        @Mock
        private ReactiveRedisTemplate<String, String> marketStringRedisTemplate;

        @Mock
        private ReactiveValueOperations<String, Item> itemValueOps;
        @Mock
        private ReactiveValueOperations<String, ListQueryResult> listValueOps;
        @Mock
        private ReactiveValueOperations<String, String> marketStringValueOps;

        private ItemsService itemsService;

        @Captor
        private ArgumentCaptor<List<Long>> itemIdsCaptor;

        private final String SEARCH = "test";
        private final SortTypeDto SORT_TYPE = SortTypeDto.NO;
        private final int PAGE = 0;
        private final int SIZE = 10;

        @BeforeEach
        void setUp() {
                lenient().when(itemRedisTemplate.opsForValue()).thenReturn(itemValueOps);
                lenient().when(itemListRedisTemplate.opsForValue()).thenReturn(listValueOps);
                lenient().when(marketStringRedisTemplate.opsForValue()).thenReturn(marketStringValueOps);

                // Default: cache miss (empty), so DB is always hit
                lenient().when(itemValueOps.get(anyString())).thenReturn(Mono.empty());
                lenient().when(listValueOps.get(anyString())).thenReturn(Mono.empty());
                lenient().when(marketStringValueOps.get(CacheConstants.LIST_VERSION_KEY)).thenReturn(Mono.just("1"));

                // Default: cache write succeeds
                lenient().when(itemValueOps.set(anyString(), any(Item.class), any(Duration.class)))
                                .thenReturn(Mono.just(true));
                lenient().when(listValueOps.set(anyString(), any(ListQueryResult.class), any(Duration.class)))
                                .thenReturn(Mono.just(true));

                itemsService = new ItemsService(itemRepository, cartService, itemMapper,
                                itemRedisTemplate, itemListRedisTemplate, marketStringRedisTemplate);
        }

        @Test
        void getItems_emptyResult_returnsEmptyRows() {
                // given
                Pageable pageable = PageRequest.of(PAGE, SIZE, SORT_TYPE.getSort());
                when(itemRepository.findByTitleContainingIgnoreCaseOrDescriptionContainingIgnoreCase(SEARCH, SEARCH,
                                pageable))
                                .thenReturn(Flux.empty());
                when(itemRepository.countByTitleContainingIgnoreCaseOrDescriptionContainingIgnoreCase(SEARCH, SEARCH))
                                .thenReturn(Mono.just(0L));

                // when
                Mono<PagedListItemDto> result = itemsService.getItems(SEARCH, SORT_TYPE, PAGE, SIZE);

                // then
                StepVerifier.create(result)
                                .assertNext(pagedDto -> {
                                        assertEquals(0, pagedDto.items().size());
                                        assertNotNull(pagedDto.pagingDto());
                                        assertEquals(PAGE, pagedDto.pagingDto().pageNumber());
                                        assertEquals(SIZE, pagedDto.pagingDto().pageSize());
                                        assertFalse(pagedDto.pagingDto().hasPrevious());
                                        assertFalse(pagedDto.pagingDto().hasNext());
                                })
                                .verifyComplete();

                verify(itemRepository).findByTitleContainingIgnoreCaseOrDescriptionContainingIgnoreCase(SEARCH, SEARCH,
                                pageable);
                verify(itemRepository).countByTitleContainingIgnoreCaseOrDescriptionContainingIgnoreCase(SEARCH,
                                SEARCH);
        }

        @Test
        void getItems_oneItem_returnsOneGroupWithTwoEmpty() {
                // given
                Pageable pageable = PageRequest.of(PAGE, SIZE, SORT_TYPE.getSort());
                Item item = new Item(1L, "Item1", "Desc1", 1000L, null);
                when(itemRepository.findByTitleContainingIgnoreCaseOrDescriptionContainingIgnoreCase(SEARCH, SEARCH,
                                pageable))
                                .thenReturn(Flux.just(item));
                when(itemRepository.countByTitleContainingIgnoreCaseOrDescriptionContainingIgnoreCase(SEARCH, SEARCH))
                                .thenReturn(Mono.just(1L));
                when(cartService.getItemCounts(List.of(1L))).thenReturn(Mono.just(Map.of(1L, 2)));

                ItemDto itemDto = new ItemDto(1L, "Item1", "Desc1", 1000L, 2);

                // when
                Mono<PagedListItemDto> result = itemsService.getItems(SEARCH, SORT_TYPE, PAGE, SIZE);

                // then
                StepVerifier.create(result)
                                .assertNext(pagedDto -> {
                                        assertEquals(1, pagedDto.items().size());
                                        List<ItemDto> row = pagedDto.items().get(0);
                                        assertEquals(3, row.size());
                                        assertEquals(itemDto, row.get(0));
                                        assertEquals(ItemDto.empty(), row.get(1));
                                        assertEquals(ItemDto.empty(), row.get(2));
                                        assertEquals(PAGE, pagedDto.pagingDto().pageNumber());
                                        assertEquals(SIZE, pagedDto.pagingDto().pageSize());
                                        assertFalse(pagedDto.pagingDto().hasPrevious());
                                        assertFalse(pagedDto.pagingDto().hasNext());
                                })
                                .verifyComplete();

                verify(cartService).getItemCounts(itemIdsCaptor.capture());
                assertEquals(List.of(1L), itemIdsCaptor.getValue());
        }

        @Test
        void getItems_threeItems_returnsOneGroupWithoutEmpty() {
                // given
                Pageable pageable = PageRequest.of(PAGE, SIZE, SORT_TYPE.getSort());
                Item item1 = new Item(1L, "Item1", "Desc1", 1000L, null);
                Item item2 = new Item(2L, "Item2", "Desc2", 2000L, null);
                Item item3 = new Item(3L, "Item3", "Desc3", 3000L, null);
                when(itemRepository.findByTitleContainingIgnoreCaseOrDescriptionContainingIgnoreCase(SEARCH, SEARCH,
                                pageable))
                                .thenReturn(Flux.just(item1, item2, item3));
                when(itemRepository.countByTitleContainingIgnoreCaseOrDescriptionContainingIgnoreCase(SEARCH, SEARCH))
                                .thenReturn(Mono.just(3L));
                when(cartService.getItemCounts(List.of(1L, 2L, 3L))).thenReturn(Mono.just(Map.of(1L, 1, 2L, 0, 3L, 5)));

                ItemDto dto1 = new ItemDto(1L, "Item1", "Desc1", 1000L, 1);
                ItemDto dto2 = new ItemDto(2L, "Item2", "Desc2", 2000L, 0);
                ItemDto dto3 = new ItemDto(3L, "Item3", "Desc3", 3000L, 5);

                // when
                Mono<PagedListItemDto> result = itemsService.getItems(SEARCH, SORT_TYPE, PAGE, SIZE);

                // then
                StepVerifier.create(result)
                                .assertNext(pagedDto -> {
                                        assertEquals(1, pagedDto.items().size());
                                        List<ItemDto> row = pagedDto.items().get(0);
                                        assertEquals(3, row.size());
                                        assertEquals(dto1, row.get(0));
                                        assertEquals(dto2, row.get(1));
                                        assertEquals(dto3, row.get(2));
                                })
                                .verifyComplete();

                verify(cartService).getItemCounts(itemIdsCaptor.capture());
                assertEquals(List.of(1L, 2L, 3L), itemIdsCaptor.getValue());
        }

        @Test
        void getItems_fourItems_returnsTwoGroups_secondGroupHasOneItemAndTwoEmpty() {
                // given
                Pageable pageable = PageRequest.of(PAGE, SIZE, SORT_TYPE.getSort());
                Item item1 = new Item(1L, "Item 1", "Desc 1", 1000L, null);
                Item item2 = new Item(2L, "Item 2", "Desc 2", 2000L, null);
                Item item3 = new Item(3L, "Item 3", "Desc 3", 3000L, null);
                Item item4 = new Item(4L, "Item 4", "Desc 4", 4000L, null);
                when(itemRepository.findByTitleContainingIgnoreCaseOrDescriptionContainingIgnoreCase(SEARCH, SEARCH,
                                pageable))
                                .thenReturn(Flux.just(item1, item2, item3, item4));
                when(itemRepository.countByTitleContainingIgnoreCaseOrDescriptionContainingIgnoreCase(SEARCH, SEARCH))
                                .thenReturn(Mono.just(4L));
                when(cartService.getItemCounts(List.of(1L, 2L, 3L, 4L))).thenReturn(Mono.just(Map.of()));

                ItemDto dto1 = new ItemDto(1L, "Item 1", "Desc 1", 1000L, 0);
                ItemDto dto2 = new ItemDto(2L, "Item 2", "Desc 2", 2000L, 0);
                ItemDto dto3 = new ItemDto(3L, "Item 3", "Desc 3", 3000L, 0);
                ItemDto dto4 = new ItemDto(4L, "Item 4", "Desc 4", 4000L, 0);

                // when
                Mono<PagedListItemDto> result = itemsService.getItems(SEARCH, SORT_TYPE, PAGE, SIZE);

                // then
                StepVerifier.create(result)
                                .assertNext(pagedDto -> {
                                        assertEquals(2, pagedDto.items().size());
                                        List<ItemDto> row1 = pagedDto.items().get(0);
                                        assertEquals(3, row1.size());
                                        assertEquals(dto1, row1.get(0));
                                        assertEquals(dto2, row1.get(1));
                                        assertEquals(dto3, row1.get(2));

                                        List<ItemDto> row2 = pagedDto.items().get(1);
                                        assertEquals(3, row2.size());
                                        assertEquals(dto4, row2.get(0));
                                        assertEquals(ItemDto.empty(), row2.get(1));
                                        assertEquals(ItemDto.empty(), row2.get(2));
                                })
                                .verifyComplete();

                verify(cartService).getItemCounts(itemIdsCaptor.capture());
                assertEquals(List.of(1L, 2L, 3L, 4L), itemIdsCaptor.getValue());
        }

        @Test
        void getItem_existingItemWithCartCount_returnsItemDto() {
                // given
                long itemId = 1L;
                Item item = new Item(itemId, "Item 1", "Desc 1", 1000L, null);
                when(itemRepository.findById(itemId)).thenReturn(Mono.just(item));
                when(cartService.getItemCounts(List.of(itemId))).thenReturn(Mono.just(Map.of(itemId, 3)));

                ItemDto expectedDto = new ItemDto(itemId, "Item 1", "Desc 1", 1000L, 3);

                // when
                Mono<ItemDto> result = itemsService.getItem(itemId);

                // then
                StepVerifier.create(result)
                                .expectNext(expectedDto)
                                .verifyComplete();

                verify(itemRepository).findById(itemId);
                verify(cartService).getItemCounts(List.of(itemId));
        }

        @Test
        void getItem_existingItemWithoutCartCount_returnsItemDtoWithZeroCount() {
                // given
                long itemId = 2L;
                Item item = new Item(itemId, "Item 2", "Desc 2", 2000L, null);
                when(itemRepository.findById(itemId)).thenReturn(Mono.just(item));
                when(cartService.getItemCounts(List.of(itemId))).thenReturn(Mono.just(Map.of()));

                ItemDto expectedDto = new ItemDto(itemId, "Item 2", "Desc 2", 2000L, 0);

                // when
                Mono<ItemDto> result = itemsService.getItem(itemId);

                // then
                StepVerifier.create(result)
                                .expectNext(expectedDto)
                                .verifyComplete();

                verify(itemRepository).findById(itemId);
                verify(cartService).getItemCounts(List.of(itemId));
        }

        @Test
        void getItem_nonExistingItem_throwsError() {
                // given
                long itemId = 999L;
                when(itemRepository.findById(itemId)).thenReturn(Mono.empty());

                // when
                Mono<ItemDto> result = itemsService.getItem(itemId);

                // then
                StepVerifier.create(result)
                                .expectErrorMatches(e -> e instanceof RuntimeException
                                                && e.getMessage().equals("Item not found"))
                                .verify();

                verify(itemRepository).findById(itemId);
        }

        @Test
        void getItems_pagination_hasPreviousAndNext() {
                // given
                Pageable pageable = PageRequest.of(2, 5, SORT_TYPE.getSort());
                when(itemRepository.findByTitleContainingIgnoreCaseOrDescriptionContainingIgnoreCase(SEARCH, SEARCH,
                                pageable))
                                .thenReturn(Flux.empty());
                when(itemRepository.countByTitleContainingIgnoreCaseOrDescriptionContainingIgnoreCase(SEARCH, SEARCH))
                                .thenReturn(Mono.just(20L));

                // when
                Mono<PagedListItemDto> result = itemsService.getItems(SEARCH, SORT_TYPE, 2, 5);

                // then
                StepVerifier.create(result)
                                .assertNext(pagedDto -> {
                                        PagingDto paging = pagedDto.pagingDto();
                                        assertEquals(2, paging.pageNumber());
                                        assertEquals(5, paging.pageSize());
                                        assertTrue(paging.hasPrevious());
                                        assertTrue(paging.hasNext());
                                })
                                .verifyComplete();
        }

        @Test
        void getItems_pagination_noNext() {
                // given
                Pageable pageable = PageRequest.of(2, 10, SORT_TYPE.getSort());
                when(itemRepository.findByTitleContainingIgnoreCaseOrDescriptionContainingIgnoreCase(SEARCH, SEARCH,
                                pageable))
                                .thenReturn(Flux.empty());
                when(itemRepository.countByTitleContainingIgnoreCaseOrDescriptionContainingIgnoreCase(SEARCH, SEARCH))
                                .thenReturn(Mono.just(25L));

                // when
                Mono<PagedListItemDto> result = itemsService.getItems(SEARCH, SORT_TYPE, 2, 10);

                // then
                StepVerifier.create(result)
                                .assertNext(pagedDto -> {
                                        PagingDto paging = pagedDto.pagingDto();
                                        assertEquals(2, paging.pageNumber());
                                        assertEquals(10, paging.pageSize());
                                        assertTrue(paging.hasPrevious());
                                        assertFalse(paging.hasNext());
                                })
                                .verifyComplete();
        }

        @Test
        void getItem_cachedItem_returnsFromCacheWithoutDbCall() {
                // given
                long itemId = 1L;
                String cacheKey = "item:card:" + itemId;
                Item item = new Item(itemId, "Cached Item", "Desc", 1000L, null);
                when(itemValueOps.get(cacheKey)).thenReturn(Mono.just(item));
                when(cartService.getItemCounts(List.of(itemId))).thenReturn(Mono.just(Map.of(itemId, 1)));

                // when
                Mono<ItemDto> result = itemsService.getItem(itemId);

                // then
                StepVerifier.create(result)
                                .assertNext(dto -> {
                                        assertEquals(itemId, dto.id());
                                        assertEquals("Cached Item", dto.title());
                                        assertEquals(1, dto.count());
                                })
                                .verifyComplete();

                verify(itemValueOps).get(cacheKey);
                verifyNoInteractions(itemRepository);
        }

        @Test
        void getItems_cachedList_returnsFromCacheWithoutDbCall() {
                // given
                Item item = new Item(1L, "Cached Item", "Desc", 1000L, null);
                ListQueryResult cachedResult = new ListQueryResult(List.of(item), 1L);
                when(listValueOps.get(anyString())).thenReturn(Mono.just(cachedResult));
                when(cartService.getItemCounts(anyList())).thenReturn(Mono.just(Map.of(1L, 0)));

                // when
                Mono<PagedListItemDto> result = itemsService.getItems(SEARCH, SORT_TYPE, PAGE, SIZE);

                // then
                StepVerifier.create(result)
                                .assertNext(pagedDto -> {
                                        assertEquals(1, pagedDto.items().size());
                                })
                                .verifyComplete();

                verify(listValueOps).get(anyString());
                verifyNoInteractions(itemRepository);
        }

        @Test
        void getItems_cacheMiss_fetchesFromDbAndCaches() {
                // given
                Pageable pageable = PageRequest.of(PAGE, SIZE, SORT_TYPE.getSort());
                Item item = new Item(1L, "DB Item", "Desc", 1000L, null);
                when(itemRepository.findByTitleContainingIgnoreCaseOrDescriptionContainingIgnoreCase(SEARCH, SEARCH,
                                pageable))
                                .thenReturn(Flux.just(item));
                when(itemRepository.countByTitleContainingIgnoreCaseOrDescriptionContainingIgnoreCase(SEARCH, SEARCH))
                                .thenReturn(Mono.just(1L));
                when(cartService.getItemCounts(anyList())).thenReturn(Mono.just(Map.of()));

                // when
                Mono<PagedListItemDto> result = itemsService.getItems(SEARCH, SORT_TYPE, PAGE, SIZE);

                // then
                StepVerifier.create(result).expectNextCount(1).verifyComplete();

                verify(listValueOps).get(anyString());
                verify(itemRepository).findByTitleContainingIgnoreCaseOrDescriptionContainingIgnoreCase(SEARCH, SEARCH,
                                pageable);
                verify(listValueOps).set(anyString(), any(ListQueryResult.class), eq(Duration.ofMinutes(1)));
        }

        @Test
        void getItems_withSearch_usesShorterTtl() {
                // given
                String search = "specific search";
                Pageable pageable = PageRequest.of(PAGE, SIZE, SORT_TYPE.getSort());
                when(itemRepository.findByTitleContainingIgnoreCaseOrDescriptionContainingIgnoreCase(eq(search),
                                eq(search), any()))
                                .thenReturn(Flux.empty());
                when(itemRepository.countByTitleContainingIgnoreCaseOrDescriptionContainingIgnoreCase(eq(search),
                                eq(search)))
                                .thenReturn(Mono.just(0L));

                // when
                itemsService.getItems(search, SORT_TYPE, PAGE, SIZE).block();

                // then
                verify(listValueOps).set(anyString(), any(ListQueryResult.class), eq(Duration.ofMinutes(1)));
        }

        @Test
        void invalidateProductCache_deletesCardAndIncrementsVersion() {
                // given
                long itemId = 1L;
                String cacheKey = "item:card:" + itemId;
                when(itemValueOps.delete(cacheKey)).thenReturn(Mono.just(true));
                when(marketStringValueOps.increment(CacheConstants.LIST_VERSION_KEY)).thenReturn(Mono.just(2L));

                // when
                itemsService.invalidateProductCache(itemId).block();

                // then
                verify(itemValueOps).delete(cacheKey);
                verify(marketStringValueOps).increment(CacheConstants.LIST_VERSION_KEY);
        }
}