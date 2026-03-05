package ru.tidinari.market.web.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import ru.tidinari.market.service.CartService;
import ru.tidinari.market.web.dto.ActionTypeDto;

@Controller
public class CartController {

    @Autowired
    private CartService cartService;

    @GetMapping("/cart/items")
    public ModelAndView getItems() {
        ModelAndView modelAndView = new ModelAndView("cart");
        modelAndView.addObject("items", cartService.getCartItems());
        modelAndView.addObject("total", cartService.getTotal());
        return modelAndView;
    }

    @PostMapping("/cart/items")
    public ModelAndView actOnItems(
            @RequestParam(name = "id") long id,
            @RequestParam(name = "action") ActionTypeDto action
    ) {
        ModelAndView modelAndView = new ModelAndView("cart");
        modelAndView.addObject("items", cartService.actOnCartItems(id, action));
        modelAndView.addObject("total", cartService.getTotal());
        return modelAndView;
    }
}
