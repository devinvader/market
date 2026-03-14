package ru.devinvader.market.unit.mapper;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import ru.devinvader.market.domain.Order;
import ru.devinvader.market.domain.OrderItem;
import ru.devinvader.market.domain.CartItem;
import ru.devinvader.market.domain.Item;
import ru.devinvader.market.mapper.OrderItemMapper;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(classes = OrderItemMapper.class)
class OrderItemMapperTest {

    @Autowired
    private OrderItemMapper orderItemMapper;

    @Test
    void fromDto_shouldMapAllFieldsCorrectly() {
        // given
        Order order = new Order();
        order.setId(1L);

        Item item = new Item();
        item.setId(10L);
        item.setTitle("Test Item");
        item.setPrice(500L);

        CartItem cartItem = new CartItem();
        cartItem.setItem(item);
        cartItem.setCount(5);

        // when
        OrderItem orderItem = orderItemMapper.fromDto(order, cartItem);

        // then
        assertNotNull(orderItem);
        assertSame(order, orderItem.getOrder());
        assertSame(item, orderItem.getItem());
        assertEquals(5, orderItem.getCount());
    }

    @Test
    void fromDto_withDifferentValues_shouldMapCorrectly() {
        // given
        Order order = new Order();
        order.setId(2L);

        Item item = new Item();
        item.setId(20L);
        item.setTitle("Another Item");
        item.setPrice(1000L);

        CartItem cartItem = new CartItem();
        cartItem.setItem(item);
        cartItem.setCount(3);

        // when
        OrderItem orderItem = orderItemMapper.fromDto(order, cartItem);

        // then
        assertNotNull(orderItem);
        assertSame(order, orderItem.getOrder());
        assertSame(item, orderItem.getItem());
        assertEquals(3, orderItem.getCount());
    }
}