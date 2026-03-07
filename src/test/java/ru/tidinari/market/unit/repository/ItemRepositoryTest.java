package ru.tidinari.market.unit.repository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import ru.tidinari.market.domain.Item;
import ru.tidinari.market.repository.ItemRepository;

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
        Item savedItem = itemRepository.save(item);
        Item foundItem = itemRepository.findById(savedItem.getId()).orElse(null);

        // then
        assertThat(foundItem).isNotNull();
        assertThat(foundItem.getTitle()).isEqualTo("Test Item");
        assertThat(foundItem.getPrice()).isEqualTo(100L);
    }

    @Test
    public void searchByTitleOrDescription_givenItems_whenSearch_thenReturnMatching() {
        // given
        Item item1 = new Item();
        item1.setTitle("Apple iPhone");
        item1.setPrice(1000L);
        itemRepository.save(item1);

        Item item2 = new Item();
        item2.setTitle("Samsung Galaxy");
        item2.setPrice(900L);
        itemRepository.save(item2);

        // when
        var page = itemRepository.searchByTitleOrDescription("apple", PageRequest.of(0, 10));

        // then
        assertThat(page.getContent()).hasSize(1);
        assertThat(page.getContent().get(0).getTitle()).isEqualTo("Apple iPhone");
    }

    @Test
    public void delete_givenItem_whenDelete_thenNotFound() {
        // given
        Item item = new Item();
        item.setTitle("Test Item");
        item.setPrice(100L);
        Item savedItem = itemRepository.save(item);

        // when
        itemRepository.deleteById(savedItem.getId());
        Item foundItem = itemRepository.findById(savedItem.getId()).orElse(null);

        // then
        assertThat(foundItem).isNull();
    }
}