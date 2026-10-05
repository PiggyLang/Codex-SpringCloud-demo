package com.liulang.springcloud.order.controller;

import com.liulang.springcloud.api.UserDTO;
import com.liulang.springcloud.order.client.UserClient;
import feign.FeignException;
import feign.Request;
import feign.RetryableException;
import feign.Response;
import jakarta.servlet.ServletException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.nio.charset.StandardCharsets;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertSame;
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
                .andExpect(jsonPath("$.user.name").value("刘浪"))
                .andExpect(jsonPath("$.userStatus").value("AVAILABLE"))
                .andExpect(jsonPath("$.userMessage").value("查询成功"));
    }

    @Test
    void returnsOrderWithoutUserWhenRemoteServiceIsUnavailable() throws Exception {
        Request request = Request.create(Request.HttpMethod.GET, "http://user-service/users/100", Map.of(), null,
                StandardCharsets.UTF_8, null);
        Response response = Response.builder().status(503).reason("Service Unavailable").request(request).build();
        when(userClient.getUser(100L)).thenThrow(FeignException.errorStatus("UserClient#getUser", response));

        mockMvc.perform(get("/orders/{id}", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.orderId").value(1))
                .andExpect(jsonPath("$.productName").value("Java 面试题"))
                .andExpect(jsonPath("$.userStatus").value("UNAVAILABLE"))
                .andExpect(jsonPath("$.userMessage").value("用户信息暂不可用"))
                .andExpect(jsonPath("$.user").doesNotHaveJsonPath());
    }

    @Test
    void returnsOrderWithoutUserWhenConnectionFails() throws Exception {
        Request request = Request.create(Request.HttpMethod.GET, "http://user-service/users/100", Map.of(), null,
                StandardCharsets.UTF_8, null);
        when(userClient.getUser(100L)).thenThrow(new RetryableException(0, "connection refused",
                Request.HttpMethod.GET, (Long) null, request));

        mockMvc.perform(get("/orders/{id}", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.orderId").value(1))
                .andExpect(jsonPath("$.productName").value("Java 面试题"))
                .andExpect(jsonPath("$.userStatus").value("UNAVAILABLE"))
                .andExpect(jsonPath("$.userMessage").value("用户信息暂不可用"))
                .andExpect(jsonPath("$.user").doesNotHaveJsonPath());
    }

    @Test
    void returnsOrderWithoutUserWhenRemoteServiceReturnsNoBody() throws Exception {
        when(userClient.getUser(100L)).thenReturn(null);

        mockMvc.perform(get("/orders/{id}", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.orderId").value(1))
                .andExpect(jsonPath("$.productName").value("Java 面试题"))
                .andExpect(jsonPath("$.userStatus").value("UNAVAILABLE"))
                .andExpect(jsonPath("$.userMessage").value("用户信息暂不可用"))
                .andExpect(jsonPath("$.user").doesNotHaveJsonPath());
    }

    @Test
    void doesNotHideProgrammingErrors() {
        IllegalStateException programmingError = new IllegalStateException("programming error");
        when(userClient.getUser(100L)).thenThrow(programmingError);

        ServletException exception = assertThrows(ServletException.class,
                () -> mockMvc.perform(get("/orders/{id}", 1L)));
        assertSame(programmingError, exception.getCause());
    }
}
