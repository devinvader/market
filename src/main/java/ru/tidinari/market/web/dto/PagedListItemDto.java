package ru.tidinari.market.web.dto;

import java.util.List;

public record PagedListItemDto(
        PagingDto pagingDto,
        List<List<ItemDto>> items
) {
}
