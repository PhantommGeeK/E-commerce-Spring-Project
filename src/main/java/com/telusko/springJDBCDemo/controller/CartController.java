package com.telusko.springJDBCDemo.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.telusko.springJDBCDemo.dto.AddToCartRequest;
import com.telusko.springJDBCDemo.dto.CartResponse;
import com.telusko.springJDBCDemo.dto.UpdateCartItemRequest;
import com.telusko.springJDBCDemo.service.CartService;

@RestController
@CrossOrigin(origins = "http://localhost:4200")
@RequestMapping("/api/cart")
public class CartController {

    @Autowired
    private CartService cartService;

    @PostMapping("/items")
    public ResponseEntity<CartResponse> addItemToCart(@RequestBody AddToCartRequest request) {
        return ResponseEntity.ok(cartService.addItemToCart(request));
    }

    @GetMapping("/{cartId}")
    public ResponseEntity<CartResponse> viewCart(@PathVariable Long cartId) {
        return ResponseEntity.ok(cartService.getCart(cartId));
    }

    @PutMapping("/{cartId}/items/{productId}")
    public ResponseEntity<CartResponse> updateCartItemQuantity(
            @PathVariable Long cartId,
            @PathVariable Integer productId,
            @RequestBody UpdateCartItemRequest request) {
        return ResponseEntity.ok(cartService.updateItemQuantity(cartId, productId, request));
    }

    @DeleteMapping("/{cartId}/items/{productId}")
    public ResponseEntity<CartResponse> removeCartItem(
            @PathVariable Long cartId,
            @PathVariable Integer productId) {
        return ResponseEntity.ok(cartService.removeItemFromCart(cartId, productId));
    }
}
