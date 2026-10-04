package com.liulang.springcloud.api;

public record OrderDTO(Long orderId, String productName, UserDTO user) {
}
