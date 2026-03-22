package ru.devinvader.market.unit.repository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import reactor.test.StepVerifier;
import ru.devinvader.market.domain.Item;
import ru.devinvader.market.repository.ItemRepository;

import static org.assertj.core.api.Assertions.assertThat;

public class ItemRepositoryTest extends BaseRepositoryTest {
    @Autowired
    private ItemRepository itemRepository;

    @Test
    public void saveAndFindById_givenItem_whenSave_thenFindById() {
        // given
        Item item = new Item();
        item.setTitle("Test Item");
        item.setPrice(100L);

        // when
        Item savedItem = itemRepository.save(item).block();

        // then
        StepVerifier.create(itemRepository.findById(savedItem.getId()))
                .assertNext(found -> {
                    assertThat(found.getTitle()).isEqualTo("Test Item");
                    assertThat(found.getPrice()).isEqualTo(100L);
                })
                .verifyComplete();
    }

    @Test
    public void searchByTitleOrDescription_givenItems_whenSearch_thenReturnMatching() {
        // given
        Item item1 = new Item();
        item1.setTitle("Apple iPhone");
        item1.setPrice(1000L);
        itemRepository.save(item1).block();

        Item item2 = new Item();
        item2.setTitle("Samsung Galaxy");
        item2.setPrice(900L);
        itemRepository.save(item2).block();

        // when & then
        StepVerifier.create(itemRepository.searchByTitleOrDescription("apple", 10, 0))
                .assertNext(found -> {
                    assertThat(found.getTitle()).isEqualTo("Apple iPhone");
                })
                .verifyComplete();
    }

    @Test
    public void delete_givenItem_whenDelete_thenNotFound() {
        // given
        Item item = new Item();
        item.setTitle("Test Item");
        item.setPrice(100L);
        Item savedItem = itemRepository.save(item).block();

        // when
        itemRepository.deleteById(savedItem.getId()).block();

        // then
        StepVerifier.create(itemRepository.findById(savedItem.getId()))
                .verifyComplete();
    }
}