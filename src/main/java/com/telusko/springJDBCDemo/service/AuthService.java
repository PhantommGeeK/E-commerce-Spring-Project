package com.telusko.springJDBCDemo.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.telusko.springJDBCDemo.dto.AuthResponse;
import com.telusko.springJDBCDemo.dto.LoginRequest;
import com.telusko.springJDBCDemo.dto.SignupRequest;
import com.telusko.springJDBCDemo.model.AppUser;
import com.telusko.springJDBCDemo.model.Authority;
import com.telusko.springJDBCDemo.repo.AuthorityRepo;
import com.telusko.springJDBCDemo.repo.UserRepo;
import com.telusko.springJDBCDemo.security.JwtService;

@Service
public class AuthService {

    private static final String DEFAULT_ROLE = "ROLE_USER";

    @Autowired
    private UserRepo userRepo;

    @Autowired
    private AuthorityRepo authorityRepo;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private UserDetailsService userDetailsService;

    @Autowired
    private JwtService jwtService;

    @Transactional
    public AuthResponse signup(SignupRequest request) {
        validateCredentials(request.getUsername(), request.getPassword());

        if (userRepo.existsByUsername(request.getUsername().trim())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Username already exists");
        }

        Authority authority = authorityRepo.findByName(DEFAULT_ROLE)
                .orElseGet(() -> {
                    Authority role = new Authority();
                    role.setName(DEFAULT_ROLE);
                    return authorityRepo.save(role);
                });

        AppUser user = new AppUser();
        user.setUsername(request.getUsername().trim());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setEnabled(true);
        user.setAuthority(authority);
        userRepo.save(user);

        UserDetails userDetails = userDetailsService.loadUserByUsername(user.getUsername());
        String token = jwtService.generateToken(userDetails);
        return new AuthResponse(token, user.getUsername(), authority.getName());
    }

    public AuthResponse login(LoginRequest request) {
        validateCredentials(request.getUsername(), request.getPassword());

        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUsername().trim(), request.getPassword()));

        AppUser user = userRepo.findByUsername(request.getUsername().trim())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid credentials"));

        UserDetails userDetails = userDetailsService.loadUserByUsername(user.getUsername());
        String token = jwtService.generateToken(userDetails);
        return new AuthResponse(token, user.getUsername(), user.getAuthority().getName());
    }

    private void validateCredentials(String username, String password) {
        if (username == null || username.trim().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "username is required");
        }

        if (password == null || password.length() < 6) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "password must be at least 6 characters");
        }
    }
}