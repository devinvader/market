package ru.devinvader.market.domain;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;
import org.springframework.data.relational.core.mapping.Column;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Table("images")
public class Image {
    @Id
    private Long id;

    @Column("data")
    private byte[] data;

    @Column("content_type")
    private String contentType;
}