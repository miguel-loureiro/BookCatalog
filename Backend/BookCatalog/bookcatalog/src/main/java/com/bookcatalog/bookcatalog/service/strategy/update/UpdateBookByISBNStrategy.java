package com.bookcatalog.bookcatalog.service.strategy.update;

import com.bookcatalog.bookcatalog.model.Book;
import com.bookcatalog.bookcatalog.repository.BookRepository;
import jakarta.persistence.EntityNotFoundException;

import java.io.IOException;
import java.util.Optional;

public class UpdateBookByISBNStrategy implements UpdateStrategy<Book> {

    private final BookRepository bookRepository;

    public UpdateBookByISBNStrategy(BookRepository bookRepository) {
        this.bookRepository = bookRepository;
    }

    @Override
    public Book update(Book book, Book newDetails, String filename) throws IOException {

        Optional<Book> bookOptional = bookRepository.findBookByIsbn(book.getIsbn());

        if (bookOptional.isEmpty()) {
            throw new EntityNotFoundException("Book with ISBN " + book.getIsbn() + " not found");
        }

        Book foundBook = bookOptional.get();
        foundBook.setTitle(newDetails.getTitle());
        foundBook.setAuthor(newDetails.getAuthor());
        foundBook.setIsbn(newDetails.getIsbn());
        foundBook.setPrice(newDetails.getPrice());
        foundBook.setPublishDate(newDetails.getPublishDate());

        if (filename != null) {
            foundBook.setCoverImage(filename);
        }

        try {
            return bookRepository.save(book);
        } catch (Exception e) {

            throw new RuntimeException("An error occurred while updating the book", e);
        }
    }
}
