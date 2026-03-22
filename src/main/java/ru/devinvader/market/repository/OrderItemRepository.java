package ru.devinvader.market.repository;

import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import ru.devinvader.market.domain.OrderItem;
import ru.devinvader.market.domain.OrderItemId;

@Repository
public interface OrderItemRepository extends ReactiveCrudRepository<OrderItem, OrderItemId> {
    @Query("SELECT * FROM order_items WHERE order_id = :orderId")
    Flux<OrderItem> findByOrderId(@Param("orderId") Long orderId);
}