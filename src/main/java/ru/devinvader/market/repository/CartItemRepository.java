package ru.devinvader.market.repository;

import java.util.List;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.devinvader.market.domain.CartItem;

public interface CartItemRepository extends ReactiveCrudRepository<CartItem, Long> {
    Flux<CartItem> findByCartId(Long cartId);
    Mono<CartItem> findByCartIdAndItemId(Long cartId, Long itemId);
    Flux<CartItem> findByCartIdAndItemIdIn(Long cartId, List<Long> itemIds);
    Mono<Long> deleteByCartId(Long cartId);
}