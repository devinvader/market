package ru.tidinari.market.unit.controller;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import ru.tidinari.market.service.CartService;
import ru.tidinari.market.service.ItemsService;
import ru.tidinari.market.service.OrderService;

@SpringBootTest
public class BaseControllerTest {
    @MockitoBean
    private OrderService orderService;
    @MockitoBean
    private ItemsService itemsService;
    @MockitoBean
    private CartService cartService;
}
