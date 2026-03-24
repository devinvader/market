package ru.devinvader.market.mapper;

import org.springframework.stereotype.Component;
import ru.devinvader.market.domain.CartItem;
import ru.devinvader.market.domain.Order;
import ru.devinvader.market.domain.OrderItem;

@Component
public class OrderItemMapper {

    public OrderItem fromDto(Order order, CartItem cartItem) {
        return new OrderItem(null, order.getId(), cartItem.getItemId(), cartItem.getCount());
    }
}
