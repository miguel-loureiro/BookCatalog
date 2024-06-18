package com.bookcatalog.bookcatalog.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.repository.CrudRepository;

import com.bookcatalog.bookcatalog.model.Role;
import com.bookcatalog.bookcatalog.model.User;

public interface UserRepository extends CrudRepository<User, Integer> {

    Optional<User> findByEmail(String email);
    Optional<User> findByUsername(String username);
    Optional<User> findByRole(Role role);
}
