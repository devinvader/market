package ru.tidinari.market.web.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import ru.tidinari.market.domain.Image;
import ru.tidinari.market.service.ImageService;

@Controller
@RequiredArgsConstructor
public class ImageController {

    private final ImageService imageService;

    @GetMapping("/items/{id}/image")
    public ResponseEntity<byte[]> getImage(@PathVariable Long id) {
        Image image = imageService.getImageByItemId(id);
        if (image == null) {
            return ResponseEntity.notFound().build();
        }

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType(image.getContentType()));
        headers.setContentLength(image.getData().length);
        return new ResponseEntity<>(image.getData(), headers, HttpStatus.OK);
    }
}