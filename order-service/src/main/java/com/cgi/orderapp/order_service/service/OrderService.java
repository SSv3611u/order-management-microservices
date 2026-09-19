package com.cgi.orderapp.order_service.service;

import com.cgi.orderapp.order_service.clients.ProductClient;
import com.cgi.orderapp.order_service.clients.ProductDTO;
import com.cgi.orderapp.order_service.clients.UserClient;
import com.cgi.orderapp.order_service.clients.UserDTO;
import com.cgi.orderapp.order_service.entity.Order;
import com.cgi.orderapp.order_service.repository.OrderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class OrderService {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private UserClient userClient;

    @Autowired
    private ProductClient productClient;

    public Order placeOrder(Long userId, Long productId, Integer quantity) {
        // 1. Validate user exists (throws if Feign call fails / 404s)
        UserDTO user = userClient.getUserById(userId);

        // 2. Validate product exists and check stock
        ProductDTO product = productClient.getProductById(productId);
        if (product.getStockQuantity() < quantity) {
            throw new RuntimeException("Insufficient stock for product: " + product.getName());
        }

        // 3. Reduce stock in product-service
        productClient.reduceStock(productId, quantity);

        // 4. Calculate total and save the order
        Order order = new Order();
        order.setUserId(user.getId());
        order.setProductId(product.getId());
        order.setQuantity(quantity);
        order.setTotalPrice(product.getPrice() * quantity);
        order.setStatus("PLACED");
        order.setOrderDate(LocalDateTime.now());

        return orderRepository.save(order);
    }

    public List<Order> getAllOrders() {
        return orderRepository.findAll();
    }

    public Order getOrderById(Long id) {
        return orderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Order not found with id: " + id));
    }

    public Order cancelOrder(Long id) {
        Order order = getOrderById(id);
        order.setStatus("CANCELLED");
        return orderRepository.save(order);
    }
}