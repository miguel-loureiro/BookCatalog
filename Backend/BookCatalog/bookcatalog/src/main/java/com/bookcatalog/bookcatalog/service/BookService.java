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
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
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

    @Autowired
    public BookService(UserRepository userRepository, BookRepository bookRepository, StrategyFactory<Book> strategyFactory) {

        this.userRepository = userRepository;
        this.bookRepository = bookRepository;
        this.strategyFactory = strategyFactory;
    }

    public Book createBook(Book book) {

        if (bookRepository == null) {

            throw new IllegalStateException("BookRepository is not initialized");
        }
        return bookRepository.save(book);
    }

    public Book getBook(String identifier, String type) {

        switch (type) {

            case "id":
                try {
                    return bookRepository.getReferenceById(Integer.parseInt(identifier));
                } catch (EntityNotFoundException e) {
                    throw new BookNotFoundException("Book not found with id: " + identifier, e);
                }
            case "title":
                return bookRepository.findBookByTitle(identifier)
                        .orElseThrow(() -> new EntityNotFoundException("Book with title " + identifier + " not found"));
            case "isbn":
                return bookRepository.findBookByIsbn(identifier)
                        .orElseThrow(() -> new EntityNotFoundException("Book with ISBN " + identifier + " not found"));
            default:
                throw new IllegalArgumentException("Invalid identifier type: " + type);
        }
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
            user = getCurrentUser();
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        if (user == null || user.getRole() == null ||
                (user.getRole() != Role.SUPER && user.getRole() != Role.ADMIN)) {
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

        User currentUser = getCurrentUser();

        currentUser.getBooks().add(book);

        userRepository.save(currentUser);
    }

    public void deleteBookFromCurrentUser(String identifier, String type) {

        Book book = getBook(identifier, type);

        User currentUser = getCurrentUser();

        if (!currentUser.getBooks().remove(book)) {

            throw new EntityNotFoundException("Book not found in the user's collection");
        }
        userRepository.save(currentUser);
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

    private User getCurrentUser() {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null) {
            throw new IllegalStateException("Authentication is not set in SecurityContext");
        }

        Object principal = authentication.getPrincipal();

        if (principal == null) {
            throw new IllegalStateException("Principal is null");
        }

        if (principal instanceof UserDetails) {
            String username = ((UserDetails) principal).getUsername();
            return userRepository.findByUsername(username)
                    .orElseThrow(() -> new IllegalStateException("User not found with username " + username));
        }

        throw new IllegalStateException("Principal is not an instance of UserDetails");
    }
}
