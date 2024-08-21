package com.bookcatalog.bookcatalog.repository;

import java.util.List;
import java.util.Optional;

import com.bookcatalog.bookcatalog.model.Book;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import com.bookcatalog.bookcatalog.model.Role;
import com.bookcatalog.bookcatalog.model.User;
import org.springframework.data.repository.query.Param;

public interface UserRepository extends JpaRepository<User, Integer> {

    Optional<User> findByEmail(String email);
    Optional<User> findByUsername(String username);
    Page<User> findAll(Pageable pageable);
}
