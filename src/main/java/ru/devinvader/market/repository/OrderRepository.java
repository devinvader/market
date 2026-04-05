package ru.devinvader.market.repository;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import ru.devinvader.market.domain.Order;

@Repository
public interface OrderRepository extends ReactiveCrudRepository<Order, Long> {
}