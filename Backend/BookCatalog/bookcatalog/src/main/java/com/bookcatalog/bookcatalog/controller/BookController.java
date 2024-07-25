package com.bookcatalog.bookcatalog.controller;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Set;

import com.bookcatalog.bookcatalog.model.CustomUserDetails;
import com.bookcatalog.bookcatalog.model.Role;
import com.bookcatalog.bookcatalog.model.User;
import com.bookcatalog.bookcatalog.model.dto.BookShortDto;
import org.springframework.beans.factory.annotation.Autowired;
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

    @GetMapping("/{id}")
    public ResponseEntity<Book> getBookById(@PathVariable Integer id) {

        return bookService.getBookById(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/all")
    @PreAuthorize("hasRole('SUPER') or hasRole('ADMIN')")
    public ResponseEntity<List<Book>> getAllBooks() {
        return bookService.getAllBooks();
    }

    @GetMapping("/allshort")
    @PreAuthorize("hasRole('SUPER') or hasRole('ADMIN')")
    public ResponseEntity<List<BookShortDto>>getAllBooksShort() {
        return bookService.getAllBooksShort();
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<Set<BookShortDto>> getBooksByUserId(@PathVariable Integer userId) {

        Set<BookShortDto> books = bookService.getBooksByUserId(userId);
        if (books.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(books);
    }

    @GetMapping("/user")
    public ResponseEntity<Set<BookShortDto>> getBooksByUserUsernameOrEmail(@RequestParam String identifier) {

        Set<BookShortDto> books = bookService.getBooksByUserIdentifier(identifier);
        if (books.isEmpty()) {

            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(books);
    }

    @PostMapping(consumes = {"multipart/form-data"})
    public ResponseEntity<?> createBook(@RequestPart("book") Book book, @RequestPart(value = "file", required = false) MultipartFile file) throws IOException {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User currentUser = (User) authentication.getPrincipal();

        if (currentUser.getRole() != Role.SUPER && currentUser.getRole() != Role.ADMIN) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("You do not have permission to create book. Only SUPER or ADMIN is allowed.");
        }

        if(file != null) {
            if (file.getSize() > MAX_FILE_SIZE) {
                return ResponseEntity.badRequest().body("File size exceeds 2MB size limit");
            }

            String filename = System.currentTimeMillis() + "_" + file.getOriginalFilename();
            Path filepath = Paths.get(UPLOAD_DIR, filename);
            Files.createDirectories(filepath.getParent());
            Files.write(filepath, file.getBytes());

            book.setCoverImage(filename);
        }

        final Book savedBook = bookService.createBook(book);
        return ResponseEntity.ok(savedBook);
    }

    @PutMapping(value = "/{id}", consumes = {"multipart/form-data"})
    @PreAuthorize("hasRole('SUPER') or hasRole('ADMIN')")
    public ResponseEntity<?> updateBook(@PathVariable Integer id, @RequestPart("book") Book bookDetails,
            @RequestPart(value = "file", required = false) MultipartFile file) throws IOException {

        Book existingBook = bookService.getBookById(id).orElse(null);

        if (existingBook == null) {

            return ResponseEntity.notFound().build();
        }

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User currentUser = (User) authentication.getPrincipal();

        if (currentUser.getRole() != Role.SUPER && currentUser.getRole() != Role.ADMIN) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("You do not have permission to update this book. Only SUPER or ADMIN is allowed.");
        }

        if (file != null && !file.isEmpty()) {

            if (file.getSize() > MAX_FILE_SIZE) {
                return ResponseEntity.badRequest().body("File size exceeds 2MB size limit");
            }

            String filename = System.currentTimeMillis() + "_" + file.getOriginalFilename();
            Path filepath = Paths.get(UPLOAD_DIR, filename);
            Files.createDirectories(filepath.getParent());
            Files.write(filepath, file.getBytes());

            bookDetails.setCoverImage(filename);

        }

        bookDetails.setId(existingBook.getId());  // Ensure that the Id of the book remains the same

        final Book updatedBook = bookService.updateBook(id, bookDetails, file != null ? bookDetails.getCoverImage() : null);
        return ResponseEntity.ok(updatedBook);
        }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('SUPER') or hasRole('ADMIN')")
    public ResponseEntity<?> deleteBook(@PathVariable Integer id) {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User currentUser = (User) authentication.getPrincipal();

        if (currentUser.getRole() != Role.SUPER && currentUser.getRole() != Role.ADMIN) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("You do not have permission to update this book. Only SUPER or ADMIN is allowed.");
        }

        Book book = bookService.getBookById(id).orElse(null);
        if (book == null) {
            return ResponseEntity.notFound().build();
        }

        bookService.deleteBookById(id);
        return ResponseEntity.noContent().build();
    }
}
