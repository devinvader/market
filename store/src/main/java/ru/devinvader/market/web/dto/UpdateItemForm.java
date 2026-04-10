package ru.devinvader.market.web.dto;

import org.springframework.http.codec.multipart.FilePart;

public record UpdateItemForm(
    String title,
    String description,
    Long price,
    FilePart imageFile
) {}