package ru.tidinari.market.mapper;

import org.springframework.stereotype.Component;
import ru.tidinari.market.domain.CartItem;
import ru.tidinari.market.domain.Order;
import ru.tidinari.market.domain.OrderItem;

@Component
public class OrderItemMapper {

    public OrderItem fromDto(Order order, CartItem item) {
        return new OrderItem(order, item.getItem(), item.getCount());
    }
}
