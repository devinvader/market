package ru.tidinari.market.service;

import org.springframework.stereotype.Service;

import ru.tidinari.market.web.dto.ActionTypeDto;
import ru.tidinari.market.web.dto.ItemDto;
import ru.tidinari.market.web.dto.PagedItemsDto;
import ru.tidinari.market.web.dto.SortTypeDto;

import java.util.List;

@Service
public class ItemsService {

    public PagedItemsDto getItems(String search, SortTypeDto sortType, Integer page, Integer size) {
        // TODO: Реализовать сервис чтобы убрать заглушку
        return new PagedItemsDto(
                List.of(
                        List.of(new ItemDto(1, "Item 1", "Description 1", "", 1, 0)),
                        List.of(new ItemDto(2, "Item 2", "Description 2", "", 1, 0)),
                        List.of(new ItemDto(3, "Item 3", "Description 3", "", 1, 0))
                )
        );
    }

    public ItemDto getItem(long id) {
        // TODO: Убрать заглушку
        return new ItemDto(id, "Item " + id, "Description " + id, "", 1, 0);
    }

    public ItemDto actOnItem(long id, ActionTypeDto action) {
        // TODO: Убрать заглушку
        return new ItemDto(id, "Item " + id, "Description " + id, "", 1, 1);
    }
}