package ru.tidinari.market.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.tidinari.market.domain.Item;
import ru.tidinari.market.repository.ItemRepository;

@Service
@RequiredArgsConstructor
public class AdminService {

    private final ItemRepository itemRepository;

    public Item findItemById(Long id) {
        return itemRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Item not found"));
    }

    public Item saveItem(Item item) {
        return itemRepository.save(item);
    }

    public void deleteItemById(Long id) {
        itemRepository.deleteById(id);
    }
}