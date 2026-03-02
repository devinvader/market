package ru.tidinari.market.unit.controller;

import org.junit.jupiter.api.Test;
import org.mockito.internal.verification.VerificationModeFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.servlet.ModelAndView;
import ru.tidinari.market.service.ItemsService;
import ru.tidinari.market.web.controller.ItemsController;
import ru.tidinari.market.web.dto.ActionTypeDto;
import ru.tidinari.market.web.dto.SortTypeDto;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;

public class ItemsControllerTest extends BaseControllerTest {
    @Autowired
    private ItemsController itemsController;
    @Autowired
    private ItemsService itemsService;

    @Test
    public void getItems_shouldReturnItemsView() {
        // when
        ModelAndView modelAndView = itemsController.getItems("", SortTypeDto.NO, 0, 10);
        // then
        assertEquals("items", modelAndView.getViewName());
        verify(itemsService, VerificationModeFactory.only()).getItems("", SortTypeDto.NO, 0, 10);
    }

    @Test
    public void getItem_shouldReturnItemView() {
        // when
        ModelAndView modelAndView = itemsController.getItem(1);
        // then
        assertEquals("item", modelAndView.getViewName());
        verify(itemsService, VerificationModeFactory.only()).getItem(1);
    }

    @Test
    public void actOnItem_shouldReturnItemView() {
        // when
        ModelAndView modelAndView = itemsController.actOnItem(1, ActionTypeDto.PLUS);
        // then
        assertEquals("item", modelAndView.getViewName());
        verify(itemsService, VerificationModeFactory.only()).actOnItem(1, ActionTypeDto.PLUS);
    }
}