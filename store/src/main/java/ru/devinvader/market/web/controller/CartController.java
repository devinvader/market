package ru.devinvader.market.web.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import reactor.core.publisher.Mono;
import ru.devinvader.market.service.CartService;
import ru.devinvader.market.service.PaymentClientService;
import ru.devinvader.market.utils.ClientConstants;
import ru.devinvader.market.utils.CurrentUserProvider;
import ru.devinvader.market.web.dto.ItemActionForm;
import ru.devinvader.market.web.dto.ItemDto;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;
    private final PaymentClientService paymentClientService;
    private final CurrentUserProvider currentUserProvider;

    @GetMapping("/cart/items")
    public Mono<String> getItems(Model model) {
        return currentUserProvider.getCurrentUserId()
                .flatMap(userId -> {
                    Mono<List<ItemDto>> itemsMono = cartService.getCartItems(userId).collectList();
                    Mono<Long> totalMono = cartService.getTotal(userId);
                    Mono<Long> balanceMono = paymentClientService.getBalance();

                    return Mono.zip(itemsMono, totalMono, balanceMono)
                            .map(tuple -> {
                                long balance = tuple.getT3();
                                boolean serviceAvailable = balance != ClientConstants.BALANCE_SERVICE_UNAVAILABLE;
                                model.addAttribute("items", tuple.getT1());
                                model.addAttribute("total", tuple.getT2());
                                model.addAttribute("balance", balance);
                                model.addAttribute("paymentServiceAvailable", serviceAvailable);
                                return "cart";
                            });
                });
    }

    @PostMapping("/cart/items")
    public Mono<String> actOnItems(
            @ModelAttribute("item") ItemActionForm item,
            Model model
    ) {
        return currentUserProvider.getCurrentUserId()
                .flatMap(userId -> {
                    Mono<List<ItemDto>> itemsMono = cartService.actOnCartItems(userId, item.id(), item.action()).collectList();
                    Mono<Long> totalMono = cartService.getTotal(userId);
                    Mono<Long> balanceMono = paymentClientService.getBalance();

                    return Mono.zip(itemsMono, totalMono, balanceMono)
                            .map(tuple -> {
                                long balance = tuple.getT3();
                                boolean serviceAvailable = balance != ClientConstants.BALANCE_SERVICE_UNAVAILABLE;
                                model.addAttribute("items", tuple.getT1());
                                model.addAttribute("total", tuple.getT2());
                                model.addAttribute("balance", balance);
                                model.addAttribute("paymentServiceAvailable", serviceAvailable);
                                return "cart";
                            });
                });
    }
}
