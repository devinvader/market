package ru.devinvader.market.web.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import reactor.core.publisher.Mono;
import ru.devinvader.market.service.CartService;
import ru.devinvader.market.service.ItemsService;
import ru.devinvader.market.web.dto.ActionTypeDto;
import ru.devinvader.market.web.dto.SortTypeDto;

@Controller
@RequiredArgsConstructor
public class ItemsController {

    private final ItemsService itemsService;
    private final CartService cartService;

    @GetMapping(path = {"/", "/items"})
    public Mono<String> getItems(
            @RequestParam(name = "search", required = false, defaultValue = "") String search,
            @RequestParam(name = "sort", required = false, defaultValue = "NO") SortTypeDto sortType,
            @RequestParam(name = "pageNumber", required = false, defaultValue = "0") Integer page,
            @RequestParam(name = "pageSize", required = false, defaultValue = "10") Integer size,
            Model model
    ) {
        return itemsService.getItems(search, sortType, page, size)
                .map(pagedItems -> {
                    model.addAttribute("search", search);
                    model.addAttribute("sort", sortType.name());
                    model.addAttribute("paging", pagedItems.pagingDto());
                    model.addAttribute("items", pagedItems.items());
                    return "items";
                });
    }

    @PostMapping("/items")
    public Mono<String> actOnItems(
            @RequestParam(name = "id") long id,
            @RequestParam(name = "action") ActionTypeDto action,
            @RequestParam(name = "search", required = false, defaultValue = "") String search,
            @RequestParam(name = "sortType", required = false, defaultValue = "NO") SortTypeDto sortType,
            @RequestParam(name = "pageNumber", required = false, defaultValue = "0") Integer page,
            @RequestParam(name = "pageSize", required = false, defaultValue = "10") Integer size
    ) {
        return cartService.actOnCartItems(id, action)
                .then(Mono.just("redirect:/items"))
                .map(redirect -> redirect + "?search=" + search + "&sort=" + sortType.name() +
                        "&pageNumber=" + page + "&pageSize=" + size);
    }

    @GetMapping("/items/{id}")
    public Mono<String> getItem(
            @PathVariable(name = "id") long id,
            Model model
    ) {
        return itemsService.getItem(id)
                .map(item -> {
                    model.addAttribute("item", item);
                    return "item";
                });
    }

    @PostMapping("/items/{id}")
    public Mono<String> actOnItem(
            @PathVariable(name = "id") long id,
            @RequestParam(name = "action") ActionTypeDto action
    ) {
        return cartService.actOnCartItems(id, action)
                .then(Mono.just("redirect:/items/" + id));
    }

    @PostMapping("/buy")
    public Mono<String> buyItems() {
        return cartService.getUserCart()
                .map(cart -> "redirect:/orders/" + cart.getId() + "?newOrder=true");
    }
}
