package com.cgi.orderapp.order_service.controller;

import lombok.Data;

@Data
public class OrderRequest {
    private Long userId;
    private Long productId;
    private Integer quantity;
}