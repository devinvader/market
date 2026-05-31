package ru.devinvader.market.web.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;
import ru.devinvader.market.service.AdminService;
import ru.devinvader.market.service.ItemsService;
import ru.devinvader.market.web.dto.AddItemForm;
import ru.devinvader.market.web.dto.ItemDto;
import ru.devinvader.market.web.dto.SortTypeDto;
import ru.devinvader.market.web.dto.UpdateItemForm;

@Controller
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminController {

    private final ItemsService itemsService;
    private final AdminService adminService;

    @GetMapping
    public Mono<String> getAdminPage(
            @RequestParam(name = "search", required = false, defaultValue = "") String search,
            @RequestParam(name = "sort", required = false, defaultValue = "NO") SortTypeDto sortType,
            @RequestParam(name = "pageNumber", required = false, defaultValue = "0") Integer page,
            @RequestParam(name = "pageSize", required = false, defaultValue = "10") Integer size,
            Model model
    ) {
        return itemsService.getItems(search, sortType, page, size, null)
                .map(pagedItems -> {
                    model.addAttribute("search", search);
                    model.addAttribute("sort", sortType.name());
                    model.addAttribute("paging", pagedItems.pagingDto());
                    model.addAttribute("items", pagedItems.items());
                    return "admin";
                });
    }

    @GetMapping("/items/new")
    public Mono<String> showAddItemForm(Model model) {
        model.addAttribute("item", ItemDto.empty());
        return Mono.just("add-item");
    }

    @PostMapping(value = "/items", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Mono<String> addItem(@ModelAttribute AddItemForm itemForm) {
        return adminService.createItem(itemForm.title(), itemForm.description(),
                        itemForm.price(), itemForm.imageFile())
                .thenReturn("redirect:/admin");
    }

    @GetMapping("/items/{id}/edit")
    public Mono<String> showEditItemForm(@PathVariable Long id, Model model) {
        return adminService.getItemDtoById(id)
                .map(itemDto -> {
                    model.addAttribute("item", itemDto);
                    return "edit-item";
                });
    }

    @PostMapping(value = "/items/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Mono<String> updateItem(
            @PathVariable Long id,
            @ModelAttribute UpdateItemForm form
    ) {
        return adminService.updateItem(id, form.title(), form.description(), form.price(), form.imageFile())
                .thenReturn("redirect:/admin");
    }

    @PostMapping("/items/{id}/delete")
    public Mono<String> deleteItem(@PathVariable Long id) {
        return adminService.deleteItemById(id)
                .thenReturn("redirect:/admin");
    }
}