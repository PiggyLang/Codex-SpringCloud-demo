package com.liulang.springcloud.user.controller;

import com.liulang.springcloud.api.UserDTO;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/users")
public class UserController {

    @GetMapping("/{id}")
    public UserDTO getUser(@PathVariable("id") Long id) {
        return new UserDTO(id, "刘浪");
    }
}
