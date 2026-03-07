package ru.tidinari.market.unit.mapper;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import ru.tidinari.market.domain.Item;
import ru.tidinari.market.web.dto.ItemDto;
import ru.tidinari.market.mapper.ItemMapper;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest(classes = ItemMapper.class)
class ItemMapperTest {
    @Autowired
    private ItemMapper itemMapper;

    @Test
    void toDto_withValidItem_returnsCorrectDto() {
        Item item = new Item();
        item.setId(1L);
        item.setTitle("Test Item");
        item.setDescription("Test Description");
        item.setPrice(1000L);

        ItemDto dto = itemMapper.toDto(item, "/images/1.jpg", 5);

        assertEquals(1L, dto.id());
        assertEquals("Test Item", dto.title());
        assertEquals("Test Description", dto.description());
        assertEquals("/images/1.jpg", dto.imgPath());
        assertEquals(1000L, dto.price());
        assertEquals(5, dto.count());
    }


    @Test
    void toDto_withDifferentParameters_returnsCorrectDto() {
        Item item = new Item();
        item.setId(10L);
        item.setTitle("Item");
        item.setDescription(null);
        item.setPrice(100L);

        ItemDto dto = itemMapper.toDto(item, null, 2);

        assertEquals(10L, dto.id());
        assertEquals("Item", dto.title());
        assertEquals(null, dto.description());
        assertEquals(null, dto.imgPath());
        assertEquals(100L, dto.price());
        assertEquals(2, dto.count());
    }
}