package com.bookcatalog.bookcatalog.service;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import com.bookcatalog.bookcatalog.model.dto.BookTitleAndAuthorDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.bookcatalog.bookcatalog.model.Book;
import com.bookcatalog.bookcatalog.repository.BookRepository;

@Service
public class BookService {

    @Autowired
    private BookRepository bookRepository;

    public Book createBook(Book book) {
        return bookRepository.save(book);
    }

    public Optional<Book> getBookById(Integer id) {
        return bookRepository.findById(id);
    }

    public List<Book> getAllBooks() {
        return bookRepository.findAll();
    }

    public List<BookTitleAndAuthorDto> getAllBooksTitlesAndAuthors() {
        List<Book> books = bookRepository.findAll();
        return books.stream()
                .map(book -> new BookTitleAndAuthorDto(book.getTitle(), book.getAuthor()))
                .collect(Collectors.toList());
    }

    public Book updateBook(Integer id, Book newDetails, String filename) throws IOException {

        Book book = bookRepository.findById(id).orElseThrow(() -> new RuntimeException("Book not found"));
        book.setTitle(newDetails.getTitle());
        book.setAuthor(newDetails.getAuthor());
        book.setIsbn(newDetails.getIsbn());
        book.setPrice(newDetails.getPrice());
        book.setPublishDate(newDetails.getPublishDate());

        if(filename != null) {

            book.setCoverImage(filename);
        }
        
        return bookRepository.save(book);
    }

    public void deleteBook(int i) {
        Book book = bookRepository.findById(i).orElseThrow(() -> new RuntimeException("Book not found"));
        bookRepository.delete(book);
    }

    public void saveAll(List<Book> books) {

        bookRepository.saveAll(books);
    }

    public List<Book>  getBooksByUserId(Integer id) {

        return bookRepository.findByUserId(id);
    }
}
