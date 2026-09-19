package com.cgi.orderapp.order_service.clients;

import lombok.Data;

@Data
public class ProductDTO {
    private Long id;
    private String name;
    private Double price;
    private Integer stockQuantity;
}