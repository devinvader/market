package ru.tidinari.market.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.tidinari.market.domain.Item;

@Repository
public interface ItemRepository extends JpaRepository<Item, Long> {
    @Query("SELECT i FROM Item i " +
            "WHERE LOWER(i.title) LIKE LOWER(CONCAT('%', :search, '%')) " +
            "   OR LOWER(i.description) LIKE LOWER(CONCAT('%', :search, '%'))")
    Page<Item> searchByTitleOrDescription(@Param("search") String search, Pageable pageable);
}