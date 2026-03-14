package ru.devinvader.market.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import ru.devinvader.market.domain.CartItem;
import ru.devinvader.market.domain.Order;
import ru.devinvader.market.domain.OrderItem;
import ru.devinvader.market.mapper.OrderItemMapper;
import ru.devinvader.market.repository.CartItemRepository;
import ru.devinvader.market.repository.OrderItemRepository;
import ru.devinvader.market.repository.OrderRepository;
import ru.devinvader.market.web.dto.ItemDto;
import ru.devinvader.market.web.dto.OrderDto;
import ru.devinvader.market.mapper.ItemMapper;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final CartItemRepository cartItemRepository;
    private final ImageService imageService;
    private final ItemMapper itemMapper;
    private final OrderItemMapper orderMapper;

    public List<OrderDto> getOrders() {
        List<Order> orders = orderRepository.findAll();

        return orders.stream().map(order -> {
            List<OrderItem> orderItems = orderItemRepository.findByOrderId(order.getId());
            List<ItemDto> items = orderItems.stream()
                    .map(orderItem -> itemMapper.toDto(
                            orderItem.getItem(),
                            orderItem.getCount()
                    ))
                    .collect(Collectors.toList());
            return new OrderDto(order.getId(), items, order.getTotalSum());
        }).collect(Collectors.toList());
    }

    public OrderDto getOrCreateOrder(long id, boolean newOrder) {
        if (newOrder) {
            // Создаем новый заказ из корзины
            return createOrderFromCart(id);
        } else {
            // Получаем существующий заказ
            Order order = orderRepository.findById(id).orElseThrow(() -> new RuntimeException("Order not found"));
            List<OrderItem> orderItems = orderItemRepository.findByOrderId(order.getId());
            List<ItemDto> items = orderItems.stream()
                    .map(orderItem -> itemMapper.toDto(
                            orderItem.getItem(),
                            orderItem.getCount()
                    ))
                    .collect(Collectors.toList());
            return new OrderDto(order.getId(), items, order.getTotalSum());
        }
    }

    private OrderDto createOrderFromCart(long cartId) {
        List<CartItem> cartItems = cartItemRepository.findByCartId(cartId);

        long totalSum = cartItems.stream()
                .mapToLong(cartItem -> cartItem.getItem().getPrice() * cartItem.getCount())
                .sum();
        final Order order = orderRepository.save(new Order(null, totalSum, null));

        // Создаем элементы заказа
        orderItemRepository.saveAll(
                cartItems.stream()
                        .map(cartItem -> orderMapper.fromDto(order, cartItem))
                        .toList());

        // Cart должна быть очищена после покупки
        cartItemRepository.deleteAll(cartItems);

        List<ItemDto> items = cartItems.stream()
                .map(cartItem -> itemMapper.toDto(
                        cartItem.getItem(),
                        cartItem.getCount()
                ))
                .collect(Collectors.toList());

        return new OrderDto(order.getId(), items, totalSum);
    }
}
