package ru.devinvader.market.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.devinvader.market.domain.Cart;
import ru.devinvader.market.domain.CartItem;
import ru.devinvader.market.domain.Item;
import ru.devinvader.market.mapper.ItemMapper;
import ru.devinvader.market.repository.CartItemRepository;
import ru.devinvader.market.repository.CartRepository;
import ru.devinvader.market.repository.ItemRepository;
import ru.devinvader.market.web.dto.ActionTypeDto;
import ru.devinvader.market.web.dto.ItemDto;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class CartService {

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final ItemRepository itemRepository;
    private final ItemMapper itemMapper;

    // т.к. у нас пока нет ни пользователей, ни сессии, используем один cartId
    private final long DEFAUL_CART_ID = 1L;

    public Flux<ItemDto> getCartItems() {
        Flux<CartItem> cartItems = getUserCart()
                .flatMapMany(cart -> cartItemRepository.findByCartId(cart.getId()));
        Flux<Long> cartItemsId = cartItems.map(CartItem::getItemId);
        Flux<Item> items = itemRepository.findAllById(cartItemsId)
                // чтобы запросить сразу всё и т.к. из ReactiveCrudRepository#findAllById:
                // "Note that the order of elements in the result is not guaranteed."
                .collectMap(Item::getId)
                .flatMapMany(map -> cartItemsId.map(map::get));

        return Flux.zip(cartItems, items)
                .map(tuple -> itemMapper.toDto(
                        tuple.getT2(),
                        tuple.getT1().getCount()));
    }

    public Mono<Long> getTotal() {
        return getCartItems()
                .map(item -> item.price() * item.count())
                .reduce(0L, Long::sum);
    }

    public Mono<Map<Long, Integer>> getItemCounts(List<Long> itemIds) {
        return getUserCart()
                .flatMap(cart -> cartItemRepository.findByCartIdAndItemIdIn(cart.getId(), itemIds)
                        .collectList()
                        .map(cartItems -> {
                            Map<Long, Integer> counts = new HashMap<>();
                            for (CartItem ci : cartItems) {
                                counts.put(ci.getItemId(), ci.getCount());
                            }
                            return counts;
                        }));
    }

    public Flux<ItemDto> actOnCartItems(long id, ActionTypeDto action) {
        return getUserCart()
                .flatMap(cart -> cartItemRepository.findByCartIdAndItemId(cart.getId(), id)
                        .switchIfEmpty(Mono.just(new CartItem(null, cart.getId(), id, 0)))
                        .flatMap(cartItem -> handleAction(cartItem, action))
                        .then()
                )
                .thenMany(getCartItems());
    }

    private Mono<CartItem> handleAction(CartItem cartItem, ActionTypeDto action) {
        switch (action) {
            case PLUS:
                cartItem.setCount(cartItem.getCount() + 1);
                return cartItemRepository.save(cartItem);
            case MINUS:
                if (cartItem.getCount() > 1) {
                    cartItem.setCount(cartItem.getCount() - 1);
                    return cartItemRepository.save(cartItem);
                } else {
                    return cartItemRepository.delete(cartItem).then(Mono.empty());
                }
            case DELETE:
                return cartItemRepository.delete(cartItem).then(Mono.empty());
            default:
                return Mono.error(new IllegalArgumentException("Unknown action"));
        }
    }

    public Mono<Cart> getUserCart() {
        return cartRepository.findById(DEFAUL_CART_ID)
                .switchIfEmpty(Mono.defer(() -> {
                    Cart newCart = new Cart();
                    return cartRepository.save(newCart);
                }));
    }
}