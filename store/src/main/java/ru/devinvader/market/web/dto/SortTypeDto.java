package ru.devinvader.market.web.dto;

import org.springframework.data.domain.Sort;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum SortTypeDto {
    ALPHA(Sort.by("title").ascending()),
    PRICE(Sort.by("price").ascending()),
    NO(Sort.unsorted());

    private final Sort sort;
}
