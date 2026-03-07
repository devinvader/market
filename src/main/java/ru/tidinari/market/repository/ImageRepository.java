package ru.tidinari.market.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.tidinari.market.domain.Image;

import java.util.Optional;

public interface ImageRepository extends JpaRepository<Image, Long> {
    @Query("SELECT i.id FROM Image i WHERE i.item.id = :itemId LIMIT 1")
    Long findIdByItemId(@Param("itemId") Long itemId);

    Optional<Image> findByItemId(Long itemId);
}