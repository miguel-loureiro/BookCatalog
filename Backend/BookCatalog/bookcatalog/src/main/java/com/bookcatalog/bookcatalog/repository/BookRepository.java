package com.bookcatalog.bookcatalog.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.bookcatalog.bookcatalog.model.Book;

import java.util.Date;
import java.util.List;
import java.util.Optional;

public interface BookRepository extends JpaRepository<Book, Integer> {

    List<Book> findByUserId(Integer userId);
    List<Book> findByUserIdIsNull();
    List<Book> findAll();
    List<Book> findByTitleContainingOrAuthorContaining(String title, String author);
    List<Book> findByPublishDate(Date publishDate);


}
