package ru.devinvader.market.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Embedded;
import org.springframework.data.relational.core.mapping.Table;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Table("cart_items")
public class CartItem {
    @Id
    @Embedded(onEmpty = Embedded.OnEmpty.USE_NULL)
    private CartItemId id;

    private Integer count;
}