package ru.tidinari.market.unit.controller;

import org.junit.jupiter.api.Test;
import org.mockito.internal.verification.VerificationModeFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.servlet.ModelAndView;
import ru.tidinari.market.service.OrderService;
import ru.tidinari.market.web.controller.OrderController;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;

public class OrderControllerTest extends BaseControllerTest {
    @Autowired
    private OrderController orderController;
    @Autowired
    private OrderService orderService;

    @Test
    public void getOrders_shouldReturnOrdersView() {
        // when
        ModelAndView modelAndView = orderController.getOrders();
        // then
        assertEquals("orders", modelAndView.getViewName());
        verify(orderService, VerificationModeFactory.only()).getOrders();
    }

    public void getOrder_shouldReturnOrderView() {
        // when
        ModelAndView modelAndView = orderController.getOrder(1, false);
        // then
        assertEquals("order", modelAndView.getViewName());
        verify(orderService, VerificationModeFactory.only()).getOrder(1, false);
    }
}
