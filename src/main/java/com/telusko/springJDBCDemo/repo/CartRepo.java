package com.telusko.springJDBCDemo.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.telusko.springJDBCDemo.model.Cart;

@Repository
public interface CartRepo extends JpaRepository<Cart, Long> {
}
