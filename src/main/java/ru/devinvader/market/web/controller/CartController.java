package ru.devinvader.market.web.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import reactor.core.publisher.Mono;
import ru.devinvader.market.service.CartService;
import ru.devinvader.market.web.dto.ActionTypeDto;

@Controller
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;

    @GetMapping("/cart/items")
    public Mono<String> getItems(Model model) {
        Mono<java.util.List<ru.devinvader.market.web.dto.ItemDto>> itemsMono = cartService.getCartItems().collectList();
        Mono<Long> totalMono = cartService.getTotal();
        return Mono.zip(itemsMono, totalMono)
                .map(tuple -> {
                    model.addAttribute("items", tuple.getT1());
                    model.addAttribute("total", tuple.getT2());
                    return "cart";
                });
    }

    @PostMapping("/cart/items")
    public Mono<String> actOnItems(
            @RequestParam(name = "id") long id,
            @RequestParam(name = "action") ActionTypeDto action,
            Model model
    ) {
        Mono<java.util.List<ru.devinvader.market.web.dto.ItemDto>> itemsMono = cartService.actOnCartItems(id, action).collectList();
        Mono<Long> totalMono = cartService.getTotal();
        return Mono.zip(itemsMono, totalMono)
                .map(tuple -> {
                    model.addAttribute("items", tuple.getT1());
                    model.addAttribute("total", tuple.getT2());
                    return "cart";
                });
    }
}
