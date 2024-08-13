package com.bookcatalog.bookcatalog.service.strategy.delete;

import com.bookcatalog.bookcatalog.model.Book;
import com.bookcatalog.bookcatalog.repository.BookRepository;
import jakarta.persistence.EntityNotFoundException;

import java.io.IOException;

public class DeleteBookByIdStrategy implements DeleteStrategy<Book> {

    private final BookRepository bookRepository;

    public DeleteBookByIdStrategy(BookRepository bookRepository) {
        this.bookRepository = bookRepository;
    }

    @Override
    public void delete(Book entity) throws IOException {

        if (entity == null || entity.getId() == null) {
            throw new EntityNotFoundException("Book not found");
        }

        try {
            bookRepository.delete(entity);
        } catch (EntityNotFoundException e) {
            throw new EntityNotFoundException("Book with ID " + entity.getId() + " not found", e);
        } catch (Exception e) {
            throw new RuntimeException("An error occurred while deleting the book", e);
        }

    }
}
