package ru.devinvader.market.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.devinvader.market.domain.Item;

@Repository
public interface ItemRepository extends ReactiveCrudRepository<Item, Long> {
    @Query("SELECT * FROM items"
        + "WHERE LOWER(title) LIKE LOWER(CONCAT('%', :search, '%')) OR "
        + "LOWER(description) LIKE LOWER(CONCAT('%', :search, '%'))")
    Flux<Item> searchByTitleOrDescription(String search, Pageable pageable);

    @Query("SELECT COUNT(*) FROM items "
        + "WHERE LOWER(title) LIKE LOWER(CONCAT('%', :search, '%')) "
        + "OR LOWER(description) LIKE LOWER(CONCAT('%', :search, '%'))")
    Mono<Long> countByTitleOrDescription(String search);
}