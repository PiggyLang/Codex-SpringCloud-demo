package com.liulang.springcloud.order.controller;

import com.liulang.springcloud.api.UserDTO;
import com.liulang.springcloud.order.client.UserClient;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(OrderController.class)
class OrderControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserClient userClient;

    @Test
    void returnsOrderWithUserFromRemoteService() throws Exception {
        when(userClient.getUser(100L)).thenReturn(new UserDTO(100L, "刘浪"));

        mockMvc.perform(get("/orders/{id}", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.orderId").value(1))
                .andExpect(jsonPath("$.productName").value("Java 面试题"))
                .andExpect(jsonPath("$.user.id").value(100))
                .andExpect(jsonPath("$.user.name").value("刘浪"));
    }
}
