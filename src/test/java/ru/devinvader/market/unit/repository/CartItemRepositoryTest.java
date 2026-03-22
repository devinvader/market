package ru.devinvader.market.unit.repository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import reactor.test.StepVerifier;
import ru.devinvader.market.domain.Cart;
import ru.devinvader.market.domain.CartItem;
import ru.devinvader.market.domain.CartItemId;
import ru.devinvader.market.domain.Item;
import ru.devinvader.market.repository.CartItemRepository;
import ru.devinvader.market.repository.CartRepository;
import ru.devinvader.market.repository.ItemRepository;

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
        Cart savedCart = cartRepository.save(cart).block();

        Item item = new Item();
        item.setTitle("Test Item");
        item.setPrice(100L);
        Item savedItem = itemRepository.save(item).block();

        CartItemId cartItemId = new CartItemId(savedCart.getId(), savedItem.getId());
        CartItem cartItem = new CartItem(cartItemId, 2);

        // when
        cartItemRepository.save(cartItem).block();

        // then
        StepVerifier.create(cartItemRepository.findById(cartItemId))
                .assertNext(found -> {
                    assertThat(found.getCount()).isEqualTo(2);
                })
                .verifyComplete();
    }

    @Test
    public void findByIdCartId_givenCartWithItems_whenFind_thenReturnFlux() {
        // given
        Cart cart = new Cart();
        Cart savedCart = cartRepository.save(cart).block();

        Item item = new Item();
        item.setTitle("Test Item");
        item.setPrice(100L);
        Item savedItem = itemRepository.save(item).block();

        CartItemId cartItemId = new CartItemId(savedCart.getId(), savedItem.getId());
        CartItem cartItem = new CartItem(cartItemId, 2);
        cartItemRepository.save(cartItem).block();

        // when & then
        StepVerifier.create(cartItemRepository.findByIdCartId(savedCart.getId()))
                .assertNext(found -> {
                    assertThat(found.getCount()).isEqualTo(2);
                })
                .verifyComplete();
    }

    @Test
    public void findByCartIdAndItemId_givenCartItem_whenFind_thenReturnMono() {
        // given
        Cart cart = new Cart();
        Cart savedCart = cartRepository.save(cart).block();

        Item item = new Item();
        item.setTitle("Test Item");
        item.setPrice(100L);
        Item savedItem = itemRepository.save(item).block();

        CartItemId cartItemId = new CartItemId(savedCart.getId(), savedItem.getId());
        CartItem cartItem = new CartItem(cartItemId, 2);
        cartItemRepository.save(cartItem).block();

        // when & then
        StepVerifier.create(cartItemRepository.findByCartIdAndItemId(savedCart.getId(), savedItem.getId()))
                .assertNext(found -> {
                    assertThat(found.getCount()).isEqualTo(2);
                })
                .verifyComplete();
    }

    @Test
    public void delete_givenCartItem_whenDelete_thenNotFound() {
        // given
        Cart cart = new Cart();
        Cart savedCart = cartRepository.save(cart).block();

        Item item = new Item();
        item.setTitle("Test Item");
        item.setPrice(100L);
        Item savedItem = itemRepository.save(item).block();

        CartItemId cartItemId = new CartItemId(savedCart.getId(), savedItem.getId());
        CartItem cartItem = new CartItem(cartItemId, 2);
        cartItemRepository.save(cartItem).block();

        // when
        cartItemRepository.deleteById(cartItemId).block();

        // then
        StepVerifier.create(cartItemRepository.findById(cartItemId))
                .verifyComplete();
    }
}