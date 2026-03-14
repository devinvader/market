package ru.devinvader.market.domain;

import jakarta.persistence.*;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "images")
public class Image {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Lob
    @Column(nullable = false, columnDefinition = "BLOB")
    private byte[] data;

    @Column(name = "content_type", nullable = false)
    private String contentType;

    @OneToOne(mappedBy = "image")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Item item;
}