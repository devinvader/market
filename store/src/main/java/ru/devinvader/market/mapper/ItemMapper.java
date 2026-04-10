package ru.devinvader.market.mapper;

import org.springframework.stereotype.Component;
import ru.devinvader.market.domain.Item;
import ru.devinvader.market.web.dto.ItemDto;

@Component
public class ItemMapper {
    public ItemDto toDto(Item item, int count) {
        return new ItemDto(
                item.getId(),
                item.getTitle(),
                item.getDescription(),
                item.getPrice(),
                count
        );
    }
}