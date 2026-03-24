package ru.devinvader.market.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.devinvader.market.domain.*;
import ru.devinvader.market.mapper.ItemMapper;
import ru.devinvader.market.mapper.OrderItemMapper;
import ru.devinvader.market.repository.CartItemRepository;
import ru.devinvader.market.repository.ItemRepository;
import ru.devinvader.market.repository.OrderItemRepository;
import ru.devinvader.market.repository.OrderRepository;
import ru.devinvader.market.web.dto.ItemDto;
import ru.devinvader.market.web.dto.OrderDto;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final CartItemRepository cartItemRepository;
    private final ItemRepository itemRepository;
    private final ItemMapper itemMapper;
    private final OrderItemMapper orderMapper;

    public Flux<OrderDto> getOrders() {
        return orderRepository.findAll()
                .flatMap(order -> findOrderDtoById(order.getId()));
    }

    public Mono<OrderDto> getOrCreateOrder(long id, boolean newOrder) {
        if (newOrder) {
            return createOrderFromCart(/* cartId */ id);
        } else {
            return findOrderDtoById(/* orderId */ id)
                    .switchIfEmpty(Mono.error(new RuntimeException("Order not found")));
        }
    }

    private Mono<OrderDto> findOrderDtoById(Long orderId) {
        return orderRepository.findById(orderId)
                .switchIfEmpty(Mono.error(new RuntimeException("Order not found")))
                .flatMap(order -> orderItemRepository.findByOrderId(orderId)
                        .collectList()
                        .flatMap(orderItems -> toItemDtosFromOrderItems(orderItems)
                                .map(items -> new OrderDto(order.getId(), items, order.getTotalSum()))
                        )
                );
    }

    private Mono<OrderDto> createOrderFromCart(long cartId) {
        return cartItemRepository.findByCartId(cartId)
                .collectList()
                .flatMap(this::processCartItemsAndCreateOrder);
    }

    private Mono<OrderDto> processCartItemsAndCreateOrder(List<CartItem> cartItems) {
        if (cartItems.isEmpty()) {
            return createEmptyOrder();
        }
        return calculateTotalSum(cartItems)
                .flatMap(totalSum -> createOrderAndItems(cartItems, totalSum))
                .flatMap(order -> clearCartAndReturnDto(cartItems, order));
    }

    private Mono<OrderDto> createEmptyOrder() {
        return orderRepository.save(new Order(null, 0L))
                .map(order -> new OrderDto(order.getId(), List.of(), 0L));
    }

    private Mono<Long> calculateTotalSum(List<CartItem> cartItems) {
        return Flux.fromIterable(cartItems)
                .flatMap(cartItem -> itemRepository.findById(cartItem.getItemId())
                        .map(item -> item.getPrice() * cartItem.getCount())
                )
                .reduce(0L, Long::sum);
    }

    private Mono<Order> createOrderAndItems(List<CartItem> cartItems, Long totalSum) {
        return orderRepository.save(new Order(null, totalSum))
                .flatMap(order -> {
                    List<OrderItem> orderItems = cartItems.stream()
                            .map(cartItem -> orderMapper.fromDto(order, cartItem))
                            .collect(Collectors.toList());
                    return orderItemRepository.saveAll(orderItems)
                            .collectList()
                            .thenReturn(order);
                });
    }

    private Mono<OrderDto> clearCartAndReturnDto(List<CartItem> cartItems, Order order) {
        return cartItemRepository.deleteAll(cartItems)
                .then(toItemDtosFromCartItems(cartItems))
                .map(items -> new OrderDto(order.getId(), items, order.getTotalSum()));
    }

    private Mono<List<ItemDto>> toItemDtosFromOrderItems(List<OrderItem> orderItems) {
        return Flux.fromIterable(orderItems)
                .flatMap(orderItem -> itemRepository.findById(orderItem.getItemId())
                        .map(item -> itemMapper.toDto(item, orderItem.getCount()))
                )
                .collectList();
    }

    private Mono<List<ItemDto>> toItemDtosFromCartItems(List<CartItem> cartItems) {
        return Flux.fromIterable(cartItems)
                .flatMap(cartItem -> itemRepository.findById(cartItem.getItemId())
                        .map(item -> itemMapper.toDto(item, cartItem.getCount()))
                )
                .collectList();
    }
}