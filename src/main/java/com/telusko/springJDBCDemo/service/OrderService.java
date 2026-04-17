package com.telusko.springJDBCDemo.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.telusko.springJDBCDemo.dto.OrderItemResponse;
import com.telusko.springJDBCDemo.dto.OrderResponse;
import com.telusko.springJDBCDemo.dto.PlaceOrderRequest;
import com.telusko.springJDBCDemo.model.Cart;
import com.telusko.springJDBCDemo.model.CartItem;
import com.telusko.springJDBCDemo.model.CustomerOrder;
import com.telusko.springJDBCDemo.model.OrderItem;
import com.telusko.springJDBCDemo.model.Product;
import com.telusko.springJDBCDemo.repo.CartRepo;
import com.telusko.springJDBCDemo.repo.OrderRepo;
import com.telusko.springJDBCDemo.repo.ProductRepo;

@Service
public class OrderService {

    @Autowired
    private OrderRepo orderRepo;

    @Autowired
    private CartRepo cartRepo;

    @Autowired
    private ProductRepo productRepo;

    @Transactional
    public OrderResponse placeOrder(PlaceOrderRequest request) {
        if (request.getCartId() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "cartId is required");
        }

        Cart cart = cartRepo.findById(request.getCartId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Cart not found"));

        if (cart.getItems().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Cart is empty");
        }

        CustomerOrder order = new CustomerOrder();
        order.setCreatedAt(LocalDateTime.now());
        order.setStatus("PLACED");

        ArrayList<OrderItem> orderItems = new ArrayList<>();
        BigDecimal totalAmount = BigDecimal.ZERO;
        int totalItems = 0;

        for (CartItem cartItem : cart.getItems()) {
            Product product = cartItem.getProduct();
            int requestedQuantity = cartItem.getQuantity();

            if (product.getQuantity() < requestedQuantity) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "Insufficient quantity for product: " + product.getName());
            }

            product.setQuantity(product.getQuantity() - requestedQuantity);
            product.setAvailable(product.getQuantity() > 0);
            productRepo.save(product);

            OrderItem orderItem = new OrderItem();
            orderItem.setOrder(order);
            orderItem.setProductId(product.getId());
            orderItem.setProductName(product.getName());
            orderItem.setBrand(product.getBrand());
            orderItem.setUnitPrice(product.getPrice());
            orderItem.setQuantity(requestedQuantity);

            BigDecimal lineTotal = product.getPrice().multiply(BigDecimal.valueOf(requestedQuantity));
            orderItem.setLineTotal(lineTotal);

            totalAmount = totalAmount.add(lineTotal);
            totalItems += requestedQuantity;
            orderItems.add(orderItem);
        }

        order.setItems(orderItems);
        order.setTotalAmount(totalAmount);
        order.setTotalItems(totalItems);

        CustomerOrder savedOrder = orderRepo.save(order);

        cart.getItems().clear();
        cartRepo.save(cart);

        return toOrderResponse(savedOrder);
    }

    @Transactional(readOnly = true)
    public List<OrderResponse> getAllOrders() {
        return orderRepo.findAllByOrderByCreatedAtDesc().stream()
                .map(this::toOrderResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<OrderItemResponse> getOrderItemsByOrderId(Long orderId) {
        CustomerOrder order = orderRepo.findById(orderId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Order not found"));

        return order.getItems().stream().map(this::toOrderItemResponse).toList();
    }

    private OrderResponse toOrderResponse(CustomerOrder order) {
        OrderResponse response = new OrderResponse();
        response.setOrderId(order.getId());
        response.setStatus(order.getStatus());
        response.setCreatedAt(order.getCreatedAt());
        response.setTotalItems(order.getTotalItems());
        response.setTotalAmount(order.getTotalAmount());
        response.setItems(order.getItems().stream().map(this::toOrderItemResponse).toList());
        return response;
    }

    private OrderItemResponse toOrderItemResponse(OrderItem item) {
        OrderItemResponse response = new OrderItemResponse();
        response.setProductId(item.getProductId());
        response.setProductName(item.getProductName());
        response.setBrand(item.getBrand());
        response.setUnitPrice(item.getUnitPrice());
        response.setQuantity(item.getQuantity());
        response.setLineTotal(item.getLineTotal());
        return response;
    }
}