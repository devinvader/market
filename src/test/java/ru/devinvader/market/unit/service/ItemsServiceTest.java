package ru.devinvader.market.unit.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import ru.devinvader.market.domain.Item;
import ru.devinvader.market.repository.ItemRepository;
import ru.devinvader.market.service.CartService;
import ru.devinvader.market.service.ImageService;
import ru.devinvader.market.service.ItemsService;
import ru.devinvader.market.web.dto.ItemDto;
import ru.devinvader.market.web.dto.PagedListItemDto;
import ru.devinvader.market.web.dto.SortTypeDto;
import ru.devinvader.market.mapper.ItemMapper;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ItemsServiceTest {

    @Mock
    private ItemRepository itemRepository;
    @Mock
    private ImageService imageService;
    @Mock
    private CartService cartService;
    @Spy
    private ItemMapper itemMapper = new ItemMapper();
    @InjectMocks
    private ItemsService itemsService;

    @Captor
    private ArgumentCaptor<List<Long>> itemIdsCaptor;

    @BeforeEach
    void setUp() {
        // Заглушки чтобы тесты не падали из-за NPE
        lenient().when(cartService.getItemCounts(anyList())).thenReturn(Map.of()); // все count - 0
    }

    @Test
    void getItems_emptyResult_returnsEmptyList() {
        // given
        Page<Item> emptyPage = new PageImpl<>(List.of(), PageRequest.of(0, 10), 0);
        when(itemRepository.searchByTitleOrDescription(anyString(), any(Pageable.class)))
                .thenReturn(emptyPage);

        // when
        PagedListItemDto result = itemsService.getItems("", SortTypeDto.NO, 0, 10);

        // then
        assertEquals(0, result.items().size());
        verify(itemRepository).searchByTitleOrDescription(eq(""), any(Pageable.class));
        verify(cartService).getItemCounts(eq(List.of()));
        verifyNoInteractions(imageService);
    }

    @Test
    void getItems_oneItem_returnsOneGroupWithTwoEmpty() {
        // given
        Item item = new Item(1L, "Item1", "Desc1", 1000L, null);
        Page<Item> page = new PageImpl<>(List.of(item), PageRequest.of(0, 10), 1);
        when(itemRepository.searchByTitleOrDescription(anyString(), any(Pageable.class)))
                .thenReturn(page);
        when(cartService.getItemCounts(List.of(1L))).thenReturn(Map.of(1L, 2));

        // when
        PagedListItemDto result = itemsService.getItems("", SortTypeDto.NO, 0, 10);

        // then
        assertEquals(1, result.items().size());
        List<ItemDto> group = result.items().get(0);
        assertEquals(3, group.size());
        assertEquals(1L, group.get(0).id());
        assertEquals(2, group.get(0).count());
        assertEquals(ItemDto.empty(), group.get(1));
        assertEquals(ItemDto.empty(), group.get(2));

        verify(cartService).getItemCounts(itemIdsCaptor.capture());
        assertEquals(List.of(1L), itemIdsCaptor.getValue());
    }

    @Test
    void getItems_threeItems_returnsOneGroupWithNoEmpty() {
        // given
        Item item1 = new Item(1L, "Item1", "Desc1", 1000L, null);
        Item item2 = new Item(2L, "Item2", "Desc2", 2000L, null);
        Item item3 = new Item(3L, "Item3", "Desc3", 3000L, null);
        Page<Item> page = new PageImpl<>(List.of(item1, item2, item3), PageRequest.of(0, 10), 3);
        when(itemRepository.searchByTitleOrDescription(anyString(), any(Pageable.class)))
                .thenReturn(page);
        when(cartService.getItemCounts(List.of(1L, 2L, 3L))).thenReturn(Map.of(1L, 1, 2L, 0, 3L, 5));

        // when
        PagedListItemDto result = itemsService.getItems("", SortTypeDto.NO, 0, 10);

        // then
        assertEquals(1, result.items().size());
        List<ItemDto> group = result.items().get(0);
        assertEquals(3, group.size());
        assertEquals(1L, group.get(0).id());
        assertEquals(1, group.get(0).count());
        assertEquals(2L, group.get(1).id());
        assertEquals(0, group.get(1).count());
        assertEquals(3L, group.get(2).id());
        assertEquals(5, group.get(2).count());

        verify(cartService).getItemCounts(itemIdsCaptor.capture());
        assertEquals(List.of(1L, 2L, 3L), itemIdsCaptor.getValue());
    }

    @Test
    void getItems_fourItems_returnsTwoGroups() {
        // given
        Item item1 = new Item(1L, "Item1", "Desc1", 1000L, null);
        Item item2 = new Item(2L, "Item2", "Desc2", 2000L, null);
        Item item3 = new Item(3L, "Item3", "Desc3", 3000L, null);
        Item item4 = new Item(4L, "Item4", "Desc4", 4000L, null);
        Page<Item> page = new PageImpl<>(List.of(item1, item2, item3, item4), PageRequest.of(0, 10), 4);
        when(itemRepository.searchByTitleOrDescription(anyString(), any(Pageable.class)))
                .thenReturn(page);
        when(cartService.getItemCounts(List.of(1L, 2L, 3L, 4L))).thenReturn(Map.of());

        // when
        PagedListItemDto result = itemsService.getItems("", SortTypeDto.NO, 0, 10);

        // then
        assertEquals(2, result.items().size());
        List<ItemDto> group1 = result.items().get(0);
        assertEquals(3, group1.size());
        assertEquals(1L, group1.get(0).id());
        assertEquals(2L, group1.get(1).id());
        assertEquals(3L, group1.get(2).id());

        List<ItemDto> group2 = result.items().get(1);
        assertEquals(3, group2.size());
        assertEquals(4L, group2.get(0).id());
        assertEquals(ItemDto.empty(), group2.get(1));
        assertEquals(ItemDto.empty(), group2.get(2));

        verify(cartService).getItemCounts(itemIdsCaptor.capture());
        assertEquals(List.of(1L, 2L, 3L, 4L), itemIdsCaptor.getValue());
    }

    @Test
    void getItems_fiveItems_returnsTwoGroups() {
        // given
        Item item1 = new Item(1L, "Item1", "Desc1", 1000L, null);
        Item item2 = new Item(2L, "Item2", "Desc2", 2000L, null);
        Item item3 = new Item(3L, "Item3", "Desc3", 3000L, null);
        Item item4 = new Item(4L, "Item4", "Desc4", 4000L, null);
        Item item5 = new Item(5L, "Item5", "Desc5", 5000L, null);
        Page<Item> page = new PageImpl<>(List.of(item1, item2, item3, item4, item5), PageRequest.of(0, 10), 5);
        when(itemRepository.searchByTitleOrDescription(anyString(), any(Pageable.class)))
                .thenReturn(page);
        when(cartService.getItemCounts(List.of(1L, 2L, 3L, 4L, 5L))).thenReturn(Map.of());

        // when
        PagedListItemDto result = itemsService.getItems("", SortTypeDto.NO, 0, 10);

        // then
        assertEquals(2, result.items().size());
        List<ItemDto> group1 = result.items().get(0);
        assertEquals(3, group1.size());
        assertEquals(1L, group1.get(0).id());
        assertEquals(2L, group1.get(1).id());
        assertEquals(3L, group1.get(2).id());

        List<ItemDto> group2 = result.items().get(1);
        assertEquals(3, group2.size());
        assertEquals(4L, group2.get(0).id());
        assertEquals(5L, group2.get(1).id());
        assertEquals(ItemDto.empty(), group2.get(2));

        verify(cartService).getItemCounts(itemIdsCaptor.capture());
        assertEquals(List.of(1L, 2L, 3L, 4L, 5L), itemIdsCaptor.getValue());
    }
}