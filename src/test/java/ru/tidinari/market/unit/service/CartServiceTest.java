package ru.tidinari.market.unit.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.tidinari.market.domain.Cart;
import ru.tidinari.market.domain.CartItem;
import ru.tidinari.market.domain.Item;
import ru.tidinari.market.repository.CartItemRepository;
import ru.tidinari.market.repository.CartRepository;
import ru.tidinari.market.repository.ItemRepository;
import ru.tidinari.market.service.CartService;
import ru.tidinari.market.service.ImageService;
import ru.tidinari.market.web.dto.ActionTypeDto;
import ru.tidinari.market.web.dto.ItemDto;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class CartServiceTest {

    @Mock
    private CartRepository cartRepository;

    @Mock
    private CartItemRepository cartItemRepository;

    @Mock
    private ItemRepository itemRepository;

    @Mock
    private ImageService imageService;

    @InjectMocks
    private CartService cartService;

    @Test
    void getCartItems_EmptyCart_ReturnsEmptyList() {
        // given
        Cart cart = new Cart();
        cart.setId(1L);
        when(cartRepository.findById(1L)).thenReturn(Optional.of(cart));
        when(cartItemRepository.findByCartId(1L)).thenReturn(List.of());

        // when
        List<ItemDto> result = cartService.getCartItems();

        // then
        assertTrue(result.isEmpty());
        verify(cartRepository).findById(1L);
        verify(cartItemRepository).findByCartId(1L);
    }

    @Test
    void getCartItems_WithItems_ReturnsItemDtos() {
        // given
        Cart cart = new Cart();
        cart.setId(1L);
        Item item = new Item(10L, "Item1", "Desc1", 1000L, null);
        CartItem cartItem = new CartItem();
        cartItem.setCart(cart);
        cartItem.setItem(item);
        cartItem.setCount(2);

        when(cartRepository.findById(1L)).thenReturn(Optional.of(cart));
        when(cartItemRepository.findByCartId(1L)).thenReturn(List.of(cartItem));
        when(imageService.getImageUrl(10L)).thenReturn("/img1.jpg");

        // when
        List<ItemDto> result = cartService.getCartItems();

        // then
        assertEquals(1, result.size());
        ItemDto dto = result.get(0);
        assertEquals(10L, dto.id());
        assertEquals("Item1", dto.title());
        assertEquals(1000L, dto.price());
        assertEquals(2, dto.count());
        verify(cartRepository).findById(1L);
        verify(cartItemRepository).findByCartId(1L);
        verify(imageService).getImageUrl(10L);
    }

    @Test
    void getCartItems_CartNotExists_CreatesNewCart() {
        // given
        when(cartRepository.findById(1L)).thenReturn(Optional.empty());
        // Имитируем сохранение корзины с присвоением id
        doAnswer(invocation -> {
            Cart cart = invocation.getArgument(0);
            cart.setId(1L);
            return cart;
        }).when(cartRepository).save(any(Cart.class));
        when(cartItemRepository.findByCartId(1L)).thenReturn(List.of());

        // when
        List<ItemDto> result = cartService.getCartItems();

        // then
        assertTrue(result.isEmpty());
        verify(cartRepository).findById(1L);
        verify(cartRepository).save(any(Cart.class));
        verify(cartItemRepository).findByCartId(1L);
    }

    @Test
    void getTotal_EmptyCart_ReturnsZero() {
        // given
        Cart cart = new Cart();
        cart.setId(1L);
        when(cartRepository.findById(1L)).thenReturn(Optional.of(cart));
        when(cartItemRepository.findByCartId(1L)).thenReturn(List.of());

        // when
        int total = cartService.getTotal();

        // then
        assertEquals(0, total);
    }

    @Test
    void getTotal_WithItems_ReturnsCorrectSum() {
        // given
        Cart cart = new Cart();
        cart.setId(1L);
        Item item1 = new Item(10L, "Item1", "Desc1", 1000L, null);
        Item item2 = new Item(20L, "Item2", "Desc2", 500L, null);
        CartItem cartItem1 = new CartItem();
        cartItem1.setCart(cart);
        cartItem1.setItem(item1);
        cartItem1.setCount(2);
        CartItem cartItem2 = new CartItem();
        cartItem2.setCart(cart);
        cartItem2.setItem(item2);
        cartItem2.setCount(3);

        when(cartRepository.findById(1L)).thenReturn(Optional.of(cart));
        when(cartItemRepository.findByCartId(1L)).thenReturn(List.of(cartItem1, cartItem2));
        when(imageService.getImageUrl(10L)).thenReturn("/img1.jpg");
        when(imageService.getImageUrl(20L)).thenReturn("/img2.jpg");

        // when
        int total = cartService.getTotal();

        // then
        // (1000 * 2) + (500 * 3) = 2000 + 1500 = 3500
        assertEquals(3500, total);
        verify(imageService).getImageUrl(10L);
        verify(imageService).getImageUrl(20L);
    }

    @Test
    void actOnCartItems_Plus_NewItem_AddsItem() {
        // given
        Cart cart = new Cart();
        cart.setId(1L);
        Item item = new Item(10L, "Item1", "Desc1", 1000L, null);
        when(cartRepository.findById(1L)).thenReturn(Optional.of(cart));
        when(cartItemRepository.findByCartIdAndItemId(1L, 10L)).thenReturn(null);
        when(itemRepository.findById(10L)).thenReturn(Optional.of(item));
        when(cartItemRepository.save(any(CartItem.class))).thenAnswer(inv -> inv.getArgument(0));
        when(cartItemRepository.findByCartId(1L)).thenReturn(List.of());

        // when
        List<ItemDto> result = cartService.actOnCartItems(10L, ActionTypeDto.PLUS);

        // then
        verify(cartItemRepository).save(any(CartItem.class));
        verify(cartItemRepository, never()).delete(any());
        assertTrue(result.isEmpty()); // потому что после добавления мы вызываем getCartItems, который возвращает пустой список (так как мок вернул пустой)
    }

    @Test
    void actOnCartItems_Plus_ExistingItem_IncreasesCount() {
        // given
        Cart cart = new Cart();
        cart.setId(1L);
        Item item = new Item(10L, "Item1", "Desc1", 1000L, null);
        CartItem existing = new CartItem();
        existing.setCart(cart);
        existing.setItem(item);
        existing.setCount(2);
        when(cartRepository.findById(1L)).thenReturn(Optional.of(cart));
        when(cartItemRepository.findByCartIdAndItemId(1L, 10L)).thenReturn(existing);
        when(cartItemRepository.save(existing)).thenReturn(existing);
        when(cartItemRepository.findByCartId(1L)).thenReturn(List.of(existing));
        when(imageService.getImageUrl(10L)).thenReturn("/img1.jpg");

        // when
        List<ItemDto> result = cartService.actOnCartItems(10L, ActionTypeDto.PLUS);

        // then
        assertEquals(3, existing.getCount());
        verify(cartItemRepository).save(existing);
        verify(cartItemRepository, never()).delete(any());
        verify(imageService).getImageUrl(10L);
    }

    @Test
    void actOnCartItems_Minus_CountMoreThanOne_DecreasesCount() {
        // given
        Cart cart = new Cart();
        cart.setId(1L);
        Item item = new Item(10L, "Item1", "Desc1", 1000L, null);
        CartItem existing = new CartItem();
        existing.setCart(cart);
        existing.setItem(item);
        existing.setCount(2);
        when(cartRepository.findById(1L)).thenReturn(Optional.of(cart));
        when(cartItemRepository.findByCartIdAndItemId(1L, 10L)).thenReturn(existing);
        when(cartItemRepository.save(existing)).thenReturn(existing);
        when(cartItemRepository.findByCartId(1L)).thenReturn(List.of(existing));
        when(imageService.getImageUrl(10L)).thenReturn("/img1.jpg");

        // when
        List<ItemDto> result = cartService.actOnCartItems(10L, ActionTypeDto.MINUS);

        // then
        assertEquals(1, existing.getCount());
        verify(cartItemRepository).save(existing);
        verify(cartItemRepository, never()).delete(any());
        verify(imageService).getImageUrl(10L);
    }

    @Test
    void actOnCartItems_Minus_CountEqualsOne_DeletesItem() {
        // given
        Cart cart = new Cart();
        cart.setId(1L);
        Item item = new Item(10L, "Item1", "Desc1", 1000L, null);
        CartItem existing = new CartItem();
        existing.setCart(cart);
        existing.setItem(item);
        existing.setCount(1);
        when(cartRepository.findById(1L)).thenReturn(Optional.of(cart));
        when(cartItemRepository.findByCartIdAndItemId(1L, 10L)).thenReturn(existing);
        when(cartItemRepository.findByCartId(1L)).thenReturn(List.of());

        // when
        List<ItemDto> result = cartService.actOnCartItems(10L, ActionTypeDto.MINUS);

        // then
        verify(cartItemRepository).delete(existing);
        verify(cartItemRepository, never()).save(any());
    }

    @Test
    void actOnCartItems_Delete_ExistingItem_DeletesItem() {
        // given
        Cart cart = new Cart();
        cart.setId(1L);
        Item item = new Item(10L, "Item1", "Desc1", 1000L, null);
        CartItem existing = new CartItem();
        existing.setCart(cart);
        existing.setItem(item);
        existing.setCount(5);
        when(cartRepository.findById(1L)).thenReturn(Optional.of(cart));
        when(cartItemRepository.findByCartIdAndItemId(1L, 10L)).thenReturn(existing);
        when(cartItemRepository.findByCartId(1L)).thenReturn(List.of());

        // when
        List<ItemDto> result = cartService.actOnCartItems(10L, ActionTypeDto.DELETE);

        // then
        verify(cartItemRepository).delete(existing);
        verify(cartItemRepository, never()).save(any());
    }

    @Test
    void actOnCartItems_Minus_NoItem_DoesNothing() {
        // given
        Cart cart = new Cart();
        cart.setId(1L);
        when(cartRepository.findById(1L)).thenReturn(Optional.of(cart));
        when(cartItemRepository.findByCartIdAndItemId(1L, 10L)).thenReturn(null);
        when(cartItemRepository.findByCartId(1L)).thenReturn(List.of());

        // when
        List<ItemDto> result = cartService.actOnCartItems(10L, ActionTypeDto.MINUS);

        // then
        verify(cartItemRepository, never()).save(any());
        verify(cartItemRepository, never()).delete(any());
        assertTrue(result.isEmpty());
    }

    @Test
    void actOnCartItems_Delete_NoItem_DoesNothing() {
        // given
        Cart cart = new Cart();
        cart.setId(1L);
        when(cartRepository.findById(1L)).thenReturn(Optional.of(cart));
        when(cartItemRepository.findByCartIdAndItemId(1L, 10L)).thenReturn(null);
        when(cartItemRepository.findByCartId(1L)).thenReturn(List.of());

        // when
        List<ItemDto> result = cartService.actOnCartItems(10L, ActionTypeDto.DELETE);

        // then
        verify(cartItemRepository, never()).save(any());
        verify(cartItemRepository, never()).delete(any());
        assertTrue(result.isEmpty());
    }
    @Test
    void getItemCounts_EmptyCart_ReturnsZeroForAllIds() {
        // given
        Cart cart = new Cart();
        cart.setId(1L);
        when(cartRepository.findById(1L)).thenReturn(Optional.of(cart));
        when(cartItemRepository.findByCartIdAndItemIdIn(1L, List.of(10L, 20L, 30L)))
                .thenReturn(List.of());

        // when
        Map<Long, Integer> counts = cartService.getItemCounts(List.of(10L, 20L, 30L));

        // then
        assertEquals(0, counts.get(10L));
        assertEquals(0, counts.get(20L));
        assertEquals(0, counts.get(30L));
        verify(cartRepository).findById(1L);
        verify(cartItemRepository).findByCartIdAndItemIdIn(1L, List.of(10L, 20L, 30L));
    }

    @Test
    void getItemCounts_WithItems_ReturnsCorrectCounts() {
        // given
        Cart cart = new Cart();
        cart.setId(1L);
        Item item1 = new Item(10L, "Item1", "Desc1", 1000L, null);
        Item item2 = new Item(20L, "Item2", "Desc2", 500L, null);
        CartItem cartItem1 = new CartItem();
        cartItem1.setCart(cart);
        cartItem1.setItem(item1);
        cartItem1.setCount(2);
        CartItem cartItem2 = new CartItem();
        cartItem2.setCart(cart);
        cartItem2.setItem(item2);
        cartItem2.setCount(5);
        when(cartRepository.findById(1L)).thenReturn(Optional.of(cart));
        when(cartItemRepository.findByCartIdAndItemIdIn(1L, List.of(10L, 20L, 30L)))
                .thenReturn(List.of(cartItem1, cartItem2));

        // when
        Map<Long, Integer> counts = cartService.getItemCounts(List.of(10L, 20L, 30L));

        // then
        assertEquals(2, counts.get(10L));
        assertEquals(5, counts.get(20L));
        assertEquals(0, counts.get(30L)); // отсутствует в корзине
        verify(cartRepository).findById(1L);
        verify(cartItemRepository).findByCartIdAndItemIdIn(1L, List.of(10L, 20L, 30L));
    }
}