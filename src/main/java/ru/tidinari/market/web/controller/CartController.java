package ru.tidinari.market.web.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import lombok.RequiredArgsConstructor;
import ru.tidinari.market.service.CartService;
import ru.tidinari.market.web.dto.ActionTypeDto;
import ru.tidinari.market.web.dto.ItemDto;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;

    @GetMapping("/cart/items")
    public ModelAndView getItems() {
        ModelAndView modelAndView = new ModelAndView("cart");
        modelAndView.addObject("items", List.of(
                new ItemDto(1, "Item 1", "Description 1", "", 100, 0),
                new ItemDto(2, "Item 2", "Description 2", "", 200, 0)
        ));
        modelAndView.addObject("total", 300);
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
