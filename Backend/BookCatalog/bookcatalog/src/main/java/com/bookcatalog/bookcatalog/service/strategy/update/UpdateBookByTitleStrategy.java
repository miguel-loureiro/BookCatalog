package com.bookcatalog.bookcatalog.service.strategy.update;

import com.bookcatalog.bookcatalog.model.Book;
import com.bookcatalog.bookcatalog.repository.BookRepository;
import jakarta.persistence.EntityNotFoundException;

import java.io.IOException;
import java.util.Optional;

public class UpdateBookByTitleStrategy implements UpdateStrategy<Book> {

    private final BookRepository bookRepository;

    public UpdateBookByTitleStrategy(BookRepository bookRepository) {

        this.bookRepository = bookRepository;
    }

    @Override
    public Book update(Book book, Book newDetails, String filename) throws IOException {

        Optional<Book> bookOptional = bookRepository.findBookByTitle(book.getTitle());

        if (bookOptional.isEmpty()) {
            throw new EntityNotFoundException("Book with title " + book.getTitle() + " not found");
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
