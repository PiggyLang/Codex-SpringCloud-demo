package com.liulang.springcloud.order.controller;

import com.liulang.springcloud.api.OrderDTO;
import com.liulang.springcloud.api.UserDTO;
import com.liulang.springcloud.order.client.UserClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/orders")
public class OrderController {

    private final UserClient userClient;

    public OrderController(UserClient userClient) {
        this.userClient = userClient;
    }

    @GetMapping("/{id}")
    public OrderDTO getOrder(@PathVariable("id") Long id) {
        UserDTO user = userClient.getUser(100L);
        return new OrderDTO(id, "Java 面试题", user);
    }
}
