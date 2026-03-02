package ru.tidinari.market.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.tidinari.market.domain.Cart;

@Repository
public interface CartRepository extends JpaRepository<Cart, Long> {
}