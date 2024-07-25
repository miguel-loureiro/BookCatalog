package com.bookcatalog.bookcatalog.service;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import com.bookcatalog.bookcatalog.model.Role;
import com.bookcatalog.bookcatalog.model.User;
import com.bookcatalog.bookcatalog.model.dto.BookShortDto;
import com.bookcatalog.bookcatalog.model.dto.BookTitleAndAuthorDto;
import com.bookcatalog.bookcatalog.model.dto.UserDto;
import com.bookcatalog.bookcatalog.model.dto.UserShortDto;
import com.bookcatalog.bookcatalog.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import com.bookcatalog.bookcatalog.model.Book;
import com.bookcatalog.bookcatalog.repository.BookRepository;

@Service
public class BookService {

    @Autowired
    private BookRepository bookRepository;
    private UserRepository userRepository;

    public BookService(UserRepository userRepository, BookRepository bookRepository) {

        this.userRepository = userRepository;
        this.bookRepository = bookRepository;
    }

    public Book createBook(Book book) {

        if (bookRepository == null) {

            throw new IllegalStateException("BookRepository is not initialized");
        }
        return bookRepository.save(book);
    }

    public Optional<Book> getBookById(Integer id) {
        return bookRepository.findById(id);
    }

    public ResponseEntity<List<Book>> getAllBooks() {

        UserDto user = getCurrentUser();

        if (user == null || user.getRole() == null ||
                (user.getRole() != Role.SUPER && user.getRole() != Role.ADMIN)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        List<Book> books = bookRepository.findAll();
        return ResponseEntity.ok(books);
    }

    public ResponseEntity<List<BookShortDto>> getAllBooksShort() {

        UserDto user = getCurrentUser();

        if (user == null || user.getRole() == null ||
                (user.getRole() != Role.SUPER && user.getRole() != Role.ADMIN)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        List<Book> books = bookRepository.findAll();

        if (books == null) {

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }

        List<BookShortDto> booksCompactList = books.stream()
                .map(book -> new BookShortDto(
                        book.getTitle(),
                        book.getAuthor(),
                        book.getIsbn(),
                        book.getPublishDate(),
                        book.getPrice()))
                .collect(Collectors.toList());


        return ResponseEntity.ok(booksCompactList);
    }

    public Set<BookShortDto> getBooksByUserId(Integer userId) {

        Optional<User> userOptional = userRepository.findById(userId);
        return userOptional.map(User::getBooks).orElse(Collections.emptySet());
    }

    public Set<BookShortDto> getBooksByUserIdentifier(String identifier) {

        Optional<User> userOptional = userRepository.findByUsername(identifier);

        if (userOptional.isEmpty()) {
            userOptional = userRepository.findByEmail(identifier);
        }

      return userOptional.map(User::getBooks).orElse(Collections.emptySet());
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

    public void deleteBookById(int i) {
        Book book = bookRepository.findById(i).orElseThrow(() -> new RuntimeException("Book not found"));
        bookRepository.delete(book);
    }

    public void saveAll(List<Book> books) {

        bookRepository.saveAll(books);
    }

    private UserDto getCurrentUser() {

        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        if (principal instanceof UserDetails) {
            String username = ((UserDetails) principal).getUsername();
            return userRepository.findByUsername(username).map(this::fromUserToUserDto).orElse(null);
        }
        return null;
    }

    private UserDto fromUserToUserDto(User user) {

        UserDto userDto = new UserDto();
        userDto.setUsername(user.getUsername());
        userDto.setEmail(user.getEmail());
        userDto.setRole(user.getRole());

        return userDto;
    }
}
