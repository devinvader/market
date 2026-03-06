package ru.tidinari.market.unit.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.tidinari.market.domain.*;
import ru.tidinari.market.repository.*;
import ru.tidinari.market.service.ImageService;
import ru.tidinari.market.service.OrderService;
import ru.tidinari.market.web.dto.ItemDto;
import ru.tidinari.market.web.dto.OrderDto;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private OrderItemRepository orderItemRepository;

    @Mock
    private CartRepository cartRepository;

    @Mock
    private CartItemRepository cartItemRepository;

    @Mock
    private ImageService imageService;

    @InjectMocks
    private OrderService orderService;

    @Test
    void getOrders_NoOrders_ReturnsEmptyList() {
        // given
        when(orderRepository.findAll()).thenReturn(List.of());

        // when
        List<OrderDto> result = orderService.getOrders();

        // then
        assertTrue(result.isEmpty());
        verify(orderRepository).findAll();
        verifyNoInteractions(orderItemRepository);
    }

    @Test
    void getOrders_WithOrders_ReturnsOrderDtos() {
        // given
        Order order = new Order();
        order.setId(1L);
        order.setTotalSum(5000L);

        Item item1 = new Item(10L, "Item1", "Desc1", 1000L, null);
        Item item2 = new Item(20L, "Item2", "Desc2", 2000L, null);
        OrderItem orderItem1 = new OrderItem(order, item1, 2);
        OrderItem orderItem2 = new OrderItem(order, item2, 1);

        when(orderRepository.findAll()).thenReturn(List.of(order));
        when(orderItemRepository.findByOrderId(1L)).thenReturn(List.of(orderItem1, orderItem2));
        when(imageService.getImageUrl(10L)).thenReturn("/items/10/image");
        when(imageService.getImageUrl(20L)).thenReturn("/items/20/image");

        // when
        List<OrderDto> result = orderService.getOrders();

        // then
        assertEquals(1, result.size());
        OrderDto dto = result.get(0);
        assertEquals(1L, dto.id());
        assertEquals(5000L, dto.totalSum());
        assertEquals(2, dto.items().size());

        ItemDto dto1 = dto.items().get(0);
        assertEquals(10L, dto1.id());
        assertEquals(1000L, dto1.price());
        assertEquals(2, dto1.count());

        ItemDto dto2 = dto.items().get(1);
        assertEquals(20L, dto2.id());
        assertEquals(2000L, dto2.price());
        assertEquals(1, dto2.count());

        verify(orderRepository).findAll();
        verify(orderItemRepository).findByOrderId(1L);
        verify(imageService).getImageUrl(10L);
        verify(imageService).getImageUrl(20L);
    }

    @Test
    void getOrder_ExistingOrder_ReturnsOrderDto() {
        // given
        Order order = new Order();
        order.setId(5L);
        order.setTotalSum(3000L);
        Item item = new Item(30L, "Item3", "Desc3", 1500L, null);
        OrderItem orderItem = new OrderItem(order, item, 2);

        when(orderRepository.findById(5L)).thenReturn(Optional.of(order));
        when(orderItemRepository.findByOrderId(5L)).thenReturn(List.of(orderItem));
        when(imageService.getImageUrl(30L)).thenReturn("/items/30/image");

        // when
        OrderDto result = orderService.getOrder(5L, false);

        // then
        assertEquals(5L, result.id());
        assertEquals(3000L, result.totalSum());
        assertEquals(1, result.items().size());
        ItemDto dto = result.items().get(0);
        assertEquals(30L, dto.id());
        assertEquals(1500L, dto.price());
        assertEquals(2, dto.count());

        verify(orderRepository).findById(5L);
        verify(orderItemRepository).findByOrderId(5L);
        verify(imageService).getImageUrl(30L);
        verifyNoInteractions(cartRepository, cartItemRepository);
    }

    @Test
    void getOrder_ExistingOrderNotFound_ThrowsException() {
        // given
        when(orderRepository.findById(99L)).thenReturn(Optional.empty());

        // when & then
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> orderService.getOrder(99L, false));
        assertEquals("Order not found", exception.getMessage());
        verify(orderRepository).findById(99L);
        verifyNoInteractions(orderItemRepository);
    }

    @Test
    void getOrder_NewOrder_CreatesOrderFromCart() {
        // given
        long cartId = 1L;
        Cart cart = new Cart();
        cart.setId(cartId);
        Item item1 = new Item(10L, "Item1", "Desc1", 1000L, null);
        Item item2 = new Item(20L, "Item2", "Desc2", 2000L, null);
        CartItem cartItem1 = new CartItem(cart, item1, 3);
        CartItem cartItem2 = new CartItem(cart, item2, 1);

        when(cartItemRepository.findByCartId(cartId)).thenReturn(List.of(cartItem1, cartItem2));
        when(orderRepository.save(any(Order.class))).thenAnswer(inv -> {
            Order o = inv.getArgument(0);
            o.setId(100L);
            return o;
        });
        when(orderItemRepository.save(any(OrderItem.class))).thenAnswer(inv -> inv.getArgument(0));
        when(imageService.getImageUrl(10L)).thenReturn("/items/10/image");
        when(imageService.getImageUrl(20L)).thenReturn("/items/20/image");

        // when
        OrderDto result = orderService.getOrder(cartId, true);

        // then
        assertEquals(100L, result.id());
        // total sum = (1000 * 3) + (2000 * 1) = 3000 + 2000 = 5000
        assertEquals(5000L, result.totalSum());
        assertEquals(2, result.items().size());

        // verify cart items were deleted
        verify(cartItemRepository).deleteAll(List.of(cartItem1, cartItem2));
        // verify order and order items saved
        verify(orderRepository).save(any(Order.class));
        verify(orderItemRepository, times(2)).save(any(OrderItem.class));
        verify(cartItemRepository).findByCartId(cartId);
        verify(imageService).getImageUrl(10L);
        verify(imageService).getImageUrl(20L);
        verifyNoInteractions(cartRepository);
    }

    @Test
    void getOrder_NewOrder_CartNotFound_ThrowsException() {
        // given
        long cartId = 999L;
        when(cartItemRepository.findByCartId(cartId)).thenReturn(List.of());
        when(orderRepository.save(any(Order.class))).thenAnswer(inv -> {
            Order o = inv.getArgument(0);
            o.setId(100L);
            return o;
        });

        // when
        OrderDto result = orderService.getOrder(cartId, true);

        // then
        assertEquals(100L, result.id());
        assertEquals(0L, result.totalSum());
        assertTrue(result.items().isEmpty());
        verify(cartItemRepository).findByCartId(cartId);
        verify(orderRepository).save(any(Order.class));
        verifyNoInteractions(cartRepository, imageService);
    }

    @Test
    void getOrder_NewOrder_EmptyCart_ReturnsOrderWithZeroTotal() {
        // given
        long cartId = 1L;
        when(cartItemRepository.findByCartId(cartId)).thenReturn(List.of());
        when(orderRepository.save(any(Order.class))).thenAnswer(inv -> {
            Order o = inv.getArgument(0);
            o.setId(100L);
            return o;
        });

        // when
        OrderDto result = orderService.getOrder(cartId, true);

        // then
        assertEquals(100L, result.id());
        assertEquals(0L, result.totalSum());
        assertTrue(result.items().isEmpty());
        verify(cartItemRepository).deleteAll(List.of());
        verify(orderRepository).save(any(Order.class));
        verify(orderItemRepository, never()).save(any());
        verifyNoInteractions(cartRepository, imageService);
    }
}