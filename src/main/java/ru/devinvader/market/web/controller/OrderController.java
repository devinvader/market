package ru.devinvader.market.web.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;
import ru.devinvader.market.service.OrderService;

@Controller
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @GetMapping("/orders")
    public ModelAndView getOrders() {
        ModelAndView modelAndView = new ModelAndView("orders");
        modelAndView.addObject("orders", orderService.getOrders());
        return modelAndView;
    }

    @GetMapping("/orders/{id}")
    public ModelAndView getOrder(
            @PathVariable(name = "id") long id,
            @RequestParam(name = "newOrder", defaultValue = "false") boolean newOrder
    ) {
        ModelAndView modelAndView = new ModelAndView("order");
        modelAndView.addObject("order", orderService.getOrCreateOrder(id, newOrder));
        modelAndView.addObject("newOrder", newOrder);
        return modelAndView;
    }
}
