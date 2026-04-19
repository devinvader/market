package ru.devinvader.market.repository;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import ru.devinvader.market.domain.Cart;

@Repository
public interface CartRepository extends ReactiveCrudRepository<Cart, Long> {
}