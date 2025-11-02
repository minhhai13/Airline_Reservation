// ========================================
// IMPLEMENTATION
// ========================================
package com.airline.service.impl;

import com.airline.dao.UserDAO;
import com.airline.dto.UserRegistrationDTO;
import com.airline.entity.User;
import com.airline.service.UserService;
import com.airline.exception.ResourceNotFoundException;
import com.airline.exception.InvalidCredentialsException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import java.util.Optional;

/**
 * UserServiceImpl Implementation with @Transactional for atomic operations
 */
@Service
@Transactional
public class UserServiceImpl implements UserService {

    private final UserDAO userDAO;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public UserServiceImpl(UserDAO userDAO, PasswordEncoder passwordEncoder) {
        this.userDAO = userDAO;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public User registerUser(UserRegistrationDTO dto) {
        // Validate username uniqueness
        if (userDAO.existsByUsername(dto.getUsername())) {
            throw new IllegalArgumentException("Username already exists");
        }

        // Validate email uniqueness
        if (userDAO.existsByEmail(dto.getEmail())) {
            throw new IllegalArgumentException("Email already exists");
        }

        // Create new user
        User user = User.builder()
                .username(dto.getUsername())
                .password(passwordEncoder.encode(dto.getPassword()))
                .email(dto.getEmail())
                .fullName(dto.getFullName())
                .role(User.UserRole.USER)
                .build();

        return userDAO.save(user);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<User> authenticateUser(String username, String password) {
        Optional<User> userOpt = userDAO.findByUsername(username);

        if (userOpt.isPresent()) {
            User user = userOpt.get();
            if (passwordEncoder.matches(password, user.getPassword())) {
                return Optional.of(user);
            }
        }
        throw new InvalidCredentialsException("Invalid username or password");
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<User> findById(Long id) {
        return userDAO.findById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<User> findByUsername(String username) {
        return userDAO.findByUsername(username);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<User> findByEmail(String email) {
        return userDAO.findByEmail(email);
    }

    @Override
    @Transactional(readOnly = true)
    public List<User> findAllUsers() {
        return userDAO.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public List<User> findUsersByRole(User.UserRole role) {
        return userDAO.findByRole(role);
    }

    @Override
    public User updateUser(User user) {
        return userDAO.save(user);
    }

    @Override
    public void deleteUser(Long id) {
        User user = userDAO.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        userDAO.delete(user);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsByUsername(String username) {
        return userDAO.existsByUsername(username);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsByEmail(String email) {
        return userDAO.existsByEmail(email);
    }
}
