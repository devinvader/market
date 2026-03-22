package ru.devinvader.market.repository;

import java.util.List;

import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.devinvader.market.domain.CartItem;
import ru.devinvader.market.domain.CartItemId;

public interface CartItemRepository extends ReactiveCrudRepository<CartItem, CartItemId> {

    Flux<CartItem> findByIdCartId(Long cartId);

    @Query("SELECT * FROM cart_items WHERE cart_id = :cartId AND item_id = :itemId")
    Mono<CartItem> findByCartIdAndItemId(@Param("cartId") Long cartId, @Param("itemId") Long itemId);

    @Query("SELECT * FROM cart_items WHERE cart_id = :cartId AND item_id IN (:itemIds)")
    Flux<CartItem> findByCartIdAndItemIdIn(@Param("cartId") Long cartId, @Param("itemIds") List<Long> itemIds);
}