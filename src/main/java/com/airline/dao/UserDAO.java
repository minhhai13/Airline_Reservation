package com.airline.dao;

import com.airline.entity.User;
import java.util.List;
import java.util.Optional;

/**
 * UserDAO Interface Data Access Object for User entity
 */
public interface UserDAO {

    User save(User user);

    Optional<User> findById(Long id);

    Optional<User> findByUsername(String username);

    Optional<User> findByEmail(String email);

    List<User> findAll();

    List<User> findByRole(User.UserRole role);

    void delete(User user);

    boolean existsByUsername(String username);

    boolean existsByEmail(String email);
}
