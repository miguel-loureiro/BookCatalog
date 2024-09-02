package com.bookcatalog.bookcatalog.service;

import java.io.IOException;
import java.util.*;

import com.bookcatalog.bookcatalog.exceptions.BookNotFoundException;
import com.bookcatalog.bookcatalog.model.Role;
import com.bookcatalog.bookcatalog.model.User;
import com.bookcatalog.bookcatalog.repository.UserRepository;
import com.bookcatalog.bookcatalog.service.strategy.StrategyFactory;
import com.bookcatalog.bookcatalog.service.strategy.delete.DeleteStrategy;
import com.bookcatalog.bookcatalog.service.strategy.update.UpdateStrategy;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import com.bookcatalog.bookcatalog.model.Book;
import com.bookcatalog.bookcatalog.repository.BookRepository;
import org.springframework.web.bind.annotation.RequestParam;

@Service
public class BookService {

    @Autowired
    private final BookRepository bookRepository;
    private final UserRepository userRepository;
    private final StrategyFactory<Book> strategyFactory;
    private final UserService userService;

    @Autowired
    public BookService(UserRepository userRepository, BookRepository bookRepository, StrategyFactory<Book> strategyFactory, UserService userService) {

        this.userRepository = userRepository;
        this.bookRepository = bookRepository;
        this.strategyFactory = strategyFactory;
        this.userService = userService;
    }

    public Book createBook(Book book) {

        if (bookRepository == null) {

            throw new IllegalStateException("BookRepository is not initialized");
        }
        return bookRepository.save(book);
    }

    public Book getBook(String identifier, String type) {

        Optional<User> currentUserOpt = userService.getCurrentUser();
        if (currentUserOpt.isEmpty()) {
            throw new IllegalStateException("Current user not found");
        }

        User currentUser = currentUserOpt.get();
        Book book;

        switch (type) {
            case "id":
                try {
                    book = bookRepository.getReferenceById(Integer.parseInt(identifier));
                } catch (EntityNotFoundException e) {
                    throw new BookNotFoundException("Book not found with id: " + identifier, e);
                }
                break;
            case "title":
                book = bookRepository.findBookByTitle(identifier)
                        .orElseThrow(() -> new EntityNotFoundException("Book with title " + identifier + " not found"));
                break;
            case "isbn":
                book = bookRepository.findBookByIsbn(identifier)
                        .orElseThrow(() -> new EntityNotFoundException("Book with ISBN " + identifier + " not found"));
                break;
            default:
                throw new IllegalArgumentException("Invalid identifier type: " + type);
        }

        if (!hasPermissionToDeleteBook(currentUser) && !hasPermissionToUpdateBook(currentUser)) {
            throw new AccessDeniedException("You do not have permission to perform this action on the book");
        }

        return book;
    }

    public Page<Book> getBooksByAuthor(String author, int page, int size) {

        Pageable pageable = PageRequest.of(page, size);
        return bookRepository.findBooksByAuthor(author, pageable);
    }

    public ResponseEntity<Page<Book>> getAllBooks(
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "10") int size) throws IOException {

        User user ;

        try {

            Optional<User> currentUserOpt = userService.getCurrentUser();

            if(currentUserOpt.isEmpty()) {
                return ResponseEntity.notFound().build();
            }

            user = currentUserOpt.get();
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        if (user.getRole() == null || user.getRole() != Role.SUPER && user.getRole() != Role.ADMIN) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        Pageable paging = PageRequest.of(page, size, Sort.by("author").and(Sort.by("title")).ascending());
        Page<Book> booksPage = bookRepository.findAll(paging);
        return ResponseEntity.ok(booksPage);
    }

    public Page<Book> getBooksByUserId(Integer userId, int page, int size) {

        Optional<User> userOptional = userRepository.findById(userId);

        if (userOptional.isEmpty()) {
            userOptional = userRepository.findById(userId);
        }

        return userOptional.map(user -> {
            List<Book> booksList = new ArrayList<>(user.getBooks());
            return paginateBooks(booksList, page, size);
        }).orElse(Page.empty(PageRequest.of(page, size)));
    }

    public Page<Book> getBooksByUserIdentifier(String identifier, int page, int size) {

        Optional<User> userOptional = userRepository.findByUsername(identifier);

        if (userOptional.isEmpty()) {
            userOptional = userRepository.findByEmail(identifier);
        }

        return userOptional.map(user -> {

            List<Book> booksList = new ArrayList<>(user.getBooks());
            return paginateBooks(booksList, page, size);
        }).orElse(Page.empty(PageRequest.of(page, size)));
    }

    public ResponseEntity<Void> updateBook(String identifier, String type, Book newBookDetails, String filename) {

        Optional<User> currentUserOpt = userService.getCurrentUser();
        if (currentUserOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        User currentUser = currentUserOpt.get();

        if (!hasPermissionToDeleteBook(currentUser)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        try {

            UpdateStrategy<Book> strategy = strategyFactory.getUpdateStrategy(type);
            if (strategy == null) {

                return ResponseEntity.badRequest().build();
            }

            Book existingBook = getBook(identifier, type);
            strategy.update(existingBook, newBookDetails, filename);
            return ResponseEntity.ok().build();
        } catch (BookNotFoundException e) {

            return ResponseEntity.notFound().build();
        } catch (IllegalArgumentException | IOException e) {

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    public ResponseEntity<Void> deleteBook(String identifier, String type) {

        Optional<User> currentUserOpt = userService.getCurrentUser();
        if (currentUserOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        User currentUser = currentUserOpt.get();

        if (!hasPermissionToUpdateBook(currentUser)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        try {

            DeleteStrategy<Book> strategy = strategyFactory.getDeleteStrategy(type);
            if (strategy == null) {

                return ResponseEntity.badRequest().build();
            }
            Book bookToDelete = getBook(identifier, type);
            strategy.delete(bookToDelete);
            return ResponseEntity.ok().build();
        } catch (BookNotFoundException e) {

            return ResponseEntity.notFound().build();
        } catch (IllegalArgumentException | IOException e) {

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    public void saveAll(List<Book> books) {

        bookRepository.saveAll(books);
    }

    public void addBookToCurrentUser(String identifier, String type) {

        Book book = getBook(identifier, type);

        userService.getCurrentUser()
                .map(currentUser -> {
                    currentUser.getBooks().add(book);
                    userRepository.save(currentUser);
                    return ResponseEntity.ok().build();
                })
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    public void deleteBookFromCurrentUser(String identifier, String type) {

        Book book = getBook(identifier, type);

        // Retrieve the current user
        userService.getCurrentUser()
                .map(currentUser -> {

                    if (currentUser.getBooks().remove(book)) {
                        userRepository.save(currentUser);
                        return ResponseEntity.noContent().build();
                    } else {
                        return ResponseEntity.notFound().build();
                    }
                })
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    private Page<Book> paginateBooks(List<Book> books, int page, int size) {

        Sort authorSort = Sort.by("author");
        Sort titleSort = Sort.by("title");
        Sort groupBySort = authorSort.and(titleSort);

        Pageable paging = PageRequest.of(page, size, groupBySort.ascending());

        int start = (int) paging.getOffset();
        int end = Math.min((start + paging.getPageSize()), books.size());


        if (start > books.size()) {

            return new PageImpl<>(Collections.emptyList(), paging, books.size());
        }

        List<Book> subList = books.subList(start, end);
        return new PageImpl<>(subList, paging, books.size());
    }

    private boolean hasPermissionToDeleteBook(User currentUser) {

        return currentUser.getRole() == Role.SUPER || currentUser.getRole() == Role.ADMIN;
    }

    private boolean hasPermissionToUpdateBook(User currentUser) {

        return currentUser.getRole() == Role.SUPER || currentUser.getRole() == Role.ADMIN;
    }
}
