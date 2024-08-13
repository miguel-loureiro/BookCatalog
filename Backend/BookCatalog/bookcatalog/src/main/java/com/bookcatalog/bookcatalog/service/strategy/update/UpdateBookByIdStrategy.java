package com.bookcatalog.bookcatalog.service.strategy.update;

import com.bookcatalog.bookcatalog.model.Book;
import com.bookcatalog.bookcatalog.repository.BookRepository;
import jakarta.persistence.EntityNotFoundException;

import java.io.IOException;

public class UpdateBookByIdStrategy implements UpdateStrategy<Book> {

    private final BookRepository bookRepository;

    public UpdateBookByIdStrategy(BookRepository bookRepository) {

        this.bookRepository = bookRepository;
    }

    public Book update(Book book, Book newDetails, String filename) throws IOException {

        if (book == null) {
            throw new EntityNotFoundException("Book not found");
        }

        if (newDetails == null) {
            throw new IllegalArgumentException("New details cannot be null");
        }

        book.setTitle(newDetails.getTitle());
        book.setAuthor(newDetails.getAuthor());
        book.setIsbn(newDetails.getIsbn());
        book.setPrice(newDetails.getPrice());
        book.setPublishDate(newDetails.getPublishDate());

        if (filename != null) {
            book.setCoverImage(filename);
        }

        try {
            return bookRepository.save(book);
        } catch (Exception e) {

            throw new RuntimeException("An error occurred while updating the book", e);
        }
    }
}
