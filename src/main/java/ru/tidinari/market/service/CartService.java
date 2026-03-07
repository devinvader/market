package ru.tidinari.market.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import ru.tidinari.market.domain.Cart;
import ru.tidinari.market.domain.CartItem;
import ru.tidinari.market.domain.Item;
import ru.tidinari.market.repository.CartItemRepository;
import ru.tidinari.market.repository.CartRepository;
import ru.tidinari.market.repository.ItemRepository;
import ru.tidinari.market.web.dto.ActionTypeDto;
import ru.tidinari.market.web.dto.ItemDto;
import ru.tidinari.market.web.mapper.ItemMapper;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class CartService {

    @Autowired
    private CartRepository cartRepository;

    @Autowired
    private CartItemRepository cartItemRepository;

    @Autowired
    private ItemRepository itemRepository;

    @Autowired
    private ImageService imageService;

    @Autowired
    private ItemMapper itemMapper;

    public List<ItemDto> getCartItems() {
        Cart cart = cartRepository.findById(1L).orElseGet(() -> {
            Cart newCart = new Cart();
            cartRepository.save(newCart);
            return newCart;
        });

        // Получаем элементы корзины
        List<CartItem> cartItems = cartItemRepository.findByCartId(cart.getId());

        // Преобразуем в ItemDto
        return cartItems.stream()
                .map(cartItem -> itemMapper.toDto(
                        cartItem.getItem(),
                        imageService.getImageUrl(cartItem.getItem().getId()),
                        cartItem.getCount()
                ))
                .collect(Collectors.toList());
    }

    public int getTotal() {
        List<ItemDto> items = getCartItems();
        return items.stream().mapToInt(item -> (int) (item.price() * item.count())).sum();
    }

    public Map<Long, Integer> getItemCounts(List<Long> itemIds) {
        Cart cart = cartRepository.findById(1L).orElseGet(() -> {
            Cart newCart = new Cart();
            cartRepository.save(newCart);
            return newCart;
        });
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
        // Получаем корзину пользователя (для простоты используем ID = 1)
        Cart cart = cartRepository.findById(1L).orElseGet(() -> {
            Cart newCart = new Cart();
            cartRepository.save(newCart);
            return newCart;
        });

        // Проверяем, существует ли элемент в корзине
        CartItem cartItem = cartItemRepository.findByCartIdAndItemId(cart.getId(), id);

        switch (action) {
            case PLUS:
                if (cartItem == null) {
                    // Создаем новый элемент корзины
                    Item item = itemRepository.findById(id).orElseThrow(() -> new RuntimeException("Item not found"));
                    cartItem = new CartItem();
                    cartItem.setCart(cart);
                    cartItem.setItem(item);
                    cartItem.setCount(1);
                } else {
                    // Увеличиваем количество
                    cartItem.setCount(cartItem.getCount() + 1);
                }
                cartItemRepository.save(cartItem);
                break;
            case MINUS:
                if (cartItem != null) {
                    if (cartItem.getCount() > 1) {
                        // Уменьшаем количество
                        cartItem.setCount(cartItem.getCount() - 1);
                        cartItemRepository.save(cartItem);
                    } else {
                        // Удаляем элемент из корзины
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
}