package com.bookcatalog.bookcatalog.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.bookcatalog.bookcatalog.model.Book;

import java.util.List;

public interface BookRepository extends JpaRepository<Book, Integer> {

    List<Book> findByUserId(Integer userId);
}
