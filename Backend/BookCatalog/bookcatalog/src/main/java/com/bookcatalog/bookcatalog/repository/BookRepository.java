package com.bookcatalog.bookcatalog.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.bookcatalog.bookcatalog.model.Book;

import java.util.Optional;

public interface BookRepository extends JpaRepository<Book, Integer> {

    Page<Book> findAll(Pageable pageable);

    Book getReferenceById(Integer id);

    Optional<Book> findBookByTitle(String title);

    Optional<Book> findBookByIsbn(String isbn);

    Page<Book> findBooksByAuthor(String author, Pageable page);
}
