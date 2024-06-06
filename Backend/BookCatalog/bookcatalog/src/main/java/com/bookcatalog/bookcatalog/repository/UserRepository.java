package com.bookcatalog.bookcatalog.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.bookcatalog.bookcatalog.model.User;

public interface UserRepository extends JpaRepository {

    User findByEmail(String email);
    User findByUsername(String username);

}
