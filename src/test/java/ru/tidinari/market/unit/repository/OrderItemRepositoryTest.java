package ru.tidinari.market.unit.repository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import ru.tidinari.market.domain.Item;
import ru.tidinari.market.domain.Order;
import ru.tidinari.market.domain.OrderItem;
import ru.tidinari.market.domain.OrderItemId;
import ru.tidinari.market.repository.ItemRepository;
import ru.tidinari.market.repository.OrderItemRepository;
import ru.tidinari.market.repository.OrderRepository;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class OrderItemRepositoryTest extends BaseRepositoryTest {
    @Autowired
    private OrderItemRepository orderItemRepository;
    
    @Autowired
    private OrderRepository orderRepository;
    
    @Autowired
    private ItemRepository itemRepository;

    @Test
    public void saveAndFindById_givenOrderItem_whenSave_thenFindById() {
        // given
        Order order = new Order();
        order.setTotalSum(1000L);
        Order savedOrder = orderRepository.save(order);
        
        Item item = new Item();
        item.setTitle("Test Item");
        item.setPrice(100L);
        Item savedItem = itemRepository.save(item);
        
        OrderItem orderItem = new OrderItem();
        orderItem.setOrder(savedOrder);
        orderItem.setItem(savedItem);
        orderItem.setCount(2);

        // when
        orderItemRepository.save(orderItem);
        OrderItemId orderItemId = new OrderItemId(savedOrder.getId(), savedItem.getId());
        OrderItem foundOrderItem = orderItemRepository.findById(orderItemId).orElse(null);

        // then
        assertThat(foundOrderItem).isNotNull();
        assertThat(foundOrderItem.getCount()).isEqualTo(2);
    }

    @Test
    public void findByOrderId_givenOrderItem_whenFind_thenReturnList() {
        // given
        Order order = new Order();
        order.setTotalSum(1000L);
        Order savedOrder = orderRepository.save(order);
        
        Item item = new Item();
        item.setTitle("Test Item");
        item.setPrice(100L);
        Item savedItem = itemRepository.save(item);
        
        OrderItem orderItem = new OrderItem();
        orderItem.setOrder(savedOrder);
        orderItem.setItem(savedItem);
        orderItem.setCount(2);
        orderItemRepository.save(orderItem);

        // when
        List<OrderItem> orderItems = orderItemRepository.findByOrderId(savedOrder.getId());

        // then
        assertThat(orderItems).hasSize(1);
        assertThat(orderItems.get(0).getCount()).isEqualTo(2);
    }

    @Test
    public void delete_givenOrderItem_whenDelete_thenNotFound() {
        // given
        Order order = new Order();
        order.setTotalSum(1000L);
        Order savedOrder = orderRepository.save(order);
        
        Item item = new Item();
        item.setTitle("Test Item");
        item.setPrice(100L);
        Item savedItem = itemRepository.save(item);
        
        OrderItem orderItem = new OrderItem();
        orderItem.setOrder(savedOrder);
        orderItem.setItem(savedItem);
        orderItem.setCount(2);
        orderItemRepository.save(orderItem);

        // when
        OrderItemId orderItemId = new OrderItemId(savedOrder.getId(), savedItem.getId());
        orderItemRepository.deleteById(orderItemId);
        OrderItem foundOrderItem = orderItemRepository.findById(orderItemId).orElse(null);

        // then
        assertThat(foundOrderItem).isNull();
    }
}