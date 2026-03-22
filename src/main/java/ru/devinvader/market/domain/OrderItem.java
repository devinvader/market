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
@Table("order_items")
public class OrderItem {
    @Id
    @Embedded(onEmpty = Embedded.OnEmpty.USE_NULL)
    private OrderItemId id;

    private Integer count;
}