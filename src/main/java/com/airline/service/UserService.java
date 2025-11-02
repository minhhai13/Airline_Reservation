package com.airline.service;

import com.airline.dto.UserRegistrationDTO;
import com.airline.entity.User;
import java.util.List;
import java.util.Optional;

/**
 * UserService Interface Business logic for User management
 */
public interface UserService {

    User registerUser(UserRegistrationDTO registrationDTO);

    Optional<User> authenticateUser(String username, String password);

    Optional<User> findById(Long id);

    Optional<User> findByUsername(String username);

    Optional<User> findByEmail(String email);

    List<User> findAllUsers();

    List<User> findUsersByRole(User.UserRole role);

    User updateUser(User user);

    void deleteUser(Long id);

    boolean existsByUsername(String username);

    boolean existsByEmail(String email);
}
