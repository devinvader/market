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
    public Mono<String> getItems(
            @RequestParam(name = "paymentError", required = false, defaultValue = "") String paymentError,
            Model model
    ) {
        return currentUserProvider.getCurrentUserId()
                .flatMap(userId -> cartService.getUserCart(userId)
                        .flatMap(cart -> {
                            Mono<List<ItemDto>> itemsMono = cartService.getCartItemsByCart(cart).collectList();
                            Mono<Long> totalMono = cartService.getTotalByCart(cart);
                            Mono<Long> balanceMono = paymentClientService.getBalance();

                            return Mono.zip(itemsMono, totalMono, balanceMono)
                                    .map(tuple -> {
                                        long balance = tuple.getT3();
                                        boolean serviceAvailable = balance != ClientConstants.BALANCE_SERVICE_UNAVAILABLE;
                                        model.addAttribute("isAuthenticated", true);
                                        model.addAttribute("items", tuple.getT1());
                                        model.addAttribute("total", tuple.getT2());
                                        model.addAttribute("balance", balance);
                                        model.addAttribute("paymentServiceAvailable", serviceAvailable);
                                        model.addAttribute("paymentError", paymentError);
                                        return "cart";
                                    });
                        }));
    }

    @PostMapping("/cart/items")
    public Mono<String> actOnItems(
            @ModelAttribute("item") ItemActionForm item,
            Model model
    ) {
        return currentUserProvider.getCurrentUserId()
                .flatMap(userId -> cartService.getUserCart(userId)
                        .flatMap(cart -> cartService.actOnCartItemsByCart(cart, item.id(), item.action())
                                .collectList()
                                .flatMap(items -> {
                                    long total = items.stream()
                                            .mapToLong(i -> i.price() * i.count())
                                            .sum();
                                    return paymentClientService.getBalance()
                                            .map(balance -> {
                                                boolean serviceAvailable = balance != ClientConstants.BALANCE_SERVICE_UNAVAILABLE;
                                                model.addAttribute("isAuthenticated", true);
                                                model.addAttribute("items", items);
                                                model.addAttribute("total", total);
                                                model.addAttribute("balance", balance);
                                                model.addAttribute("paymentServiceAvailable", serviceAvailable);
                                                model.addAttribute("paymentError", "");
                                                return "cart";
                                            });
                                })));
    }
}
