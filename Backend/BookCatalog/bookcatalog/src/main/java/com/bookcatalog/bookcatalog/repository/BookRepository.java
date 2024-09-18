package com.bookcatalog.bookcatalog.repository;

import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.bookcatalog.bookcatalog.model.Book;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface BookRepository extends JpaRepository<Book, Integer> {

    Page<Book> findAll(Pageable pageable);
    @Lock(LockModeType.OPTIMISTIC_FORCE_INCREMENT)
    Book getReferenceById(Integer id);

    Optional<Book> findBookByTitle(String title);

    Optional<Book> findBookByIsbn(String isbn);

    Page<Book> findBooksByAuthor(String author, Pageable page);

    @Query("SELECT b FROM Book b LEFT JOIN FETCH b.users WHERE b.id = :id")
    Optional<Book> findByIdWithUsers(@Param("id") Integer id);

    @Query("SELECT b FROM Book b LEFT JOIN FETCH b.users WHERE b.title = :title")
    Optional<Book> findByTitleWithUsers(@Param("title") String title);

    @Query("SELECT b FROM Book b LEFT JOIN FETCH b.users WHERE b.isbn = :isbn")
    Optional<Book> findByIsbnWithUsers(@Param("isbn") String isbn);
}
