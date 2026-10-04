package com.liulang.springcloud.order.client;

import com.liulang.springcloud.api.UserDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient("user-service")
public interface UserClient {

    @GetMapping("/users/{id}")
    UserDTO getUser(@PathVariable("id") Long id);
}
