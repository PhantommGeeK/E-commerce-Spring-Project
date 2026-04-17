package com.telusko.springJDBCDemo.repo;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.telusko.springJDBCDemo.model.Authority;

@Repository
public interface AuthorityRepo extends JpaRepository<Authority, Long> {

    Optional<Authority> findByName(String name);
}