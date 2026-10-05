package com.liulang.springcloud.api;

import com.fasterxml.jackson.annotation.JsonInclude;

public record OrderDTO(Long orderId, String productName,
                       @JsonInclude(JsonInclude.Include.NON_NULL) UserDTO user,
                       String userStatus, String userMessage) {
}
