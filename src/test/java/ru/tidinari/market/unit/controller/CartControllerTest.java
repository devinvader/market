package ru.tidinari.market.unit.controller;

import org.junit.jupiter.api.Test;
import org.mockito.internal.verification.VerificationModeFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.servlet.ModelAndView;
import ru.tidinari.market.service.CartService;
import ru.tidinari.market.web.controller.CartController;
import ru.tidinari.market.web.dto.ActionTypeDto;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;

public class CartControllerTest extends BaseControllerTest {
    @Autowired
    private CartController cartController;
    @Autowired
    private CartService cartService;

    @Test
    public void getItems_shouldReturnCartView() {
        // when
        ModelAndView modelAndView = cartController.getItems();
        // then
        assertEquals("cart", modelAndView.getViewName());
        verify(cartService, VerificationModeFactory.only()).getCartItems();
        verify(cartService, VerificationModeFactory.only()).getTotal();
    }

    @Test
    public void actOnItems_shouldReturnCartView() {
        // when
        ModelAndView modelAndView = cartController.actOnItems(1, ActionTypeDto.PLUS);
        // then
        assertEquals("cart", modelAndView.getViewName());
        verify(cartService, VerificationModeFactory.only()).actOnCartItems(1, ActionTypeDto.PLUS);
        verify(cartService, VerificationModeFactory.only()).getTotal();
    }
}