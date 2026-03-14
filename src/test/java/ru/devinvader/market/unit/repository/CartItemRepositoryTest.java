package ru.devinvader.market.unit.repository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import ru.devinvader.market.domain.Cart;
import ru.devinvader.market.domain.CartItem;
import ru.devinvader.market.domain.CartItemId;
import ru.devinvader.market.domain.Item;
import ru.devinvader.market.repository.CartItemRepository;
import ru.devinvader.market.repository.CartRepository;
import ru.devinvader.market.repository.ItemRepository;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class CartItemRepositoryTest extends BaseRepositoryTest {
    @Autowired
    private CartItemRepository cartItemRepository;

    @Autowired
    private CartRepository cartRepository;

    @Autowired
    private ItemRepository itemRepository;

    @Test
    public void saveAndFindById_givenCartAndItem_whenSave_thenFindById() {
        // given
        Cart cart = new Cart();
        Cart savedCart = cartRepository.save(cart);

        Item item = new Item();
        item.setTitle("Test Item");
        item.setPrice(100L);
        Item savedItem = itemRepository.save(item);

        CartItem cartItem = new CartItem();
        cartItem.setCart(savedCart);
        cartItem.setItem(savedItem);
        cartItem.setCount(2);

        // when
        cartItemRepository.save(cartItem);
        CartItemId cartItemId = new CartItemId(savedCart.getId(), savedItem.getId());
        CartItem foundCartItem = cartItemRepository.findById(cartItemId).orElse(null);

        // then
        assertThat(foundCartItem).isNotNull();
        assertThat(foundCartItem.getCount()).isEqualTo(2);
    }

    @Test
    public void findByCartId_givenCartWithItems_whenFind_thenReturnList() {
        // given
        Cart cart = new Cart();
        Cart savedCart = cartRepository.save(cart);

        Item item = new Item();
        item.setTitle("Test Item");
        item.setPrice(100L);
        Item savedItem = itemRepository.save(item);

        CartItem cartItem = new CartItem();
        cartItem.setCart(savedCart);
        cartItem.setItem(savedItem);
        cartItem.setCount(2);
        cartItemRepository.save(cartItem);

        // when
        List<CartItem> cartItems = cartItemRepository.findByCartId(savedCart.getId());

        // then
        assertThat(cartItems).hasSize(1);
        assertThat(cartItems.get(0).getCount()).isEqualTo(2);
    }

    @Test
    public void findByCartIdAndItemId_givenCartItem_whenFind_thenReturnItem() {
        // given
        Cart cart = new Cart();
        Cart savedCart = cartRepository.save(cart);

        Item item = new Item();
        item.setTitle("Test Item");
        item.setPrice(100L);
        Item savedItem = itemRepository.save(item);

        CartItem cartItem = new CartItem();
        cartItem.setCart(savedCart);
        cartItem.setItem(savedItem);
        cartItem.setCount(2);
        cartItemRepository.save(cartItem);

        // when
        CartItem foundCartItem = cartItemRepository.findByCartIdAndItemId(savedCart.getId(), savedItem.getId());

        // then
        assertThat(foundCartItem).isNotNull();
        assertThat(foundCartItem.getCount()).isEqualTo(2);
    }

    @Test
    public void delete_givenCartItem_whenDelete_thenNotFound() {
        // given
        Cart cart = new Cart();
        Cart savedCart = cartRepository.save(cart);

        Item item = new Item();
        item.setTitle("Test Item");
        item.setPrice(100L);
        Item savedItem = itemRepository.save(item);

        CartItem cartItem = new CartItem();
        cartItem.setCart(savedCart);
        cartItem.setItem(savedItem);
        cartItem.setCount(2);
        cartItemRepository.save(cartItem);

        // when
        CartItemId cartItemId = new CartItemId(savedCart.getId(), savedItem.getId());
        cartItemRepository.deleteById(cartItemId);
        CartItem foundCartItem = cartItemRepository.findById(cartItemId).orElse(null);

        // then
        assertThat(foundCartItem).isNull();
    }
}