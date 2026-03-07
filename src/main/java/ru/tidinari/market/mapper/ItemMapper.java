package ru.tidinari.market.mapper;

import org.springframework.stereotype.Component;
import ru.tidinari.market.domain.Item;
import ru.tidinari.market.web.dto.ItemDto;

@Component
public class ItemMapper {
    public ItemDto toDto(Item item, String imgPath, int count) {
        return new ItemDto(
                item.getId(),
                item.getTitle(),
                item.getDescription(),
                imgPath,
                item.getPrice(),
                count
        );
    }
}