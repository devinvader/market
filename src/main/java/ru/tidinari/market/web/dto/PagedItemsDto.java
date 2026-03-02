package ru.tidinari.market.web.dto;

import java.util.List;

public record PagedItemsDto(
        List<List<ItemDto>> items
) {

}
