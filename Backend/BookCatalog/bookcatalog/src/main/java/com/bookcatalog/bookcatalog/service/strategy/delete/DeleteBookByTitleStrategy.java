package com.bookcatalog.bookcatalog.service.strategy.delete;

import com.bookcatalog.bookcatalog.model.Book;
import com.bookcatalog.bookcatalog.repository.BookRepository;
import jakarta.persistence.EntityNotFoundException;

import java.io.IOException;
import java.util.Optional;

public class DeleteBookByTitleStrategy implements  DeleteStrategy<Book> {

    private final BookRepository bookRepository;

    public DeleteBookByTitleStrategy(BookRepository bookRepository) {

        this.bookRepository = bookRepository;
    }

    @Override
    public void delete(Book entity) throws IOException {

        Optional<Book> bookOptional = bookRepository.findBookByTitle(entity.getTitle());

        if (bookOptional.isEmpty()) {
            throw new EntityNotFoundException("Book with title " + entity.getTitle() + " not found");
        }

        try {
            bookRepository.delete(entity);
        } catch (Exception e) {

            throw new RuntimeException("An error occurred while deleting the book", e);
        }
    }
}



