package ru.devinvader.market.web.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import reactor.core.publisher.Mono;
import ru.devinvader.market.service.CartService;
import ru.devinvader.market.web.dto.ActionTypeDto;
import ru.devinvader.market.web.dto.ItemActionForm;
import ru.devinvader.market.web.dto.ItemDto;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;

    @GetMapping("/cart/items")
    public Mono<String> getItems(Model model) {
        Mono<List<ItemDto>> itemsMono = cartService.getCartItems().collectList();
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
            @ModelAttribute("item") ItemActionForm item,
            Model model
    ) {
        Mono<List<ItemDto>> itemsMono = cartService.actOnCartItems(item.id(), item.action()).collectList();
        Mono<Long> totalMono = cartService.getTotal();
        return Mono.zip(itemsMono, totalMono)
                .map(tuple -> {
                    model.addAttribute("items", tuple.getT1());
                    model.addAttribute("total", tuple.getT2());
                    return "cart";
                });
    }
}
