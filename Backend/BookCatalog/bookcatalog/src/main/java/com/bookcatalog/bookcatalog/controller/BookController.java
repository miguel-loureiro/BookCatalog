package com.bookcatalog.bookcatalog.controller;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import com.bookcatalog.bookcatalog.exceptions.BookNotFoundException;
import com.bookcatalog.bookcatalog.model.Role;
import com.bookcatalog.bookcatalog.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import com.bookcatalog.bookcatalog.model.Book;
import com.bookcatalog.bookcatalog.service.BookService;

@RequestMapping("/book")
@RestController
public class BookController {

    private static final String UPLOAD_DIR = System.getProperty("user.dir") + "/uploads"; //uploads folder
    private static final long MAX_FILE_SIZE = 2 * 1024 * 1024; // 2MB

    @Autowired
    private BookService bookService;

    @GetMapping("/{type}/{identifier}")
    public ResponseEntity<?> getBook(@PathVariable String type, @PathVariable String identifier) {

        Book book = bookService.getBook(identifier, type);
        return ResponseEntity.ok(book);
    }

    @GetMapping("/all")
    @PreAuthorize("hasRole('SUPER') or hasRole('ADMIN')")
    public ResponseEntity<Page<Book>> getAllBooks(
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "10") int size
    ) throws IOException {

        return bookService.getAllBooks(page, size);
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<Page<Book>> getBooksByUserId(@PathVariable Integer userId,
                                                      @RequestParam(name = "page", defaultValue = "0") int page,
                                                      @RequestParam(name = "size", defaultValue = "10") int size) {

        Page<Book> books = bookService.getBooksByUserId(userId, page, size);

        if (books.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(books);
    }

    @GetMapping("/user/{identifier}")
    public ResponseEntity<Page<Book>> getBooksByUserUsernameOrEmail(@RequestParam String identifier,
                                                                   @RequestParam(name = "page", defaultValue = "0") int page,
                                                                   @RequestParam(name = "size", defaultValue = "10") int size) {

        Page<Book> books = bookService.getBooksByUserIdentifier(identifier, page, size);

        if (books.isEmpty()) {

            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(books);
    }

    @PostMapping(consumes = {"multipart/form-data"})
    public ResponseEntity<?> createBook(@RequestPart("book") Book book, @RequestPart(value = "file", required = false) MultipartFile file) throws IOException {

        return bookService.createBook(book, file);
    }

    @PutMapping(value = "/{type}/{identifier}", consumes = {"multipart/form-data"})
    @PreAuthorize("hasRole('SUPER') or hasRole('ADMIN')")
    public ResponseEntity<?> updateBook(@RequestParam String type, @RequestParam String identifier,
                                        @RequestPart("book") Book bookDetails,
                                        @RequestPart(value = "file", required = false) MultipartFile file) throws IOException {

        String filename = null;

        if (file != null && !file.isEmpty()) {
            if (file.getSize() > MAX_FILE_SIZE) {
                return ResponseEntity.badRequest().body("File size exceeds 2MB size limit");
            }

            filename = System.currentTimeMillis() + "_" + file.getOriginalFilename();
            Path filepath = Paths.get(UPLOAD_DIR, filename);
            Files.createDirectories(filepath.getParent());
            Files.write(filepath, file.getBytes());

            bookDetails.setCoverImage(filename);
        }

        return bookService.updateBook(identifier, type, bookDetails, filename);
    }

    @DeleteMapping("/{type}/{identifier}")
    @PreAuthorize("hasRole('SUPER') or hasRole('ADMIN')")
    public ResponseEntity<?> deleteBook(@RequestParam String type, @RequestParam String identifier) {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User currentUser = (User) authentication.getPrincipal();

        if (currentUser.getRole() != Role.SUPER && currentUser.getRole() != Role.ADMIN) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("You do not have permission to update this book. Only SUPER or ADMIN is allowed.");
        }

        Book book = bookService.getBook(identifier, type);
        if (book == null) {
            return ResponseEntity.notFound().build();
        }

        bookService.deleteBook(identifier, type);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{type}/{identifier}/add-to-user")
    public ResponseEntity<?> addBookToCurrentUser(@PathVariable String type, @PathVariable String identifier) {
        try {
            return bookService.addBookToCurrentUser(identifier, type);
        } catch (BookNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An unexpected error occurred: " + e.getMessage());
        }
    }

    @DeleteMapping("/{type}/{identifier}/remove-from-user")
    public ResponseEntity<?> deleteBookFromCurrentUser(@PathVariable String type, @PathVariable String identifier) {
        try {
            return bookService.deleteBookFromCurrentUser(identifier, type);
        } catch (BookNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An unexpected error occurred: " + e.getMessage());
        }
    }
}
