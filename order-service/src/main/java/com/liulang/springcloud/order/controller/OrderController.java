package com.liulang.springcloud.order.controller;

import com.liulang.springcloud.api.OrderDTO;
import com.liulang.springcloud.api.UserDTO;
import com.liulang.springcloud.order.client.UserClient;
import feign.FeignException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/orders")
public class OrderController {

    private static final Logger log = LoggerFactory.getLogger(OrderController.class);

    private final UserClient userClient;

    public OrderController(UserClient userClient) {
        this.userClient = userClient;
    }

    @GetMapping("/{id}")
    public OrderDTO getOrder(@PathVariable("id") Long id) {
        try {
            UserDTO user = userClient.getUser(100L);
            if (user == null) {
                log.warn("用户服务返回空响应");
                return new OrderDTO(id, "Java 面试题", null, "UNAVAILABLE", "用户信息暂不可用");
            }
            return new OrderDTO(id, "Java 面试题", user, "AVAILABLE", "查询成功");
        } catch (FeignException exception) {
            log.warn("用户服务暂不可用: {}", exception.getMessage());
            return new OrderDTO(id, "Java 面试题", null, "UNAVAILABLE", "用户信息暂不可用");
        }
    }
}
