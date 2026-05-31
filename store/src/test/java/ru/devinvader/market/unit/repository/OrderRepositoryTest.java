package ru.devinvader.market.unit.repository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import reactor.test.StepVerifier;
import ru.devinvader.market.domain.Order;
import ru.devinvader.market.repository.OrderRepository;

import static org.assertj.core.api.Assertions.assertThat;

public class OrderRepositoryTest extends BaseRepositoryTest {
    @Autowired
    private OrderRepository orderRepository;

    @Test
    public void saveAndFindById_givenOrder_whenSave_thenFindById() {
        // given
        Order order = new Order();
        order.setTotalSum(1000L);
        order.setUserId(1L);

        // when
        Order savedOrder = orderRepository.save(order).block();

        // then
        StepVerifier.create(orderRepository.findById(savedOrder.getId()))
                .assertNext(found -> assertThat(found.getTotalSum()).isEqualTo(1000L))
                .verifyComplete();
    }

    @Test
    public void delete_givenOrder_whenDelete_thenNotFound() {
        // given
        Order order = new Order();
        order.setTotalSum(1000L);
        order.setUserId(1L);
        Order savedOrder = orderRepository.save(order).block();

        // when
        orderRepository.deleteById(savedOrder.getId()).block();

        // then
        StepVerifier.create(orderRepository.findById(savedOrder.getId()))
                .verifyComplete();
    }
}