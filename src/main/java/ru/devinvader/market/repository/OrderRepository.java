package ru.devinvader.market.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.devinvader.market.domain.Order;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
}