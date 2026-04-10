package ru.devinvader.market.web.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;
import ru.devinvader.market.service.ImageService;

@RestController
@RequiredArgsConstructor
public class ImageController {

    private final ImageService imageService;

    @GetMapping("/items/{id}/image")
    public Mono<ResponseEntity<byte[]>> getImage(@PathVariable Long id) {
        return imageService.getImageByItemId(id)
                .map(image -> {
                    HttpHeaders headers = new HttpHeaders();
                    headers.setContentType(MediaType.parseMediaType(image.getContentType()));
                    headers.setContentLength(image.getData().length);
                    return new ResponseEntity<>(image.getData(), headers, HttpStatus.OK);
                })
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }
}