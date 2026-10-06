package com.liulang.springcloud.order.controller;

import com.liulang.springcloud.api.OrderDTO;
import com.liulang.springcloud.api.UserDTO;
import com.alibaba.csp.sentinel.slots.block.BlockException;
import com.liulang.springcloud.order.service.UserLookupService;
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

    private final UserLookupService userLookupService;

    public OrderController(UserLookupService userLookupService) {
        this.userLookupService = userLookupService;
    }

    @GetMapping("/{id}")
    public OrderDTO getOrder(@PathVariable("id") Long id) {
        try {
            UserDTO user = userLookupService.getUser();
            if (user == null) {
                log.warn("用户服务返回空响应");
                return new OrderDTO(id, "Java 面试题", null, "UNAVAILABLE", "用户信息暂不可用");
            }
            return new OrderDTO(id, "Java 面试题", user, "AVAILABLE", "查询成功");
        } catch (BlockException exception) {
            log.warn("调用被熔断拦截，直接返回降级结果");
            return unavailableOrder(id);
        } catch (FeignException exception) {
            log.warn("用户服务暂不可用: {}", exception.getMessage());
            return unavailableOrder(id);
        }
    }

    private static OrderDTO unavailableOrder(Long id) {
        return new OrderDTO(id, "Java 面试题", null, "UNAVAILABLE", "用户信息暂不可用");
    }
}
