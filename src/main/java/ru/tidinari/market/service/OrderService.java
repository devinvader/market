package ru.tidinari.market.service;

import org.springframework.stereotype.Service;
import ru.tidinari.market.web.dto.ItemDto;
import ru.tidinari.market.web.dto.OrderDto;

import java.util.List;

@Service
public class OrderService {

    public List<OrderDto> getOrders() {
        return List.of(
                new OrderDto(1, List.of(new ItemDto(1, "Item 1", "Description 1", "", 100, 1)), 100),
                new OrderDto(2, List.of(new ItemDto(2, "Item 2", "Description 2", "", 200, 2)), 200)
        );
    }

    public OrderDto getOrder(long id, boolean newOrder) {
        return new OrderDto(id, List.of(new ItemDto(1, "Item 1", "Description 1", "", 100, 1)), 100);
    }
}
