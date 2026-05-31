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

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

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

    public Flux<OrderDto> getOrders(Long userId) {
        return orderRepository.findByUserId(userId)
                .flatMap(order -> findOrderDtoById(order.getId()));
    }

    public Mono<OrderDto> getOrCreateOrder(long id, boolean newOrder, Long userId) {
        if (newOrder) {
            return createOrderFromCart(/* cartId */ id, userId);
        } else {
            return orderRepository.findById(id)
                    .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.FORBIDDEN, "Order not found")))
                    .flatMap(order -> {
                        if (!order.getUserId().equals(userId)) {
                            return Mono.error(new ResponseStatusException(HttpStatus.FORBIDDEN, "Order not found"));
                        }
                        return toOrderDto(order);
                    });
        }
    }

    private Mono<OrderDto> findOrderDtoById(Long orderId) {
        return orderRepository.findById(orderId)
                .switchIfEmpty(Mono.error(new RuntimeException("Order not found")))
                .flatMap(this::toOrderDto);
    }

    private Mono<OrderDto> createOrderFromCart(long cartId, Long userId) {
        return cartItemRepository.findByCartId(cartId)
                .collectList()
                .flatMap(cartItems -> processCartItemsAndCreateOrder(cartItems, userId));
    }

    private Mono<OrderDto> processCartItemsAndCreateOrder(List<CartItem> cartItems, Long userId) {
        if (cartItems.isEmpty()) {
            return createEmptyOrder(userId);
        }
        return calculateTotalSum(cartItems)
                .flatMap(totalSum -> createOrderAndItems(cartItems, totalSum, userId))
                .flatMap(order -> clearCartAndReturnDto(cartItems, order));
    }

    private Mono<OrderDto> createEmptyOrder(Long userId) {
        Order order = new Order(null, 0L, userId);
        return orderRepository.save(order)
                .map(saved -> new OrderDto(saved.getId(), List.of(), 0L));
    }

    private Mono<Long> calculateTotalSum(List<CartItem> cartItems) {
        List<Long> itemIds = cartItems.stream()
                .map(CartItem::getItemId)
                .collect(Collectors.toList());

        return itemRepository.findAllById(itemIds)
                .collectMap(Item::getId, Item::getPrice)
                .map(priceMap -> cartItems.stream()
                        .filter(cartItem -> priceMap.containsKey(cartItem.getItemId()))
                        .mapToLong(cartItem -> priceMap.get(cartItem.getItemId()) * cartItem.getCount())
                        .sum()
                );
    }

    private Mono<Order> createOrderAndItems(List<CartItem> cartItems, Long totalSum, Long userId) {
        Order order = new Order(null, totalSum, userId);
        return orderRepository.save(order)
                .flatMap(savedOrder -> {
                    List<OrderItem> orderItems = cartItems.stream()
                            .map(cartItem -> orderMapper.fromDto(savedOrder, cartItem))
                            .collect(Collectors.toList());
                    return orderItemRepository.saveAll(orderItems)
                            .collectList()
                            .thenReturn(savedOrder);
                });
    }

    private Mono<OrderDto> clearCartAndReturnDto(List<CartItem> cartItems, Order order) {
        return cartItemRepository.deleteAll(cartItems)
                .then(toItemDtosFromCartItems(cartItems))
                .map(items -> new OrderDto(order.getId(), items, order.getTotalSum()));
    }

    private Mono<List<ItemDto>> toItemDtosFromOrderItems(List<OrderItem> orderItems) {
        List<Long> itemIds = orderItems.stream()
                .map(OrderItem::getItemId)
                .toList();

        return itemRepository.findAllById(itemIds)
                .collectMap(Item::getId)
                .flatMapMany(itemMap -> Flux.fromIterable(orderItems)
                        .filter(orderItem -> itemMap.containsKey(orderItem.getItemId()))
                        .map(orderItem -> itemMapper.toDto(itemMap.get(orderItem.getItemId()), orderItem.getCount()))
                )
                .collectList();
    }

    private Mono<List<ItemDto>> toItemDtosFromCartItems(List<CartItem> cartItems) {
        List<Long> itemIds = cartItems.stream()
                .map(CartItem::getItemId)
                .toList();

        return itemRepository.findAllById(itemIds)
                .collectMap(Item::getId)
                .flatMapMany(itemMap -> Flux.fromIterable(cartItems)
                        .filter(cartItem -> itemMap.containsKey(cartItem.getItemId()))
                        .map(cartItem -> itemMapper.toDto(itemMap.get(cartItem.getItemId()), cartItem.getCount()))
                )
                .collectList();
    }

    private Mono<OrderDto> toOrderDto(Order order) {
        return orderItemRepository.findByOrderId(order.getId())
                .collectList()
                .flatMap(orderItems -> toItemDtosFromOrderItems(orderItems)
                        .map(items -> new OrderDto(order.getId(), items, order.getTotalSum()))
                );
    }
}