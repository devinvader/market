package ru.devinvader.market.web.dto;

public record PagingDto(
        int pageSize,
        int pageNumber,
        boolean hasPrevious,
        boolean hasNext
) {
}
