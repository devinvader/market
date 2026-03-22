package ru.devinvader.market.mapper;

import org.springframework.stereotype.Component;
import ru.devinvader.market.domain.CartItem;
import ru.devinvader.market.domain.Order;
import ru.devinvader.market.domain.OrderItem;
import ru.devinvader.market.domain.OrderItemId;

@Component
public class OrderItemMapper {

    public OrderItem fromDto(Order order, CartItem cartItem) {
        OrderItemId id = new OrderItemId(order.getId(), cartItem.getId().getItemId());
        return new OrderItem(id, cartItem.getCount());
    }
}
