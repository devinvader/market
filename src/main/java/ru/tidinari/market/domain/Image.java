package ru.tidinari.market.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

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
    private Item item;
}