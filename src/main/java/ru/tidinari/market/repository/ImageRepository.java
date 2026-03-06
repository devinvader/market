package ru.tidinari.market.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.tidinari.market.domain.Image;

public interface ImageRepository extends JpaRepository<Image, Long> {
    Image findByItemId(Long itemId);
}