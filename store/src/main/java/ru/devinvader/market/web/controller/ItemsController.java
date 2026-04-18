package ru.devinvader.market.web.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import reactor.core.publisher.Mono;
import ru.devinvader.market.service.CartService;
import ru.devinvader.market.service.ItemsService;
import ru.devinvader.market.service.PaymentClientService;
import ru.devinvader.market.web.dto.SortTypeDto;
import ru.devinvader.market.web.dto.ItemsActionDto;
import ru.devinvader.market.web.dto.ItemActionDto;

@Controller
@RequiredArgsConstructor
public class ItemsController {

    private final ItemsService itemsService;
    private final CartService cartService;
    private final PaymentClientService paymentClientService;

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
    public Mono<String> actOnItems(@ModelAttribute ItemsActionDto dto) {
        String search = dto.search() != null ? dto.search() : "";
        SortTypeDto sort = dto.sort() != null ? dto.sort() : SortTypeDto.NO;
        int pageNumber = dto.pageNumber() != null ? dto.pageNumber() : 0;
        int pageSize = dto.pageSize() != null ? dto.pageSize() : 10;
        return cartService.actOnCartItems(dto.id(), dto.action())
                .then(Mono.just("redirect:/items"))
                .map(redirect -> redirect + "?search=" + search + "&sort=" + sort.name() +
                        "&pageNumber=" + pageNumber + "&pageSize=" + pageSize);
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
            @ModelAttribute ItemActionDto dto
    ) {
        return cartService.actOnCartItems(id, dto.action())
                .then(Mono.just("redirect:/items/" + id));
    }

    /**
     * Оформление заказа с предварительной проверкой оплаты.
     * 1. Получаем корзину и суммарный итог.
     * 2. Вызываем paymentClientService.pay(total).
     * 3. При success=true — редирект на страницу заказа.
     * 4. При success=false или недоступности сервиса — редирект в корзину с ошибкой.
     */
    @PostMapping("/buy")
    public Mono<String> buyItems() {
        return cartService.getUserCart()
                .flatMap(cart -> cartService.getTotal()
                        .flatMap(total -> paymentClientService.pay(total)
                                .map(paymentResponse -> {
                                    if (paymentResponse.success()) {
                                        return "redirect:/orders/" + cart.getId() + "?newOrder=true";
                                    } else {
                                        String msg = paymentResponse.message() != null
                                                ? paymentResponse.message()
                                                : "Ошибка оплаты";
                                        return "redirect:/cart/items?paymentError=" +
                                                java.net.URLEncoder.encode(msg, java.nio.charset.StandardCharsets.UTF_8);
                                    }
                                })
                        )
                );
    }
}
