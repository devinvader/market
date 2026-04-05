package ru.devinvader.market.unit.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import ru.devinvader.market.domain.Cart;
import ru.devinvader.market.domain.CartItem;
import ru.devinvader.market.domain.Item;
import ru.devinvader.market.mapper.ItemMapper;
import ru.devinvader.market.repository.CartItemRepository;
import ru.devinvader.market.repository.CartRepository;
import ru.devinvader.market.repository.ItemRepository;
import ru.devinvader.market.service.CartService;
import ru.devinvader.market.web.dto.ActionTypeDto;
import ru.devinvader.market.web.dto.ItemDto;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class CartServiceTest {

    @Mock
    private CartRepository cartRepository;

    @Mock
    private CartItemRepository cartItemRepository;

    @Mock
    private ItemRepository itemRepository;

    @Spy
    private ItemMapper itemMapper = new ItemMapper();

    @InjectMocks
    private CartService cartService;

    @Captor
    private ArgumentCaptor<Cart> cartCaptor;

    @Captor
    private ArgumentCaptor<CartItem> cartItemCaptor;

    private final long DEFAULT_CART_ID = 1L;

    @Test
    void getUserCart_givenCartExists_returnsExistingCart() {
        // given
        Cart existingCart = new Cart();
        existingCart.setId(DEFAULT_CART_ID);
        when(cartRepository.findById(DEFAULT_CART_ID)).thenReturn(Mono.just(existingCart));

        // when
        Mono<Cart> result = cartService.getUserCart();

        // then
        StepVerifier.create(result)
                .expectNext(existingCart)
                .verifyComplete();
        verify(cartRepository).findById(DEFAULT_CART_ID);
        verify(cartRepository, never()).save(any());
    }

    @Test
    void getUserCart_givenCartNotExists_createsAndReturnsNewCart() {
        // given
        when(cartRepository.findById(DEFAULT_CART_ID)).thenReturn(Mono.empty());
        Cart newCart = new Cart();
        newCart.setId(DEFAULT_CART_ID);
        when(cartRepository.save(any(Cart.class))).thenReturn(Mono.just(newCart));

        // when
        Mono<Cart> result = cartService.getUserCart();

        // then
        StepVerifier.create(result)
                .expectNextMatches(cart -> cart.getId() == DEFAULT_CART_ID)
                .verifyComplete();
        verify(cartRepository).findById(DEFAULT_CART_ID);
        verify(cartRepository).save(cartCaptor.capture());
    }

    @Test
    void getCartItems_givenEmptyCart_returnsEmptyFlux() {
        // given
        Cart cart = new Cart();
        cart.setId(DEFAULT_CART_ID);
        when(cartRepository.findById(DEFAULT_CART_ID)).thenReturn(Mono.just(cart));
        when(cartItemRepository.findByCartId(DEFAULT_CART_ID)).thenReturn(Flux.empty());
        when(itemRepository.findAllById(anyIterable())).thenReturn(Flux.empty());

        // when
        Flux<ItemDto> result = cartService.getCartItems();

        // then
        StepVerifier.create(result)
                .expectNextCount(0)
                .verifyComplete();
        verify(cartRepository).findById(DEFAULT_CART_ID);
        verify(cartItemRepository).findByCartId(DEFAULT_CART_ID);
        verify(itemRepository).findAllById(anyIterable());
    }

    @Test
    void getCartItems_givenItems_returnsItemDtos() {
        // given
        Cart cart = new Cart();
        cart.setId(DEFAULT_CART_ID);
        Item item1 = new Item(10L, "Item1", "Desc1", 1000L, null);
        Item item2 = new Item(20L, "Item2", "Desc2", 500L, null);
        CartItem cartItem1 = new CartItem(1L, DEFAULT_CART_ID, 10L, 2);
        CartItem cartItem2 = new CartItem(2L, DEFAULT_CART_ID, 20L, 3);
        ItemDto dto1 = new ItemDto(10L, "Item1", "Desc1", 1000L, 2);
        ItemDto dto2 = new ItemDto(20L, "Item2", "Desc2", 500L, 3);

        when(cartRepository.findById(DEFAULT_CART_ID)).thenReturn(Mono.just(cart));
        when(cartItemRepository.findByCartId(DEFAULT_CART_ID)).thenReturn(Flux.just(cartItem1, cartItem2));
        when(itemRepository.findAllById(anyIterable())).thenReturn(Flux.just(item1, item2));

        // when
        Flux<ItemDto> result = cartService.getCartItems();

        // then
        StepVerifier.create(result)
                .expectNext(dto1)
                .expectNext(dto2)
                .verifyComplete();
        verify(cartRepository).findById(DEFAULT_CART_ID);
        verify(cartItemRepository).findByCartId(DEFAULT_CART_ID);
        verify(itemRepository).findAllById(anyIterable());
    }

    @Test
    void getTotal_givenEmptyCart_returnsZero() {
        // given
        Cart cart = new Cart();
        cart.setId(DEFAULT_CART_ID);
        when(cartRepository.findById(DEFAULT_CART_ID)).thenReturn(Mono.just(cart));
        when(cartItemRepository.findByCartId(DEFAULT_CART_ID)).thenReturn(Flux.empty());
        when(itemRepository.findAllById(anyIterable())).thenReturn(Flux.empty());

        // when
        Mono<Long> result = cartService.getTotal();

        // then
        StepVerifier.create(result)
                .expectNext(0L)
                .verifyComplete();
    }

    @Test
    void getTotal_givenItems_returnsCorrectSum() {
        // given
        Cart cart = new Cart();
        cart.setId(DEFAULT_CART_ID);
        Item item1 = new Item(10L, "Item1", "Desc1", 1000L, null);
        Item item2 = new Item(20L, "Item2", "Desc2", 500L, null);
        CartItem cartItem1 = new CartItem(1L, DEFAULT_CART_ID, 10L, 2);
        CartItem cartItem2 = new CartItem(2L, DEFAULT_CART_ID, 20L, 3);

        when(cartRepository.findById(DEFAULT_CART_ID)).thenReturn(Mono.just(cart));
        when(cartItemRepository.findByCartId(DEFAULT_CART_ID)).thenReturn(Flux.just(cartItem1, cartItem2));
        when(itemRepository.findAllById(anyIterable())).thenReturn(Flux.just(item1, item2));

        // when
        Mono<Long> result = cartService.getTotal();

        // then
        StepVerifier.create(result)
                .expectNext(1000L * 2 + 500L * 3)
                .verifyComplete();
    }

    @Test
    void getItemCounts_givenEmptyCart_returnsZeroForAllIds() {
        // given
        Cart cart = new Cart();
        cart.setId(DEFAULT_CART_ID);
        List<Long> itemIds = List.of(10L, 20L, 30L);
        when(cartRepository.findById(DEFAULT_CART_ID)).thenReturn(Mono.just(cart));
        when(cartItemRepository.findByCartIdAndItemIdIn(DEFAULT_CART_ID, itemIds))
                .thenReturn(Flux.empty());

        // when
        Mono<Map<Long, Integer>> result = cartService.getItemCounts(itemIds);

        // then
        StepVerifier.create(result)
                .assertNext(map -> {
                    assertEquals(0, map.getOrDefault(10L, 0));
                    assertEquals(0, map.getOrDefault(20L, 0));
                    assertEquals(0, map.getOrDefault(30L, 0));
                })
                .verifyComplete();
        verify(cartRepository).findById(DEFAULT_CART_ID);
        verify(cartItemRepository).findByCartIdAndItemIdIn(DEFAULT_CART_ID, itemIds);
    }

    @Test
    void getItemCounts_givenItems_returnsCorrectCounts() {
        // given
        Cart cart = new Cart();
        cart.setId(DEFAULT_CART_ID);
        CartItem cartItem1 = new CartItem(1L, DEFAULT_CART_ID, 10L, 2);
        CartItem cartItem2 = new CartItem(2L, DEFAULT_CART_ID, 20L, 5);
        List<Long> itemIds = List.of(10L, 20L, 30L);
        when(cartRepository.findById(DEFAULT_CART_ID)).thenReturn(Mono.just(cart));
        when(cartItemRepository.findByCartIdAndItemIdIn(DEFAULT_CART_ID, itemIds))
                .thenReturn(Flux.just(cartItem1, cartItem2));

        // when
        Mono<Map<Long, Integer>> result = cartService.getItemCounts(itemIds);

        // then
        StepVerifier.create(result)
                .assertNext(map -> {
                    assertEquals(2, map.get(10L));
                    assertEquals(5, map.get(20L));
                    assertNull(map.get(30L));
                })
                .verifyComplete();
    }

    @Test
    void actOnCartItems_givenNewItemAndPlusAction_addsItem() {
        // given
        Cart cart = new Cart();
        cart.setId(DEFAULT_CART_ID);
        Item item = new Item(10L, "Item1", "Desc1", 1000L, null);
        CartItem savedCartItem = new CartItem(1L, DEFAULT_CART_ID, 10L, 1);
        ItemDto dto = new ItemDto(10L, "Item1", "Desc1", 1000L, 1);

        when(cartRepository.findById(DEFAULT_CART_ID)).thenReturn(Mono.just(cart));
        when(cartItemRepository.findByCartIdAndItemId(DEFAULT_CART_ID, 10L))
                .thenReturn(Mono.empty());
        when(cartItemRepository.save(any(CartItem.class))).thenReturn(Mono.just(savedCartItem));
        when(cartItemRepository.findByCartId(DEFAULT_CART_ID)).thenReturn(Flux.just(savedCartItem));
        when(itemRepository.findAllById(anyIterable())).thenReturn(Flux.just(item));

        // when
        Flux<ItemDto> result = cartService.actOnCartItems(10L, ActionTypeDto.PLUS);

        // then
        StepVerifier.create(result)
                .expectNext(dto)
                .verifyComplete();

        verify(cartItemRepository).save(cartItemCaptor.capture());
        CartItem captured = cartItemCaptor.getValue();
        assertEquals(DEFAULT_CART_ID, captured.getCartId());
        assertEquals(10L, captured.getItemId());
        assertEquals(1, captured.getCount());
    }

    @Test
    void actOnCartItems_givenExistingItemAndPlusAction_increasesCount() {
        // given
        Cart cart = new Cart();
        cart.setId(DEFAULT_CART_ID);
        CartItem existing = new CartItem(1L, DEFAULT_CART_ID, 10L, 2);
        Item item = new Item(10L, "Item1", "Desc1", 1000L, null);
        ItemDto dto = new ItemDto(10L, "Item1", "Desc1", 1000L, 3);

        when(cartRepository.findById(DEFAULT_CART_ID)).thenReturn(Mono.just(cart));
        when(cartItemRepository.findByCartIdAndItemId(DEFAULT_CART_ID, 10L))
                .thenReturn(Mono.just(existing));
        when(cartItemRepository.save(existing)).thenReturn(Mono.just(existing));
        when(cartItemRepository.findByCartId(DEFAULT_CART_ID)).thenReturn(Flux.just(existing));
        when(itemRepository.findAllById(anyIterable())).thenReturn(Flux.just(item));

        // when
        Flux<ItemDto> result = cartService.actOnCartItems(10L, ActionTypeDto.PLUS);

        // then
        StepVerifier.create(result)
                .expectNext(dto)
                .verifyComplete();

        assertEquals(3, existing.getCount());
        verify(cartItemRepository).save(existing);
    }

    @Test
    void actOnCartItems_givenCountMoreThanOneAndMinusAction_decreasesCount() {
        // given
        Cart cart = new Cart();
        cart.setId(DEFAULT_CART_ID);
        CartItem existing = new CartItem(1L, DEFAULT_CART_ID, 10L, 2);
        Item item = new Item(10L, "Item1", "Desc1", 1000L, null);
        ItemDto dto = new ItemDto(10L, "Item1", "Desc1", 1000L, 1);

        when(cartRepository.findById(DEFAULT_CART_ID)).thenReturn(Mono.just(cart));
        when(cartItemRepository.findByCartIdAndItemId(DEFAULT_CART_ID, 10L))
                .thenReturn(Mono.just(existing));
        when(cartItemRepository.save(existing)).thenReturn(Mono.just(existing));
        when(cartItemRepository.findByCartId(DEFAULT_CART_ID)).thenReturn(Flux.just(existing));
        when(itemRepository.findAllById(anyIterable())).thenReturn(Flux.just(item));

        // when
        Flux<ItemDto> result = cartService.actOnCartItems(10L, ActionTypeDto.MINUS);

        // then
        StepVerifier.create(result)
                .expectNext(dto)
                .verifyComplete();

        assertEquals(1, existing.getCount());
        verify(cartItemRepository).save(existing);
        verify(cartItemRepository, never()).delete(any());
    }

    @Test
    void actOnCartItems_givenCountEqualsOneAndMinusAction_deletesItem() {
        // given
        Cart cart = new Cart();
        cart.setId(DEFAULT_CART_ID);
        CartItem existing = new CartItem(1L, DEFAULT_CART_ID, 10L, 1);
        Item item = new Item(10L, "Item 1", "Desc 1", 1000L, null);
        when(cartRepository.findById(DEFAULT_CART_ID)).thenReturn(Mono.just(cart));
        when(cartItemRepository.findByCartIdAndItemId(DEFAULT_CART_ID, 10L))
                .thenReturn(Mono.just(existing));
        when(cartItemRepository.delete(existing)).thenReturn(Mono.empty());
        when(cartItemRepository.findByCartId(DEFAULT_CART_ID)).thenReturn(Flux.empty());
        when(itemRepository.findAllById(anyIterable())).thenReturn(Flux.just(item));

        // when
        Flux<ItemDto> result = cartService.actOnCartItems(10L, ActionTypeDto.MINUS);

        // then
        StepVerifier.create(result)
                .expectNextCount(0)
                .verifyComplete();

        verify(cartItemRepository).delete(existing);
    }

    @Test
    void actOnCartItems_givenItemNotExistsAndMinusAction_doesNothing() {
        // given
        Cart cart = new Cart();
        cart.setId(DEFAULT_CART_ID);
        Item item = new Item(10L, "Item 1", "Desc 1", 1000L, null);
        when(cartRepository.findById(DEFAULT_CART_ID)).thenReturn(Mono.just(cart));
        when(cartItemRepository.findByCartIdAndItemId(DEFAULT_CART_ID, 10L))
                .thenReturn(Mono.empty());
        when(cartItemRepository.findByCartId(DEFAULT_CART_ID)).thenReturn(Flux.empty());
        when(itemRepository.findAllById(anyIterable())).thenReturn(Flux.just(item));
        when(cartItemRepository.delete(any(CartItem.class))).thenReturn(Mono.empty());

        // when
        Flux<ItemDto> result = cartService.actOnCartItems(10L, ActionTypeDto.MINUS);

        // then
        StepVerifier.create(result)
                .expectNextCount(0)
                .verifyComplete();
    }
}