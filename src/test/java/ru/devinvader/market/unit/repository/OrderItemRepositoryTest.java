package ru.devinvader.market.unit.repository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import reactor.test.StepVerifier;
import ru.devinvader.market.domain.Item;
import ru.devinvader.market.domain.Order;
import ru.devinvader.market.domain.OrderItem;
import ru.devinvader.market.domain.OrderItemId;
import ru.devinvader.market.repository.ItemRepository;
import ru.devinvader.market.repository.OrderItemRepository;
import ru.devinvader.market.repository.OrderRepository;

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
        Order savedOrder = orderRepository.save(order).block();

        Item item = new Item();
        item.setTitle("Test Item");
        item.setPrice(100L);
        Item savedItem = itemRepository.save(item).block();

        OrderItemId orderItemId = new OrderItemId(savedOrder.getId(), savedItem.getId());
        OrderItem orderItem = new OrderItem(orderItemId, 2);

        // when
        orderItemRepository.save(orderItem).block();

        // then
        StepVerifier.create(orderItemRepository.findById(orderItemId))
                .assertNext(found -> {
                    assertThat(found.getCount()).isEqualTo(2);
                })
                .verifyComplete();
    }

    @Test
    public void findByOrderId_givenOrderItem_whenFind_thenReturnFlux() {
        // given
        Order order = new Order();
        order.setTotalSum(1000L);
        Order savedOrder = orderRepository.save(order).block();

        Item item = new Item();
        item.setTitle("Test Item");
        item.setPrice(100L);
        Item savedItem = itemRepository.save(item).block();

        OrderItemId orderItemId = new OrderItemId(savedOrder.getId(), savedItem.getId());
        OrderItem orderItem = new OrderItem(orderItemId, 2);
        orderItemRepository.save(orderItem).block();

        // when & then
        StepVerifier.create(orderItemRepository.findByOrderId(savedOrder.getId()))
                .assertNext(found -> {
                    assertThat(found.getCount()).isEqualTo(2);
                })
                .verifyComplete();
    }

    @Test
    public void delete_givenOrderItem_whenDelete_thenNotFound() {
        // given
        Order order = new Order();
        order.setTotalSum(1000L);
        Order savedOrder = orderRepository.save(order).block();

        Item item = new Item();
        item.setTitle("Test Item");
        item.setPrice(100L);
        Item savedItem = itemRepository.save(item).block();

        OrderItemId orderItemId = new OrderItemId(savedOrder.getId(), savedItem.getId());
        OrderItem orderItem = new OrderItem(orderItemId, 2);
        orderItemRepository.save(orderItem).block();

        // when
        orderItemRepository.deleteById(orderItemId).block();

        // then
        StepVerifier.create(orderItemRepository.findById(orderItemId))
                .verifyComplete();
    }
}