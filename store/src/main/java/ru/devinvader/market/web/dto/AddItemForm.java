package ru.devinvader.market.web.dto;

import org.springframework.http.codec.multipart.FilePart;

public record AddItemForm(
    String title,
    String description,
    Long price,
    FilePart imageFile
) {}