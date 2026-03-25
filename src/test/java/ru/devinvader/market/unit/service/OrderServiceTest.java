package ru.devinvader.market.unit.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import ru.devinvader.market.domain.*;
import ru.devinvader.market.mapper.ItemMapper;
import ru.devinvader.market.mapper.OrderItemMapper;
import ru.devinvader.market.repository.CartItemRepository;
import ru.devinvader.market.repository.ItemRepository;
import ru.devinvader.market.repository.OrderItemRepository;
import ru.devinvader.market.repository.OrderRepository;
import ru.devinvader.market.service.OrderService;
import ru.devinvader.market.web.dto.ItemDto;
import ru.devinvader.market.web.dto.OrderDto;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private OrderItemRepository orderItemRepository;

    @Mock
    private CartItemRepository cartItemRepository;

    @Mock
    private ItemRepository itemRepository;

    @Spy
    private ItemMapper itemMapper = new ItemMapper();

    @Spy
    private OrderItemMapper orderItemMapper = new OrderItemMapper();

    @InjectMocks
    private OrderService orderService;

    @Captor
    private ArgumentCaptor<Order> orderCaptor;

    @Captor
    private ArgumentCaptor<List<OrderItem>> orderItemsCaptor;

    @Test
    void getOrders_noOrders_returnsEmptyFlux() {
        // given
        when(orderRepository.findAll()).thenReturn(Flux.empty());

        // when
        Flux<OrderDto> result = orderService.getOrders();

        // then
        StepVerifier.create(result)
                .expectNextCount(0)
                .verifyComplete();
        verify(orderRepository).findAll();
        verifyNoInteractions(orderItemRepository);
    }

    @Test
    void getOrders_withOrders_returnsOrderDtos() {
        // given
        Order order1 = new Order(1L, 5000L);
        Order order2 = new Order(2L, 3000L);
        when(orderRepository.findAll()).thenReturn(Flux.just(order1, order2));
        when(orderRepository.findById(1L)).thenReturn(Mono.just(order1));
        when(orderRepository.findById(2L)).thenReturn(Mono.just(order2));

        // 1 заказ
        OrderItem orderItem1 = new OrderItem(10L, 1L, 100L, 2);
        OrderItem orderItem2 = new OrderItem(20L, 1L, 200L, 1);
        // заказ 2
        OrderItem orderItem3 = new OrderItem(30L, 2L, 300L, 3);

        when(orderItemRepository.findByOrderId(1L)).thenReturn(Flux.just(orderItem1, orderItem2));
        when(orderItemRepository.findByOrderId(2L)).thenReturn(Flux.just(orderItem3));
        Item item1 = new Item(100L, "Item 1", "Desc 1", 1000L, null);
        Item item2 = new Item(200L, "Item 2", "Desc 2", 2000L, null);
        Item item3 = new Item(300L, "Item 3", "Desc 3", 1500L, null);
        when(itemRepository.findById(100L)).thenReturn(Mono.just(item1));
        when(itemRepository.findById(200L)).thenReturn(Mono.just(item2));
        when(itemRepository.findById(300L)).thenReturn(Mono.just(item3));
        ItemDto dto1 = new ItemDto(100L, "Item 1", "Desc 1", 1000L, 2);
        ItemDto dto2 = new ItemDto(200L, "Item 2", "Desc 2", 2000L, 1);
        ItemDto dto3 = new ItemDto(300L, "Item 3", "Desc 3", 1500L, 3);

        // when
        Flux<OrderDto> result = orderService.getOrders();

        // then
        StepVerifier.create(result)
                .expectNextMatches(dto -> dto.id() == 1L &&
                        dto.totalSum() == 5000L &&
                        dto.items().size() == 2 &&
                        dto.items().contains(dto1) &&
                        dto.items().contains(dto2))
                .expectNextMatches(dto -> dto.id() == 2L &&
                        dto.totalSum() == 3000L &&
                        dto.items().size() == 1 &&
                        dto.items().contains(dto3))
                .verifyComplete();

        verify(orderRepository).findAll();
        verify(orderItemRepository).findByOrderId(1L);
        verify(orderItemRepository).findByOrderId(2L);
        verify(itemRepository, times(1)).findById(100L);
        verify(itemRepository, times(1)).findById(200L);
        verify(itemRepository, times(1)).findById(300L);
    }

    @Test
    void getOrCreateOrder_existingOrder_returnsOrderDto() {
        // given
        long orderId = 5L;
        Order order = new Order(orderId, 3000L);
        OrderItem orderItem = new OrderItem(30L, orderId, 300L, 2);
        Item item = new Item(300L, "Item 3", "Desc 3", 1500L, null);
        ItemDto itemDto = new ItemDto(300L, "Item 3", "Desc 3", 1500L, 2);

        when(orderRepository.findById(orderId)).thenReturn(Mono.just(order));
        when(orderItemRepository.findByOrderId(orderId)).thenReturn(Flux.just(orderItem));
        when(itemRepository.findById(300L)).thenReturn(Mono.just(item));

        // when
        Mono<OrderDto> result = orderService.getOrCreateOrder(orderId, false);

        // then
        StepVerifier.create(result)
                .expectNextMatches(dto -> dto.id() == orderId &&
                        dto.totalSum() == 3000L &&
                        dto.items().size() == 1 &&
                        dto.items().get(0).equals(itemDto))
                .verifyComplete();

        verify(orderRepository).findById(orderId);
        verify(orderItemRepository).findByOrderId(orderId);
        verify(itemRepository).findById(300L);
    }

    @Test
    void getOrCreateOrder_existingOrderNotFound_throwsError() {
        // given
        long orderId = 99L;
        when(orderRepository.findById(orderId)).thenReturn(Mono.empty());

        // when
        Mono<OrderDto> result = orderService.getOrCreateOrder(orderId, false);

        // then
        StepVerifier.create(result)
                .expectErrorMatches(e ->
                        e instanceof RuntimeException && e.getMessage().equals("Order not found"))
                .verify();

        verify(orderRepository).findById(orderId);
    }

    @Test
    void getOrCreateOrder_newOrderWithCartItems_createsOrderAndClearsCart() {
        // given
        long cartId = 1L;
        CartItem cartItem1 = new CartItem(1L, cartId, 10L, 3);
        CartItem cartItem2 = new CartItem(2L, cartId, 20L, 1);
        Item item1 = new Item(10L, "Item 1", "Desc 1", 1000L, null);
        Item item2 = new Item(20L, "Item 2", "Desc 2", 2000L, null);
        Order savedOrder = new Order(100L, 5000L);
        OrderItem orderItem1 = new OrderItem(null, 100L, 10L, 3);
        OrderItem orderItem2 = new OrderItem(null, 100L, 20L, 1);
        ItemDto dto1 = new ItemDto(10L, "Item 1", "Desc 1", 1000L, 3);
        ItemDto dto2 = new ItemDto(20L, "Item 2", "Desc 2", 2000L, 1);

        when(cartItemRepository.findByCartId(cartId)).thenReturn(Flux.just(cartItem1, cartItem2));
        when(itemRepository.findById(10L)).thenReturn(Mono.just(item1));
        when(itemRepository.findById(20L)).thenReturn(Mono.just(item2));
        when(orderRepository.save(any(Order.class))).thenReturn(Mono.just(savedOrder));
        when(orderItemRepository.saveAll(anyList())).thenReturn(Flux.just(orderItem1, orderItem2));
        when(cartItemRepository.deleteAll(anyList())).thenReturn(Mono.empty());

        // when
        Mono<OrderDto> result = orderService.getOrCreateOrder(cartId, true);

        // then
        StepVerifier.create(result)
                .expectNextMatches(dto -> dto.id() == 100L &&
                        dto.totalSum() == 5000L &&
                        dto.items().size() == 2 &&
                        dto.items().contains(dto1) &&
                        dto.items().contains(dto2))
                .verifyComplete();

        verify(cartItemRepository).findByCartId(cartId);
        verify(itemRepository, times(4)).findById(anyLong());
        verify(orderRepository).save(orderCaptor.capture());
        Order capturedOrder = orderCaptor.getValue();
        assertEquals(5000L, capturedOrder.getTotalSum());

        verify(orderItemRepository).saveAll(orderItemsCaptor.capture());
        List<OrderItem> savedOrderItems = orderItemsCaptor.getValue();
        assertEquals(2, savedOrderItems.size());
        assertEquals(3, savedOrderItems.get(0).getCount());
        assertEquals(1, savedOrderItems.get(1).getCount());

        verify(cartItemRepository).deleteAll(List.of(cartItem1, cartItem2));
    }

    @Test
    void getOrCreateOrder_newOrderWithEmptyCart_createsEmptyOrder() {
        // given
        long cartId = 999L;
        when(cartItemRepository.findByCartId(cartId)).thenReturn(Flux.empty());
        Order savedOrder = new Order(100L, 0L);
        when(orderRepository.save(any(Order.class))).thenReturn(Mono.just(savedOrder));

        // when
        Mono<OrderDto> result = orderService.getOrCreateOrder(cartId, true);

        // then
        StepVerifier.create(result)
                .expectNextMatches(dto -> dto.id() == 100L &&
                        dto.totalSum() == 0L &&
                        dto.items().isEmpty())
                .verifyComplete();

        verify(cartItemRepository).findByCartId(cartId);
        verify(orderRepository).save(orderCaptor.capture());
        Order capturedOrder = orderCaptor.getValue();
        assertEquals(0L, capturedOrder.getTotalSum());
    }
}