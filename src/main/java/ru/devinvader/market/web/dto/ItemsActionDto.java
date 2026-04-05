package ru.devinvader.market.web.dto;

public record ItemsActionDto(
        long id,
        ActionTypeDto action,
        String search,
        SortTypeDto sort,
        Integer pageNumber,
        Integer pageSize
) {
}