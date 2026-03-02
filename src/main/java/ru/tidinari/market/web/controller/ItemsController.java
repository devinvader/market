package ru.tidinari.market.web.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;
import ru.tidinari.market.service.ItemsService;
import ru.tidinari.market.web.dto.*;

@Controller
@RequiredArgsConstructor
public class ItemsController {

    private final ItemsService itemsService;

    @GetMapping(path = {"/", "/items"})
    public ModelAndView getItems(
            @RequestParam(name = "search", required = false, defaultValue = "") String search,
            @RequestParam(name = "sortType", required = false, defaultValue = "NO") SortTypeDto sortType,
            @RequestParam(name = "pageNumber", required = false, defaultValue = "0") Integer page,
            @RequestParam(name = "pageSize", required = false, defaultValue = "10") Integer size
    ) {
        ModelAndView modelAndView = new ModelAndView("items");
        modelAndView.addObject("search", search);
        modelAndView.addObject("sort", sortType.name());
        modelAndView.addObject("paging", new PagingDto(size, page, page > 1, /* TODO: Убрать заглушку */ false));
        modelAndView.addObject("items", itemsService.getItems(search, sortType, page, size));
        return modelAndView;
    }

    @PostMapping("/items")
        public ModelAndView actOnItems(
            @RequestParam(name = "id") long id,
            @RequestParam(name = "action") ActionTypeDto action,
            @RequestParam(name = "search", required = false, defaultValue = "") String search,
            @RequestParam(name = "sortType", required = false, defaultValue = "NO") SortTypeDto sortType,
            @RequestParam(name = "pageNumber", required = false, defaultValue = "0") Integer page,
            @RequestParam(name = "pageSize", required = false, defaultValue = "10") Integer size
    ) {
        // TODO: Реализовать запрос
        ModelAndView modelAndView = new ModelAndView("redirect:/items");
        modelAndView.addObject("search", search);
        modelAndView.addObject("sort", sortType.name());
        modelAndView.addObject("pageNumber", page);
        modelAndView.addObject("pageSize", size);
        return modelAndView;
    }

    @GetMapping("/items/{id}")
   public ModelAndView getItem(
           @PathVariable(name = "id") long id
   ) {
       ModelAndView modelAndView = new ModelAndView("item");
       modelAndView.addObject("item", itemsService.getItem(id));
       return modelAndView;
   }

    @PostMapping("/items/{id}")
   public ModelAndView actOnItem(
           @PathVariable(name = "id") long id,
           @RequestParam(name = "action") ActionTypeDto action
   ) {
       ModelAndView modelAndView = new ModelAndView("item");
       modelAndView.addObject("item", itemsService.actOnItem(id, action));
       return modelAndView;
   }

    @PostMapping("/buy")
    public ModelAndView buyItems() {
        long orderId = 1;
        ModelAndView modelAndView = new ModelAndView("redirect:/orders/{id}");
        modelAndView.addObject("id", orderId);
        modelAndView.addObject("newOrder", true);
        return modelAndView;
    }
}
