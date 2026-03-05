package ru.tidinari.market.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import ru.tidinari.market.domain.Cart;
import ru.tidinari.market.domain.CartItem;
import ru.tidinari.market.domain.Order;
import ru.tidinari.market.domain.OrderItem;
import ru.tidinari.market.repository.CartItemRepository;
import ru.tidinari.market.repository.CartRepository;
import ru.tidinari.market.repository.OrderItemRepository;
import ru.tidinari.market.repository.OrderRepository;
import ru.tidinari.market.web.dto.ItemDto;
import ru.tidinari.market.web.dto.OrderDto;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class OrderService {

    @Autowired
    private OrderRepository orderRepository;
    
    @Autowired
    private OrderItemRepository orderItemRepository;
    
    @Autowired
    private CartRepository cartRepository;
    
    @Autowired
    private CartItemRepository cartItemRepository;

    public List<OrderDto> getOrders() {
        List<Order> orders = orderRepository.findAll();
        
        return orders.stream().map(order -> {
            List<OrderItem> orderItems = orderItemRepository.findByOrderId(order.getId());
            List<ItemDto> items = orderItems.stream()
                    .map(orderItem -> {
                        ru.tidinari.market.domain.Item item = orderItem.getItem();
                        return new ItemDto(item.getId(), item.getTitle(), item.getDescription(), item.getImgPath(), item.getPrice(), orderItem.getCount());
                    })
                    .collect(Collectors.toList());
            return new OrderDto(order.getId(), items, order.getTotalSum());
        }).collect(Collectors.toList());
    }

    public OrderDto getOrder(long id, boolean newOrder) {
        if (newOrder) {
            // Создаем новый заказ из корзины
            return createOrderFromCart(id);
        } else {
            // Получаем существующий заказ
            Order order = orderRepository.findById(id).orElseThrow(() -> new RuntimeException("Order not found"));
            List<OrderItem> orderItems = orderItemRepository.findByOrderId(order.getId());
            List<ItemDto> items = orderItems.stream()
                    .map(orderItem -> {
                        ru.tidinari.market.domain.Item item = orderItem.getItem();
                        return new ItemDto(item.getId(), item.getTitle(), item.getDescription(), item.getImgPath(), item.getPrice(), orderItem.getCount());
                    })
                    .collect(Collectors.toList());
            return new OrderDto(order.getId(), items, order.getTotalSum());
        }
    }
    
    private OrderDto createOrderFromCart(long cartId) {
        Cart cart = cartRepository.findById(cartId).orElseThrow(() -> new RuntimeException("Cart not found"));
        List<CartItem> cartItems = cartItemRepository.findByCartId(cartId);
        
        // Создаем новый заказ
        Order order = new Order();
        long totalSum = cartItems.stream().mapToLong(cartItem -> cartItem.getItem().getPrice() * cartItem.getCount()).sum();
        order.setTotalSum(totalSum);
        order = orderRepository.save(order);
        
        // Создаем элементы заказа
        for (CartItem cartItem : cartItems) {
            OrderItem orderItem = new OrderItem();
            orderItem.setOrder(order);
            orderItem.setItem(cartItem.getItem());
            orderItem.setCount(cartItem.getCount());
            orderItemRepository.save(orderItem);
        }
        
        // Очищаем корзину
        cartItemRepository.deleteAll(cartItems);
        
        // Создаем OrderDto
        List<ItemDto> items = cartItems.stream()
                .map(cartItem -> {
                    ru.tidinari.market.domain.Item item = cartItem.getItem();
                    return new ItemDto(item.getId(), item.getTitle(), item.getDescription(), item.getImgPath(), item.getPrice(), cartItem.getCount());
                })
                .collect(Collectors.toList());
        
        return new OrderDto(order.getId(), items, totalSum);
    }
}
