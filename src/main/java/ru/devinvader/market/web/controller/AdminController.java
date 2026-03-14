package ru.devinvader.market.web.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;
import ru.devinvader.market.service.AdminService;

import ru.devinvader.market.service.ItemsService;
import ru.devinvader.market.web.dto.ItemDto;
import ru.devinvader.market.web.dto.PagedListItemDto;
import ru.devinvader.market.web.dto.SortTypeDto;

import java.io.IOException;

@Controller
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminController {

    private final ItemsService itemsService;
    private final AdminService adminService;

    @GetMapping
    public ModelAndView getAdminPage(
            @RequestParam(name = "search", required = false, defaultValue = "") String search,
            @RequestParam(name = "sort", required = false, defaultValue = "NO") SortTypeDto sortType,
            @RequestParam(name = "pageNumber", required = false, defaultValue = "0") Integer page,
            @RequestParam(name = "pageSize", required = false, defaultValue = "10") Integer size
    ) {
        ModelAndView modelAndView = new ModelAndView("admin");
        modelAndView.addObject("search", search);
        modelAndView.addObject("sort", sortType.name());
        PagedListItemDto pagedItems = itemsService.getItems(search, sortType, page, size);
        modelAndView.addObject("paging", pagedItems.pagingDto());
        modelAndView.addObject("items", pagedItems.items());
        return modelAndView;
    }

    @GetMapping("/items/new")
    public ModelAndView showAddItemForm() {
        ModelAndView modelAndView = new ModelAndView("add-item");
        modelAndView.addObject("item", ItemDto.empty());
        return modelAndView;
    }

    @PostMapping("/items")
    public ModelAndView addItem(
            @RequestParam String title,
            @RequestParam String description,
            @RequestParam Long price,
            @RequestParam(value = "imageFile", required = false) MultipartFile imageFile
    ) throws IOException {
        adminService.createItem(title, description, price, imageFile);
        return new ModelAndView("redirect:/admin");
    }

    @GetMapping("/items/{id}/edit")
    public ModelAndView showEditItemForm(@PathVariable Long id) {
        ItemDto itemDto = adminService.getItemDtoById(id);
        ModelAndView modelAndView = new ModelAndView("edit-item");
        modelAndView.addObject("item", itemDto);
        return modelAndView;
    }

    @PostMapping("/items/{id}")
    public ModelAndView updateItem(
            @PathVariable Long id,
            @RequestParam String title,
            @RequestParam String description,
            @RequestParam Long price,
            @RequestParam(value = "imageFile", required = false) MultipartFile imageFile
    ) throws IOException {
        adminService.updateItem(id, title, description, price, imageFile);
        return new ModelAndView("redirect:/admin");
    }

    @PostMapping("/items/{id}/delete")
    public ModelAndView deleteItem(@PathVariable Long id) {
        adminService.deleteItemById(id);
        return new ModelAndView("redirect:/admin");
    }
}