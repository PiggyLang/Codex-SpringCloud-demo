package com.liulang.springcloud.user.controller;

import com.liulang.springcloud.api.UserDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/users")
public class UserController {

    private static final Logger log = LoggerFactory.getLogger(UserController.class);

    @Value("${server.port}")
    private int serverPort;

    @GetMapping("/{id}")
    public UserDTO getUser(@PathVariable("id") Long id) {
        log.info("user-service 实例端口={}，处理用户查询 id={}", serverPort, id);
        return new UserDTO(id, "刘浪");
    }
}
