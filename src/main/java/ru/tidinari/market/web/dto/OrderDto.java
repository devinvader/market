package ru.tidinari.market.web.dto;

import java.util.List;

public record OrderDto(long id, List<ItemDto> items, long totalSum) {
}
