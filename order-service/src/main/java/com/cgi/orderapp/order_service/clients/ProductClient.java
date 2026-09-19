package com.cgi.orderapp.order_service.clients;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

@FeignClient(name = "product-service")
public interface ProductClient {

    @GetMapping("/api/products/{id}")
    ProductDTO getProductById(@PathVariable("id") Long id);

    @PutMapping("/api/products/{id}/reduce-stock")
    void reduceStock(@PathVariable("id") Long id, @RequestParam("quantity") int quantity);
}