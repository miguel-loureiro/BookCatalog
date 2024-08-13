package com.bookcatalog.bookcatalog.service;

import java.io.IOException;
import java.util.*;

import com.bookcatalog.bookcatalog.exceptions.BookNotFoundException;
import com.bookcatalog.bookcatalog.model.Role;
import com.bookcatalog.bookcatalog.model.User;
import com.bookcatalog.bookcatalog.repository.UserRepository;
import com.bookcatalog.bookcatalog.service.strategy.delete.DeleteBookByISBNStrategy;
import com.bookcatalog.bookcatalog.service.strategy.delete.DeleteBookByIdStrategy;
import com.bookcatalog.bookcatalog.service.strategy.delete.DeleteBookByTitleStrategy;
import com.bookcatalog.bookcatalog.service.strategy.delete.DeleteStrategy;
import com.bookcatalog.bookcatalog.service.strategy.update.UpdateBookByISBNStrategy;
import com.bookcatalog.bookcatalog.service.strategy.update.UpdateBookByIdStrategy;
import com.bookcatalog.bookcatalog.service.strategy.update.UpdateBookByTitleStrategy;
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

    private Map<String, UpdateStrategy> updateStrategies;
    private Map<String, DeleteStrategy> deleteStrategies;

    @Autowired
    public BookService(UserRepository userRepository, BookRepository bookRepository, List<UpdateStrategy> updatestrategies, List<DeleteStrategy> deletestrategies) {

        this.userRepository = userRepository;
        this.bookRepository = bookRepository;

        // Register all update strategies
        updateStrategies = new HashMap<>();
        updatestrategies.forEach(updatestrategy -> {

            if (updatestrategy instanceof UpdateBookByIdStrategy) {
                updateStrategies.put("id", updatestrategy);
            } else if (updatestrategy instanceof UpdateBookByTitleStrategy) {
                updateStrategies.put("title", updatestrategy);
            } else if (updatestrategy instanceof UpdateBookByISBNStrategy) {
                updateStrategies.put("isbn", updatestrategy);
            }
        });

        // Register all delete strategies
        deleteStrategies = new HashMap<>();
        deletestrategies.forEach(deletestrategy -> {

            if (deletestrategy instanceof DeleteBookByIdStrategy) {
                deleteStrategies.put("id", deletestrategy);
            } else if (deletestrategy instanceof DeleteBookByTitleStrategy) {
                deleteStrategies.put("title", deletestrategy);
            } else if (deletestrategy instanceof DeleteBookByISBNStrategy) {
                deleteStrategies.put("isbn", deletestrategy);
            }
        });
    }

    @Autowired
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

    public Book getBookByBookId(Integer id) {

        try {

            return bookRepository.getReferenceById(id);
        } catch (EntityNotFoundException e) {

            throw new BookNotFoundException("Book not found with id: " + id, e);
        }
    }

    public Optional<Book> getBookByIsbn(String isbn) {

        return bookRepository.findBookByIsbn(isbn);
    }

    public Optional<Book> getBookByTitle(String title) {

        return bookRepository.findBookByTitle(title);
    }

    public Page<Book> getBooksByAuthor(String author, int page, int size) {

        Pageable pageable = PageRequest.of(page, size);
        return bookRepository.findBooksByAuthor(author, pageable);
    }


    public ResponseEntity<Page<Book>> getAllBooks(
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "10") int size) throws IOException {

        User user;
        try {
            user = getCurrentUser();
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        if (user == null || user.getRole() == null ||
                (user.getRole() != Role.SUPER && user.getRole() != Role.ADMIN)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        Sort authorSort = Sort.by("author");
        Sort titleSort = Sort.by("title");
        Sort groupBySort = authorSort.and(titleSort);

        Pageable paging = PageRequest.of(page, size, groupBySort.ascending());

        try {

            Page<Book> booksPage = bookRepository.findAll(paging);
            return ResponseEntity.ok(booksPage);
        } catch (Exception e) {

            throw new RuntimeException("An error occurred while fetching books", e);
        }
    }

    public Page<Book> getBooksByUserId(Integer userId, int page, int size) {

        Optional<User> userOptional = userRepository.findById(userId);

        Sort authorSort = Sort.by("author");
        Sort titleSort = Sort.by("title");
        Sort groupBySort = authorSort.and(titleSort);

        Pageable paging = PageRequest.of(page, size, groupBySort.ascending());

        if (userOptional.isPresent()) {
            User user = userOptional.get();
            Set<Book> booksSet = new HashSet<>(user.getBooks());

            List<Book> booksList = new ArrayList<>(booksSet); // Converte Set para List para funcionar com a subList

            booksList.sort(Comparator.comparing(Book::getAuthor).thenComparing(Book::getTitle)); // Assegurar a ordenacao, primeiro por autor e depois por titulo

            int start = (int) paging.getOffset();
            int end = Math.min((start + paging.getPageSize()), booksList.size());

            if (start > booksList.size()) {
                return new PageImpl<>(Collections.emptyList(), paging, booksList.size());
            }

            List<Book> subList = booksList.subList(start, end);
            return new PageImpl<>(subList, paging, booksList.size());
        } else {
            // verificacao do indice de inicio. se for maior que o tamanho da lista então retorna uma lista vazia
            return Page.empty(paging);
        }
    }

    public Page<Book> getBooksByUserIdentifier(String identifier, int page, int size) {

        Optional<User> userOptional = userRepository.findByUsername(identifier);

        if (userOptional.isEmpty()) {
            userOptional = userRepository.findByEmail(identifier);
        }

        Sort authorSort = Sort.by("author");
        Sort titleSort = Sort.by("title");
        Sort groupBySort = authorSort.and(titleSort);

        Pageable paging = PageRequest.of(page, size, groupBySort.ascending());

        if (userOptional.isPresent()) {

            User user = userOptional.get();
            List<Book> booksList = new ArrayList<>(user.getBooks());

            int start = (int) paging.getOffset();
            int end = Math.min((start + paging.getPageSize()), booksList.size());


            if (start > booksList.size()) {

                return new PageImpl<>(Collections.emptyList(), paging, booksList.size());
            }

            List<Book> subList = booksList.subList(start, end);
            return new PageImpl<>(subList, paging, booksList.size());
        } else {
            // verificacao do indice de inicio. se for maior que o tamanho da lista então retorna uma lista vazia
            return Page.empty(paging);
        }
    }

    public Book updateBook(String identifier, String type, Book newDetails, String filename) throws IOException {

        UpdateStrategy<Book> strategy = updateStrategies.get(type);

        if (strategy == null) {
            throw new IllegalArgumentException("Invalid update type: " + type);
        }

        Book bookToUpdate;

        switch (type.toLowerCase()) {
            case "id":
                bookToUpdate = bookRepository.getReferenceById(Integer.valueOf(identifier));
                break;
            case "title":
                bookToUpdate = bookRepository.findBookByTitle(identifier)
                        .orElseThrow(() -> new EntityNotFoundException("Book not found with title: " + identifier));
                break;
            case "isbn":
                bookToUpdate = bookRepository.findBookByIsbn(identifier)
                        .orElseThrow(() -> new EntityNotFoundException("Book not found with ISBN: " + identifier));
                break;
            default:
                throw new IllegalArgumentException("Invalid update type: " + type);
        }

        return strategy.update(bookToUpdate, newDetails, filename);

    }

    public void deleteBook(String identifier, String type) throws IOException {

        DeleteStrategy strategy = deleteStrategies.get(type);

        if (strategy == null) {
            throw new IllegalArgumentException("Invalid update type: " + type);
        }

        Book bookToDelete = getBookByIdentifier(identifier, type);

        strategy.delete(bookToDelete);
    }

    public void saveAll(List<Book> books) {

        bookRepository.saveAll(books);
    }

    public void addBookToCurrentUser(String identifier, String type) {

        Book book = getBookByIdentifier(identifier, type);

        User currentUser = getCurrentUser();

        currentUser.getBooks().add(book);

        userRepository.save(currentUser);
    }

    public void deleteBookFromCurrentUser(String identifier, String type) {

        Book book = getBookByIdentifier(identifier, type);

        User currentUser = getCurrentUser();

        if (!currentUser.getBooks().remove(book)) {

            throw new EntityNotFoundException("Book not found in the user's collection");
        }
        userRepository.save(currentUser);
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

    private Book getBookByIdentifier(String identifier, String type) {

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
}
