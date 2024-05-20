package com.bookcatalog.bookcatalog.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.bookcatalog.bookcatalog.model.Book;

public interface BookRepository extends JpaRepository<Book, Integer> {

}
