package ru.tidinari.market.unit.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import ru.tidinari.market.service.OrderService;
import ru.tidinari.market.web.controller.OrderController;
import ru.tidinari.market.web.dto.OrderDto;

import java.util.List;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(OrderController.class)
public class OrderControllerTest {

    @MockitoBean
    private OrderService orderService;

    @Autowired
    private MockMvc mockMvc;

    @Test
    public void getOrders_shouldReturnOrdersView() throws Exception {
        // given
        List<OrderDto> expectedOrders = List.of(
                new OrderDto(1L, List.of(), 100)
        );
        when(orderService.getOrders()).thenReturn(expectedOrders);

        // when
        mockMvc.perform(get("/orders"))
        // then
                .andExpect(status().isOk())
                .andExpect(view().name("orders"))
                .andExpect(model().attribute("orders", expectedOrders));

        verify(orderService).getOrders();
    }

    @Test
    public void getOrder_shouldReturnOrderView() throws Exception {
        // given
        OrderDto expectedOrder = new OrderDto(1L, List.of(), 200);
        when(orderService.getOrder(1L, false)).thenReturn(expectedOrder);

        // when
        mockMvc.perform(get("/orders/1"))
        // then
                .andExpect(status().isOk())
                .andExpect(view().name("order"))
                .andExpect(model().attribute("order", expectedOrder))
                .andExpect(model().attribute("newOrder", false));

        verify(orderService).getOrder(1L, false);
    }
}
