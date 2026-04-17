package com.telusko.springJDBCDemo.dto;

public class PlaceOrderRequest {

    private Long cartId;

    public Long getCartId() {
        return cartId;
    }

    public void setCartId(Long cartId) {
        this.cartId = cartId;
    }
}