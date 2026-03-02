package ru.tidinari.market.service;

import org.springframework.stereotype.Service;

import ru.tidinari.market.web.dto.ActionTypeDto;
import ru.tidinari.market.web.dto.ItemDto;

import java.util.List;

@Service
public class CartService {

    public List<ItemDto> getCartItems() {
        return List.of(
                new ItemDto(1, "Item 1", "Description 1", "", 100, 0),
                new ItemDto(2, "Item 2", "Description 2", "", 200, 0)
        );
    }

    public int getTotal() {
        return 300;
    }

    public List<ItemDto> actOnCartItems(long id, ActionTypeDto action) {
        return List.of(
                new ItemDto(1, "Item 1", "Description 1", "", 100, 0),
                new ItemDto(2, "Item 2", "Description 2", "", 200, 0)
        );
    }
}