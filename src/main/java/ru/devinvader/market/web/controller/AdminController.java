package ru.devinvader.market.web.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;
import ru.devinvader.market.service.AdminService;
import ru.devinvader.market.service.ItemsService;
import ru.devinvader.market.web.dto.ItemDto;
import ru.devinvader.market.web.dto.SortTypeDto;

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
        return itemsService.getItems(search, sortType, page, size)
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

    @PostMapping("/items")
    public Mono<String> addItem(
            @RequestParam String title,
            @RequestParam String description,
            @RequestParam Long price,
            @RequestPart(value = "imageFile", required = false) FilePart imageFile
    ) {
        return adminService.createItem(title, description, price, imageFile)
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

    @PostMapping("/items/{id}")
    public Mono<String> updateItem(
            @PathVariable Long id,
            @RequestParam String title,
            @RequestParam String description,
            @RequestParam Long price,
            @RequestPart(value = "imageFile", required = false) FilePart imageFile
    ) {
        return adminService.updateItem(id, title, description, price, imageFile)
                .thenReturn("redirect:/admin");
    }

    @PostMapping("/items/{id}/delete")
    public Mono<String> deleteItem(@PathVariable Long id) {
        return adminService.deleteItemById(id)
                .thenReturn("redirect:/admin");
    }
}