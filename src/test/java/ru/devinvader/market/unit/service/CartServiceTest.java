package ru.devinvader.market.unit.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.devinvader.market.domain.Cart;
import ru.devinvader.market.domain.CartItem;
import ru.devinvader.market.domain.Item;
import ru.devinvader.market.repository.CartItemRepository;
import ru.devinvader.market.repository.CartRepository;
import ru.devinvader.market.repository.ItemRepository;
import ru.devinvader.market.service.CartService;
import ru.devinvader.market.service.ImageService;
import ru.devinvader.market.web.dto.ActionTypeDto;
import ru.devinvader.market.web.dto.ItemDto;
import ru.devinvader.market.mapper.ItemMapper;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
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
    @Spy
    private ItemMapper itemMapper = new ItemMapper();

    @InjectMocks
    private CartService cartService;

    @Captor
    private ArgumentCaptor<Cart> cartCaptor;
    @Captor
    private ArgumentCaptor<CartItem> cartItemCaptor;

    private final Long CART_ID = 1L;

    @Test
    void getCartItems_emptyCart_returnsEmptyList() {
        // given
        Cart cart = new Cart();
        cart.setId(CART_ID);
        when(cartRepository.findById(CART_ID)).thenReturn(Optional.of(cart));
        when(cartItemRepository.findByCartId(CART_ID)).thenReturn(List.of());

        // when
        List<ItemDto> result = cartService.getCartItems();

        // then
        assertTrue(result.isEmpty());
        verify(cartRepository).findById(CART_ID);
        verify(cartItemRepository).findByCartId(CART_ID);
    }

    @Test
    void getCartItems_withItems_returnsItemDtos() {
        // given
        Cart cart = new Cart();
        cart.setId(CART_ID);
        Item item = new Item(10L, "Item1", "Desc1", 1000L, null);
        CartItem cartItem = new CartItem();
        cartItem.setCart(cart);
        cartItem.setItem(item);
        cartItem.setCount(2);

        when(cartRepository.findById(CART_ID)).thenReturn(Optional.of(cart));
        when(cartItemRepository.findByCartId(CART_ID)).thenReturn(List.of(cartItem));

        // when
        List<ItemDto> result = cartService.getCartItems();

        // then
        assertEquals(1, result.size());
        ItemDto dto = result.get(0);
        assertEquals(10L, dto.id());
        assertEquals("Item1", dto.title());
        assertEquals(1000L, dto.price());
        assertEquals(2, dto.count());
    }

    @Test
    void getCartItems_cartNotExists_createsNewCart() {
        // given
        when(cartRepository.findById(CART_ID)).thenReturn(Optional.empty());
        doAnswer(inv -> {
            Cart cart = inv.getArgument(0);
            cart.setId(CART_ID);
            return null;
        }).when(cartRepository).save(any(Cart.class));
        when(cartItemRepository.findByCartId(CART_ID)).thenReturn(List.of());

        // when
        List<ItemDto> result = cartService.getCartItems();

        // then
        assertTrue(result.isEmpty());
        verify(cartRepository).save(cartCaptor.capture());
        assertEquals(CART_ID, cartCaptor.getValue().getId());
    }

    @Test
    void getTotal_emptyCart_returnsZero() {
        // given
        Cart cart = new Cart();
        cart.setId(CART_ID);
        when(cartRepository.findById(CART_ID)).thenReturn(Optional.of(cart));
        when(cartItemRepository.findByCartId(CART_ID)).thenReturn(List.of());

        // when
        int total = cartService.getTotal();

        // then
        assertEquals(0, total);
    }

    @Test
    void getTotal_withItems_returnsCorrectSum() {
        // given
        Cart cart = new Cart();
        cart.setId(CART_ID);
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

        when(cartRepository.findById(CART_ID)).thenReturn(Optional.of(cart));
        when(cartItemRepository.findByCartId(CART_ID)).thenReturn(List.of(cartItem1, cartItem2));

        // when
        int total = cartService.getTotal();

        // then
        assertEquals(1000 * 2 + 500 * 3, total);
    }

    @Test
    void actOnCartItems_plus_newItem_addsItem() {
        // given
        Cart cart = new Cart();
        cart.setId(CART_ID);
        Item item = new Item(10L, "Item1", "Desc1", 1000L, null);

        when(cartRepository.findById(CART_ID)).thenReturn(Optional.of(cart));
        when(cartItemRepository.findByCartIdAndItemId(CART_ID, 10L)).thenReturn(null);
        when(itemRepository.findById(10L)).thenReturn(Optional.of(item));
        when(cartItemRepository.save(any(CartItem.class))).thenAnswer(inv -> inv.getArgument(0));
        when(cartItemRepository.findByCartId(CART_ID)).thenReturn(List.of()); // для getCartItems

        // when
        List<ItemDto> result = cartService.actOnCartItems(10L, ActionTypeDto.PLUS);

        // then
        verify(cartItemRepository).save(cartItemCaptor.capture());
        CartItem saved = cartItemCaptor.getValue();
        assertEquals(cart, saved.getCart());
        assertEquals(item, saved.getItem());
        assertEquals(1, saved.getCount());
        verify(cartItemRepository, never()).delete(any());
        assertTrue(result.isEmpty()); // потому что мок вернул пустой список для getCartItems
    }

    @Test
    void actOnCartItems_plus_existingItem_increasesCount() {
        // given
        Cart cart = new Cart();
        cart.setId(CART_ID);
        Item item = new Item(10L, "Item1", "Desc1", 1000L, null);
        CartItem existing = new CartItem();
        existing.setCart(cart);
        existing.setItem(item);
        existing.setCount(2);

        when(cartRepository.findById(CART_ID)).thenReturn(Optional.of(cart));
        when(cartItemRepository.findByCartIdAndItemId(CART_ID, 10L)).thenReturn(existing);
        when(cartItemRepository.save(existing)).thenReturn(existing);
        when(cartItemRepository.findByCartId(CART_ID)).thenReturn(List.of(existing));

        // when
        List<ItemDto> result = cartService.actOnCartItems(10L, ActionTypeDto.PLUS);

        // then
        assertEquals(3, existing.getCount());
        verify(cartItemRepository).save(existing);
        assertEquals(1, result.size());
    }

    @Test
    void actOnCartItems_plus_itemNotFound_throwsException() {
        // given
        Cart cart = new Cart();
        cart.setId(CART_ID);
        when(cartRepository.findById(CART_ID)).thenReturn(Optional.of(cart));
        when(cartItemRepository.findByCartIdAndItemId(CART_ID, 10L)).thenReturn(null);
        when(itemRepository.findById(10L)).thenReturn(Optional.empty());

        // when
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> cartService.actOnCartItems(10L, ActionTypeDto.PLUS));

        // then
        assertEquals("Item not found", exception.getMessage());
        verify(cartItemRepository, never()).save(any());
    }

    @Test
    void actOnCartItems_cartNotExists_createsNewCartAndAddsItem() {
        // given
        long cartId = 1L;
        Cart savedCart = new Cart();
        savedCart.setId(cartId);

        when(cartRepository.findById(cartId))
                .thenReturn(Optional.empty()) // первый вызов (в actOnCartItems)
                .thenReturn(Optional.of(savedCart)); // второй вызов (в getCartItems)

        doAnswer(inv -> {
            Cart cart = inv.getArgument(0);
            cart.setId(cartId); // имитация присвоения ID при сохранении
            return null;
        }).when(cartRepository).save(any(Cart.class));

        Item item = new Item(10L, "Item1", "Desc1", 1000L, null);
        when(itemRepository.findById(10L)).thenReturn(Optional.of(item));
        when(cartItemRepository.findByCartIdAndItemId(cartId, 10L)).thenReturn(null);
        when(cartItemRepository.save(any(CartItem.class))).thenAnswer(inv -> inv.getArgument(0));
        when(cartItemRepository.findByCartId(cartId)).thenReturn(List.of()); // для getCartItems

        // when
        cartService.actOnCartItems(10L, ActionTypeDto.PLUS);

        // then
        verify(cartRepository, times(1)).save(any(Cart.class)); // теперь только один раз
        verify(cartItemRepository).save(any(CartItem.class));
        // остальные проверки
    }

    @Test
    void actOnCartItems_minus_countMoreThanOne_decreasesCount() {
        // given
        Cart cart = new Cart();
        cart.setId(CART_ID);
        Item item = new Item(10L, "Item1", "Desc1", 1000L, null);
        CartItem existing = new CartItem();
        existing.setCart(cart);
        existing.setItem(item);
        existing.setCount(2);

        when(cartRepository.findById(CART_ID)).thenReturn(Optional.of(cart));
        when(cartItemRepository.findByCartIdAndItemId(CART_ID, 10L)).thenReturn(existing);
        when(cartItemRepository.save(existing)).thenReturn(existing);
        when(cartItemRepository.findByCartId(CART_ID)).thenReturn(List.of(existing));

        // when
        List<ItemDto> result = cartService.actOnCartItems(10L, ActionTypeDto.MINUS);

        // then
        assertEquals(1, existing.getCount());
        verify(cartItemRepository).save(existing);
        verify(cartItemRepository, never()).delete(any());
        assertEquals(1, result.size());
    }

    @Test
    void actOnCartItems_minus_countEqualsOne_deletesItem() {
        // given
        Cart cart = new Cart();
        cart.setId(CART_ID);
        Item item = new Item(10L, "Item1", "Desc1", 1000L, null);
        CartItem existing = new CartItem();
        existing.setCart(cart);
        existing.setItem(item);
        existing.setCount(1);

        when(cartRepository.findById(CART_ID)).thenReturn(Optional.of(cart));
        when(cartItemRepository.findByCartIdAndItemId(CART_ID, 10L)).thenReturn(existing);
        when(cartItemRepository.findByCartId(CART_ID)).thenReturn(List.of());

        // when
        List<ItemDto> result = cartService.actOnCartItems(10L, ActionTypeDto.MINUS);

        // then
        verify(cartItemRepository).delete(existing);
        verify(cartItemRepository, never()).save(any());
        assertTrue(result.isEmpty());
    }

    @Test
    void actOnCartItems_delete_existingItem_deletesItem() {
        // given
        Cart cart = new Cart();
        cart.setId(CART_ID);
        Item item = new Item(10L, "Item1", "Desc1", 1000L, null);
        CartItem existing = new CartItem();
        existing.setCart(cart);
        existing.setItem(item);
        existing.setCount(5);

        when(cartRepository.findById(CART_ID)).thenReturn(Optional.of(cart));
        when(cartItemRepository.findByCartIdAndItemId(CART_ID, 10L)).thenReturn(existing);
        when(cartItemRepository.findByCartId(CART_ID)).thenReturn(List.of());

        // when
        List<ItemDto> result = cartService.actOnCartItems(10L, ActionTypeDto.DELETE);

        // then
        verify(cartItemRepository).delete(existing);
        verify(cartItemRepository, never()).save(any());
        assertTrue(result.isEmpty());
    }

    @Test
    void actOnCartItems_minus_noItem_doesNothing() {
        // given
        Cart cart = new Cart();
        cart.setId(CART_ID);
        when(cartRepository.findById(CART_ID)).thenReturn(Optional.of(cart));
        when(cartItemRepository.findByCartIdAndItemId(CART_ID, 10L)).thenReturn(null);
        when(cartItemRepository.findByCartId(CART_ID)).thenReturn(List.of());

        // when
        List<ItemDto> result = cartService.actOnCartItems(10L, ActionTypeDto.MINUS);

        // then
        verify(cartItemRepository, never()).save(any());
        verify(cartItemRepository, never()).delete(any());
        assertTrue(result.isEmpty());
    }

    @Test
    void actOnCartItems_delete_noItem_doesNothing() {
        // given
        Cart cart = new Cart();
        cart.setId(CART_ID);
        when(cartRepository.findById(CART_ID)).thenReturn(Optional.of(cart));
        when(cartItemRepository.findByCartIdAndItemId(CART_ID, 10L)).thenReturn(null);
        when(cartItemRepository.findByCartId(CART_ID)).thenReturn(List.of());

        // when
        List<ItemDto> result = cartService.actOnCartItems(10L, ActionTypeDto.DELETE);

        // then
        verify(cartItemRepository, never()).save(any());
        verify(cartItemRepository, never()).delete(any());
        assertTrue(result.isEmpty());
    }

    @Test
    void getItemCounts_emptyCart_returnsZeroForAllIds() {
        // given
        Cart cart = new Cart();
        cart.setId(CART_ID);
        when(cartRepository.findById(CART_ID)).thenReturn(Optional.of(cart));
        when(cartItemRepository.findByCartIdAndItemIdIn(CART_ID, List.of(10L, 20L, 30L)))
                .thenReturn(List.of());

        // when
        Map<Long, Integer> counts = cartService.getItemCounts(List.of(10L, 20L, 30L));

        // then
        assertEquals(0, counts.get(10L));
        assertEquals(0, counts.get(20L));
        assertEquals(0, counts.get(30L));
        verify(cartRepository).findById(CART_ID);
    }

    @Test
    void getItemCounts_withItems_returnsCorrectCounts() {
        // given
        Cart cart = new Cart();
        cart.setId(CART_ID);
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

        when(cartRepository.findById(CART_ID)).thenReturn(Optional.of(cart));
        when(cartItemRepository.findByCartIdAndItemIdIn(CART_ID, List.of(10L, 20L, 30L)))
                .thenReturn(List.of(cartItem1, cartItem2));

        // when
        Map<Long, Integer> counts = cartService.getItemCounts(List.of(10L, 20L, 30L));

        // then
        assertEquals(2, counts.get(10L));
        assertEquals(5, counts.get(20L));
        assertEquals(0, counts.get(30L));
    }
}