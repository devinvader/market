package ru.tidinari.market.unit.repository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.jdbc.Sql;
import ru.tidinari.market.domain.Cart;
import ru.tidinari.market.repository.CartRepository;

import static org.assertj.core.api.Assertions.assertThat;

public class CartRepositoryTest extends BaseRepositoryTest {
    @Autowired
    private CartRepository cartRepository;

    @Test
    public void testSaveAndFindById() {
        // given
        Cart cart = new Cart();

        // when
        Cart savedCart = cartRepository.save(cart);
        Cart foundCart = cartRepository.findById(savedCart.getId()).orElse(null);

        // then
        assertThat(foundCart).isNotNull();
        assertThat(foundCart.getId()).isEqualTo(savedCart.getId());
    }

    @Test
    public void testDelete() {
        // given
        Cart cart = new Cart();
        Cart savedCart = cartRepository.save(cart);

        // when
        cartRepository.deleteById(savedCart.getId());
        Cart foundCart = cartRepository.findById(savedCart.getId()).orElse(null);

        // then
        assertThat(foundCart).isNull();
    }
}