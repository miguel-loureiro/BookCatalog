package com.bookcatalog.bookcatalog.service.strategy.delete;

import com.bookcatalog.bookcatalog.model.Book;
import com.bookcatalog.bookcatalog.repository.BookRepository;
import jakarta.persistence.EntityNotFoundException;

import java.io.IOException;
import java.util.Optional;

public class DeleteBookByISBNStrategy implements DeleteStrategy<Book>{

    private final BookRepository bookRepository;

    public DeleteBookByISBNStrategy(BookRepository bookRepository) {
        this.bookRepository = bookRepository;
    }

    @Override
    public void delete(Book book) throws IOException {

        Optional<Book> bookOptional = bookRepository.findBookByIsbn(book.getIsbn());

        if (bookOptional.isEmpty()) {
            throw new EntityNotFoundException("Book with ISBN " + book.getIsbn() + " not found");
        }

        try {
            bookRepository.delete(book);
        } catch (Exception e) {

            throw new RuntimeException("An error occurred while deleting the book", e);
        }
    }
}
