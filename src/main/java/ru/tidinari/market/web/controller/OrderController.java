package ru.tidinari.market.web.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;
import ru.tidinari.market.service.OrderService;

@Controller
public class OrderController {

    @Autowired
    private OrderService orderService;

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
        modelAndView.addObject("order", orderService.getOrder(id, newOrder));
        modelAndView.addObject("newOrder", newOrder);
        return modelAndView;
    }
}
