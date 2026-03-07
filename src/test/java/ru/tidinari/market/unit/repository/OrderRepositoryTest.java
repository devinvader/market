package ru.tidinari.market.unit.repository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import ru.tidinari.market.domain.Order;
import ru.tidinari.market.repository.OrderRepository;

import static org.assertj.core.api.Assertions.assertThat;

public class OrderRepositoryTest extends BaseRepositoryTest {
    @Autowired
    private OrderRepository orderRepository;

    @Test
    public void saveAndFindById_givenOrder_whenSave_thenFindById() {
        // given
        Order order = new Order();
        order.setTotalSum(1000L);

        // when
        Order savedOrder = orderRepository.save(order);
        Order foundOrder = orderRepository.findById(savedOrder.getId()).orElse(null);

        // then
        assertThat(foundOrder).isNotNull();
        assertThat(foundOrder.getTotalSum()).isEqualTo(1000L);
    }

    @Test
    public void delete_givenOrder_whenDelete_thenNotFound() {
        // given
        Order order = new Order();
        order.setTotalSum(1000L);
        Order savedOrder = orderRepository.save(order);

        // when
        orderRepository.deleteById(savedOrder.getId());
        Order foundOrder = orderRepository.findById(savedOrder.getId()).orElse(null);

        // then
        assertThat(foundOrder).isNull();
    }
}