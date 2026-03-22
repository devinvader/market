package ru.devinvader.market.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;

import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CartItemId implements Serializable {
    @Id
    @Column("cart_id")
    private Long cartId;

    @Id
    @Column("item_id")
    private Long itemId;
}