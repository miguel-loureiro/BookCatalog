package com.bookcatalog.bookcatalog.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.bookcatalog.bookcatalog.model.Book;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface BookRepository extends JpaRepository<Book, Integer> {

    Page<Book> findAll(Pageable pageable);

    Book getReferenceById(Integer id);

    Optional<Book> findBookByTitle(String title);

    Optional<Book> findBookByIsbn(String isbn);

    Page<Book> findBooksByAuthor(String author, Pageable page);

    @Query("SELECT b FROM Book b LEFT JOIN FETCH b.users WHERE b.id = :id")
    Optional<Book> findByIdWithUsers(@Param("id") Integer id);

    @Query("SELECT b FROM Book b LEFT JOIN FETCH b.users WHERE b.isbn = :isbn")
    Optional<Book> findByTitleWithUsers(@Param("title") String isbn);

    @Query("SELECT b FROM Book b LEFT JOIN FETCH b.users WHERE b.isbn = :isbn")
    Optional<Book> findByIsbnWithUsers(@Param("isbn") String isbn);
}
