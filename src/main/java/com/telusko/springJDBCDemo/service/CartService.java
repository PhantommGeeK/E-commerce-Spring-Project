package com.telusko.springJDBCDemo.service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.telusko.springJDBCDemo.dto.AddToCartRequest;
import com.telusko.springJDBCDemo.dto.CartItemResponse;
import com.telusko.springJDBCDemo.dto.CartResponse;
import com.telusko.springJDBCDemo.dto.UpdateCartItemRequest;
import com.telusko.springJDBCDemo.model.Cart;
import com.telusko.springJDBCDemo.model.CartItem;
import com.telusko.springJDBCDemo.model.Product;
import com.telusko.springJDBCDemo.repo.CartRepo;
import com.telusko.springJDBCDemo.repo.ProductRepo;

@Service
public class CartService {

    @Autowired
    private CartRepo cartRepo;

    @Autowired
    private ProductRepo productRepo;

    @Transactional
    public CartResponse addItemToCart(AddToCartRequest request) {
        if (request.getProductId() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "productId is required");
        }

        int quantityToAdd = request.getQuantity() == null ? 1 : request.getQuantity();
        if (quantityToAdd <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "quantity must be greater than 0");
        }

        Product product = productRepo.findById(request.getProductId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Product not found"));

        Cart cart = resolveCart(request.getCartId());

        Optional<CartItem> existingItem = cart.getItems().stream()
                .filter(item -> item.getProduct().getId() == product.getId())
                .findFirst();

        if (existingItem.isPresent()) {
            CartItem item = existingItem.get();
            item.setQuantity(item.getQuantity() + quantityToAdd);
        } else {
            CartItem item = new CartItem();
            item.setCart(cart);
            item.setProduct(product);
            item.setQuantity(quantityToAdd);
            cart.getItems().add(item);
        }

        Cart savedCart = cartRepo.save(cart);
        return toCartResponse(savedCart);
    }

    @Transactional(readOnly = true)
    public CartResponse getCart(Long cartId) {
        Cart cart = cartRepo.findById(cartId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Cart not found"));
        return toCartResponse(cart);
    }

    @Transactional
    public CartResponse updateItemQuantity(Long cartId, Integer productId, UpdateCartItemRequest request) {
        if (request == null || request.getQuantity() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "quantity is required");
        }

        int quantity = request.getQuantity();
        if (quantity <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "quantity must be greater than 0");
        }

        Cart cart = cartRepo.findById(cartId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Cart not found"));

        CartItem cartItem = cart.getItems().stream()
                .filter(item -> item.getProduct().getId() == productId)
                .findFirst()
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Cart item not found"));

        cartItem.setQuantity(quantity);
        Cart savedCart = cartRepo.save(cart);
        return toCartResponse(savedCart);
    }

    @Transactional
    public CartResponse removeItemFromCart(Long cartId, Integer productId) {
        Cart cart = cartRepo.findById(cartId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Cart not found"));

        boolean removed = cart.getItems().removeIf(item -> item.getProduct().getId() == productId);
        if (!removed) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Cart item not found");
        }

        Cart savedCart = cartRepo.save(cart);
        return toCartResponse(savedCart);
    }

    private Cart resolveCart(Long cartId) {
        if (cartId == null) {
            return cartRepo.save(new Cart());
        }

        return cartRepo.findById(cartId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Cart not found"));
    }

    private CartResponse toCartResponse(Cart cart) {
        CartResponse response = new CartResponse();
        response.setCartId(cart.getId());

        ArrayList<CartItemResponse> itemResponses = new ArrayList<>();
        BigDecimal totalAmount = BigDecimal.ZERO;
        int totalItems = 0;

        for (CartItem item : cart.getItems()) {
            CartItemResponse itemResponse = new CartItemResponse();
            itemResponse.setProductId(item.getProduct().getId());
            itemResponse.setProductName(item.getProduct().getName());
            itemResponse.setBrand(item.getProduct().getBrand());
            itemResponse.setUnitPrice(item.getProduct().getPrice());
            itemResponse.setQuantity(item.getQuantity());

            BigDecimal lineTotal = item.getProduct().getPrice().multiply(BigDecimal.valueOf(item.getQuantity()));
            itemResponse.setLineTotal(lineTotal);

            totalAmount = totalAmount.add(lineTotal);
            totalItems += item.getQuantity();
            itemResponses.add(itemResponse);
        }

        response.setItems(itemResponses);
        response.setTotalAmount(totalAmount);
        response.setTotalItems(totalItems);
        return response;
    }
}
