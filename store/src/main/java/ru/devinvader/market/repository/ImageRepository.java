package ru.devinvader.market.repository;

import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Mono;
import ru.devinvader.market.domain.Image;

public interface ImageRepository extends ReactiveCrudRepository<Image, Long> {
    @Query("SELECT i.* FROM images i JOIN items it ON i.id = it.image_id WHERE it.id = :itemId")
    Mono<Image> findByItemId(@Param("itemId") Long itemId);
    @Query("SELECT image_id FROM items WHERE id = :itemId")
    Mono<Long> findImageIdByItemId(@Param("itemId") Long itemId);
}