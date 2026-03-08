package ru.tidinari.market.unit.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.tidinari.market.domain.*;
import ru.tidinari.market.mapper.OrderItemMapper;
import ru.tidinari.market.repository.*;
import ru.tidinari.market.service.ImageService;
import ru.tidinari.market.service.OrderService;
import ru.tidinari.market.web.dto.ItemDto;
import ru.tidinari.market.web.dto.OrderDto;
import ru.tidinari.market.mapper.ItemMapper;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
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
    @Spy
    private OrderItemMapper orderItemMapper = new OrderItemMapper();
    @Spy
    private ItemMapper itemMapper = new ItemMapper();
    @InjectMocks
    private OrderService orderService;

    @Captor
    private ArgumentCaptor<Order> orderCaptor;
    @Captor
    private ArgumentCaptor<Iterable<OrderItem>> orderItemCaptor;

    @Test
    void getOrCreateOrders_noOrders_returnsEmptyList() {
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
    void getOrders_withOrders_returnsOrCreateOrderDtos() {
        Order order = new Order();
        order.setId(1L);
        order.setTotalSum(5000L);

        Item item1 = new Item(10L, "Item1", "Desc1", 1000L, null);
        Item item2 = new Item(20L, "Item2", "Desc2", 2000L, null);
        OrderItem orderItem1 = new OrderItem(order, item1, 2);
        OrderItem orderItem2 = new OrderItem(order, item2, 1);

        when(orderRepository.findAll()).thenReturn(List.of(order));
        when(orderItemRepository.findByOrderId(1L)).thenReturn(List.of(orderItem1, orderItem2));

        List<OrderDto> result = orderService.getOrders();

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
    }

    @Test
    void getOrder_existingOrder_returnsOrCreateOrderDto() {
        Order order = new Order();
        order.setId(5L);
        order.setTotalSum(3000L);
        Item item = new Item(30L, "Item3", "Desc3", 1500L, null);
        OrderItem orderItem = new OrderItem(order, item, 2);

        when(orderRepository.findById(5L)).thenReturn(Optional.of(order));
        when(orderItemRepository.findByOrderId(5L)).thenReturn(List.of(orderItem));

        OrderDto result = orderService.getOrCreateOrder(5L, false);

        assertEquals(5L, result.id());
        assertEquals(3000L, result.totalSum());
        assertEquals(1, result.items().size());
        ItemDto dto = result.items().get(0);
        assertEquals(30L, dto.id());
        assertEquals(1500L, dto.price());
        assertEquals(2, dto.count());

        verify(orderRepository).findById(5L);
        verify(orderItemRepository).findByOrderId(5L);
        verifyNoInteractions(cartRepository, cartItemRepository);
    }

    @Test
    void getOrder_existingOrCreateOrderNotFound_throwsException() {
        when(orderRepository.findById(99L)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> orderService.getOrCreateOrder(99L, false));
        assertEquals("Order not found", exception.getMessage());
        verify(orderRepository).findById(99L);
        verifyNoInteractions(orderItemRepository);
    }

    @Test
    void getOrder_newOrder_createsOrCreateOrderFromCart() {
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
        when(orderItemRepository.saveAll(any(Iterable.class))).thenAnswer(inv -> inv.getArgument(0));

        OrderDto result = orderService.getOrCreateOrder(cartId, true);

        assertEquals(100L, result.id());
        assertEquals(1000 * 3 + 2000, result.totalSum());
        assertEquals(2, result.items().size());

        verify(cartItemRepository).deleteAll(List.of(cartItem1, cartItem2));
        verify(orderRepository).save(orderCaptor.capture());
        assertEquals(5000L, orderCaptor.getValue().getTotalSum());

        verify(orderItemRepository).saveAll(orderItemCaptor.capture());
        List<OrderItem> savedItems = (List<OrderItem>) orderItemCaptor.getValue();
        assertEquals(2, savedItems.size());
        assertEquals(3, savedItems.get(0).getCount());
        assertEquals(1, savedItems.get(1).getCount());

        verify(cartItemRepository).findByCartId(cartId);
        verifyNoInteractions(cartRepository);
    }

    @Test
    void getOrder_newOrder_noCartItems_returnsOrCreateOrderWithZeroTotal() {
        long cartId = 999L;
        when(cartItemRepository.findByCartId(cartId)).thenReturn(List.of());
        when(orderRepository.save(any(Order.class))).thenAnswer(inv -> {
            Order o = inv.getArgument(0);
            o.setId(100L);
            return o;
        });

        OrderDto result = orderService.getOrCreateOrder(cartId, true);

        assertEquals(100L, result.id());
        assertEquals(0L, result.totalSum());
        assertTrue(result.items().isEmpty());
        verify(cartItemRepository).findByCartId(cartId);
        verify(orderRepository).save(orderCaptor.capture());
        assertEquals(0L, orderCaptor.getValue().getTotalSum());
        verify(orderItemRepository, never()).save(any());
        verifyNoInteractions(cartRepository, imageService);
    }
}