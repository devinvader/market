package ru.devinvader.market.unit.repository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import reactor.test.StepVerifier;
import ru.devinvader.market.domain.Cart;
import ru.devinvader.market.repository.CartRepository;

import static org.assertj.core.api.Assertions.assertThat;

public class CartRepositoryTest extends BaseRepositoryTest {
    @Autowired
    private CartRepository cartRepository;

    @Test
    public void saveAndFindById_givenCart_whenSave_thenFindById() {
        // given
        Cart cart = new Cart();

        // when
        Cart savedCart = cartRepository.save(cart).block();

        // then
        StepVerifier.create(cartRepository.findById(savedCart.getId()))
                .assertNext(found -> {
                    assertThat(found.getId()).isEqualTo(savedCart.getId());
                })
                .verifyComplete();
    }

    @Test
    public void delete_givenCart_whenDelete_thenNotFound() {
        // given
        Cart cart = new Cart();
        Cart savedCart = cartRepository.save(cart).block();

        // when
        cartRepository.deleteById(savedCart.getId()).block();

        // then
        StepVerifier.create(cartRepository.findById(savedCart.getId()))
                .verifyComplete();
    }
}