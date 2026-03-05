package ru.tidinari.market.unit.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import ru.tidinari.market.domain.Item;
import ru.tidinari.market.repository.ItemRepository;
import ru.tidinari.market.service.ItemsService;
import ru.tidinari.market.web.dto.ItemDto;
import ru.tidinari.market.web.dto.SortTypeDto;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class ItemsServiceTest {

    @Mock
    private ItemRepository itemRepository;

    @InjectMocks
    private ItemsService itemsService;

    @Test
    void getItems_EmptyResult_ReturnsEmptyList() {
        // given
        Page<Item> emptyPage = new PageImpl<>(List.of(), PageRequest.of(0, 10), 0);
        when(itemRepository.findByTitleContainingIgnoreCase(anyString(), any(Pageable.class)))
                .thenReturn(emptyPage);

        // when
        List<List<ItemDto>> result = itemsService.getItems("", SortTypeDto.NO, 0, 10);

        // then
        assertEquals(0, result.size());
    }

    @Test
    void getItems_OneItem_ReturnsOneGroupWithTwoEmpty() {
        // given
        Item item = new Item(1L, "Item1", "Desc1", "/img1.jpg", 1000L);
        Page<Item> page = new PageImpl<>(List.of(item), PageRequest.of(0, 10), 1);
        when(itemRepository.findByTitleContainingIgnoreCase(anyString(), any(Pageable.class)))
                .thenReturn(page);

        // when
        List<List<ItemDto>> result = itemsService.getItems("", SortTypeDto.NO, 0, 10);

        // then
        assertEquals(1, result.size());
        List<ItemDto> group = result.get(0);
        assertEquals(3, group.size());
        assertEquals(1L, group.get(0).id());
        assertEquals(-1L, group.get(1).id());
        assertEquals(-1L, group.get(2).id());
        assertEquals("Item1", group.get(0).title());
        assertEquals(ItemDto.empty(), group.get(1));
        assertEquals(ItemDto.empty(), group.get(2));
    }

    @Test
    void getItems_ThreeItems_ReturnsOneGroupWithNoEmpty() {
        // given
        Item item1 = new Item(1L, "Item1", "Desc1", "/img1.jpg", 1000L);
        Item item2 = new Item(2L, "Item2", "Desc2", "/img2.jpg", 2000L);
        Item item3 = new Item(3L, "Item3", "Desc3", "/img3.jpg", 3000L);
        Page<Item> page = new PageImpl<>(List.of(item1, item2, item3), PageRequest.of(0, 10), 3);
        when(itemRepository.findByTitleContainingIgnoreCase(anyString(), any(Pageable.class)))
                .thenReturn(page);

        // when
        List<List<ItemDto>> result = itemsService.getItems("", SortTypeDto.NO, 0, 10);

        // then
        assertEquals(1, result.size());
        List<ItemDto> group = result.get(0);
        assertEquals(3, group.size());
        assertEquals(1L, group.get(0).id());
        assertEquals(2L, group.get(1).id());
        assertEquals(3L, group.get(2).id());
    }

    @Test
    void getItems_FourItems_ReturnsTwoGroups() {
        // given
        Item item1 = new Item(1L, "Item1", "Desc1", "/img1.jpg", 1000L);
        Item item2 = new Item(2L, "Item2", "Desc2", "/img2.jpg", 2000L);
        Item item3 = new Item(3L, "Item3", "Desc3", "/img3.jpg", 3000L);
        Item item4 = new Item(4L, "Item4", "Desc4", "/img4.jpg", 4000L);
        Page<Item> page = new PageImpl<>(List.of(item1, item2, item3, item4), PageRequest.of(0, 10), 4);
        when(itemRepository.findByTitleContainingIgnoreCase(anyString(), any(Pageable.class)))
                .thenReturn(page);

        // when
        List<List<ItemDto>> result = itemsService.getItems("", SortTypeDto.NO, 0, 10);

        // then
        assertEquals(2, result.size());

        List<ItemDto> group1 = result.get(0);
        assertEquals(3, group1.size());
        assertEquals(1L, group1.get(0).id());
        assertEquals(2L, group1.get(1).id());
        assertEquals(3L, group1.get(2).id());

        List<ItemDto> group2 = result.get(1);
        assertEquals(3, group2.size());
        assertEquals(4L, group2.get(0).id());
        assertEquals(ItemDto.empty(), group2.get(1));
        assertEquals(ItemDto.empty(), group2.get(2));
    }

    @Test
    void getItems_FiveItems_ReturnsTwoGroups() {
        // given
        Item item1 = new Item(1L, "Item1", "Desc1", "/img1.jpg", 1000L);
        Item item2 = new Item(2L, "Item2", "Desc2", "/img2.jpg", 2000L);
        Item item3 = new Item(3L, "Item3", "Desc3", "/img3.jpg", 3000L);
        Item item4 = new Item(4L, "Item4", "Desc4", "/img4.jpg", 4000L);
        Item item5 = new Item(5L, "Item5", "Desc5", "/img5.jpg", 5000L);
        Page<Item> page = new PageImpl<>(List.of(item1, item2, item3, item4, item5), PageRequest.of(0, 10), 5);
        when(itemRepository.findByTitleContainingIgnoreCase(anyString(), any(Pageable.class)))
                .thenReturn(page);

        // when
        List<List<ItemDto>> result = itemsService.getItems("", SortTypeDto.NO, 0, 10);

        // then
        assertEquals(2, result.size());

        List<ItemDto> group1 = result.get(0);
        assertEquals(3, group1.size());
        assertEquals(1L, group1.get(0).id());
        assertEquals(2L, group1.get(1).id());
        assertEquals(3L, group1.get(2).id());

        List<ItemDto> group2 = result.get(1);
        assertEquals(3, group2.size());
        assertEquals(4L, group2.get(0).id());
        assertEquals(5L, group2.get(1).id());
        assertEquals(ItemDto.empty(), group2.get(2));
    }
}