package ru.tidinari.market.web.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;
import ru.tidinari.market.domain.Item;
import ru.tidinari.market.service.AdminService;
import ru.tidinari.market.service.ImageService;
import ru.tidinari.market.service.ItemsService;
import ru.tidinari.market.web.dto.ItemDto;
import ru.tidinari.market.web.dto.PagingDto;
import ru.tidinari.market.web.dto.SortTypeDto;

import java.io.IOException;
import java.util.List;

@Controller
@RequestMapping("/admin")
public class AdminController {

    @Autowired
    private ItemsService itemsService;

    @Autowired
    private AdminService adminService;

    @Autowired
    private ImageService imageService;

    @GetMapping
    public ModelAndView getAdminPage(
            @RequestParam(name = "search", required = false, defaultValue = "") String search,
            @RequestParam(name = "sortType", required = false, defaultValue = "NO") SortTypeDto sortType,
            @RequestParam(name = "pageNumber", required = false, defaultValue = "0") Integer page,
            @RequestParam(name = "pageSize", required = false, defaultValue = "10") Integer size
    ) {
        ModelAndView modelAndView = new ModelAndView("admin");
        modelAndView.addObject("search", search);
        modelAndView.addObject("sort", sortType.name());
        modelAndView.addObject("paging", new PagingDto(size, page, page > 1, false));
        List<List<ItemDto>> items = itemsService.getItems(search, sortType, page, size);
        modelAndView.addObject("items", items);
        return modelAndView;
    }

    @GetMapping("/items/new")
    public ModelAndView showAddItemForm() {
        ModelAndView modelAndView = new ModelAndView("add-item");
        modelAndView.addObject("item", new ItemDto(-1L, "", "", "", 0L, 0));
        return modelAndView;
    }

    @PostMapping("/items")
    public ModelAndView addItem(
            @RequestParam String title,
            @RequestParam String description,
            @RequestParam Long price,
            @RequestParam(value = "imageFile", required = false) MultipartFile imageFile
    ) throws IOException {
        Item item = new Item();
        item.setTitle(title);
        item.setDescription(description);
        item.setPrice(price);
        Item savedItem = adminService.saveItem(item);

        if (imageFile != null && !imageFile.isEmpty()) {
            imageService.saveImage(savedItem.getId(), imageFile);
        }

        return new ModelAndView("redirect:/admin");
    }

    @GetMapping("/items/{id}/edit")
    public ModelAndView showEditItemForm(@PathVariable Long id) {
        Item item = adminService.findItemById(id);
        ItemDto itemDto = new ItemDto(item.getId(), item.getTitle(), item.getDescription(), imageService.getImageUrl(id), item.getPrice(), 0);
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
        Item item = adminService.findItemById(id);
        item.setTitle(title);
        item.setDescription(description);
        item.setPrice(price);
        adminService.saveItem(item);

        if (imageFile != null && !imageFile.isEmpty()) {
            imageService.saveImage(id, imageFile);
        }

        return new ModelAndView("redirect:/admin");
    }

    @PostMapping("/items/{id}/delete")
    public ModelAndView deleteItem(@PathVariable Long id) {
        adminService.deleteItemById(id);
        return new ModelAndView("redirect:/admin");
    }
}