package ru.devinvader.market.domain;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Table("carts")
public class Cart {
    @Id
    private Long id;

    @Column("user_id")
    private Long userId;
}