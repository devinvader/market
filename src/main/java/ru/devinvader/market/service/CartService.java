package ru.devinvader.market.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import ru.devinvader.market.domain.Cart;
import ru.devinvader.market.domain.CartItem;
import ru.devinvader.market.domain.Item;
import ru.devinvader.market.repository.CartItemRepository;
import ru.devinvader.market.repository.CartRepository;
import ru.devinvader.market.repository.ItemRepository;
import ru.devinvader.market.web.dto.ActionTypeDto;
import ru.devinvader.market.web.dto.ItemDto;
import ru.devinvader.market.mapper.ItemMapper;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CartService {

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final ItemRepository itemRepository;
    private final ImageService imageService;
    private final ItemMapper itemMapper;

    // т.к. у нас пока нет ни пользователей, ни сессии, используем один cartId
    private final long DEFAUL_CART_ID = 1L;

    public List<ItemDto> getCartItems() {
        Cart cart = getUserCart();
        return cartItemRepository.findByCartId(cart.getId()).stream()
                .map(cartItem -> itemMapper.toDto(
                        cartItem.getItem(),
                        cartItem.getCount()
                ))
                .collect(Collectors.toList());
    }

    public int getTotal() {
        List<ItemDto> items = getCartItems();
        return items.stream().mapToInt(item -> (int) (item.price() * item.count())).sum();
    }

    public Map<Long, Integer> getItemCounts(List<Long> itemIds) {
        Cart cart = getUserCart();
        List<CartItem> cartItems = cartItemRepository.findByCartIdAndItemIdIn(cart.getId(), itemIds);
        Map<Long, Integer> counts = new HashMap<>();
        for (CartItem ci : cartItems) {
            counts.put(ci.getItem().getId(), ci.getCount());
        }
        for (Long id : itemIds) {
            counts.putIfAbsent(id, 0);
        }
        return counts;
    }

    public List<ItemDto> actOnCartItems(long id, ActionTypeDto action) {
        Cart cart = getUserCart();
        CartItem cartItem = cartItemRepository.findByCartIdAndItemId(cart.getId(), id);
        switch (action) {
            case PLUS:
                if (cartItem == null) {
                    Item item = itemRepository.findById(id).orElseThrow(() -> new RuntimeException("Item not found"));
                    cartItem = new CartItem();
                    cartItem.setCart(cart);
                    cartItem.setItem(item);
                    cartItem.setCount(1);
                } else {
                    cartItem.setCount(cartItem.getCount() + 1);
                }
                cartItemRepository.save(cartItem);
                break;
            case MINUS:
                if (cartItem != null) {
                    if (cartItem.getCount() > 1) {
                        cartItem.setCount(cartItem.getCount() - 1);
                        cartItemRepository.save(cartItem);
                    } else {
                        cartItemRepository.delete(cartItem);
                    }
                }
                break;
            case DELETE:
                if (cartItem != null) {
                    cartItemRepository.delete(cartItem);
                }
                break;
        }

        return getCartItems();
    }

    public Cart getUserCart() {
        return cartRepository.findById(DEFAUL_CART_ID).orElseGet(() -> {
            Cart newCart = new Cart();
            cartRepository.save(newCart);
            return newCart;
        });
    }
}