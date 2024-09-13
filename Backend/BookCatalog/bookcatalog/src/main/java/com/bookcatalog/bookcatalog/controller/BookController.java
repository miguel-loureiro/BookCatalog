package com.bookcatalog.bookcatalog.controller;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Set;

import com.bookcatalog.bookcatalog.exceptions.BookNotFoundException;
import com.bookcatalog.bookcatalog.model.dto.BookDetailWithoutUserListDto;
import com.bookcatalog.bookcatalog.model.dto.BookDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
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

        try {
            BookDto book = bookService.getBookWithShortUserDetails(identifier, type);
            return ResponseEntity.ok(book);
        } catch (BookNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }

    @GetMapping("/all")
    @PreAuthorize("hasRole('SUPER') or hasRole('ADMIN')")
    public ResponseEntity<Page<BookDto>> getAllBooks(@RequestParam(name = "page", defaultValue = "0") int page,
                                                     @RequestParam(name = "size", defaultValue = "10") int size) throws IOException {

        return bookService.getAllBooks(page, size);
    }

    @GetMapping("/books/only")
    public ResponseEntity<Set<BookDetailWithoutUserListDto>> getBooksOnly(@RequestParam(defaultValue = "0") int page,
                                                                          @RequestParam(defaultValue = "10") int size) {
        return bookService.getOnlyBooks(page, size);
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<Page<BookDetailWithoutUserListDto>> getBooksByUserId(@PathVariable Integer userId,
                                                                               @RequestParam(name = "page", defaultValue = "0") int page,
                                                                               @RequestParam(name = "size", defaultValue = "10") int size) {

        Page<BookDetailWithoutUserListDto> books = bookService.getBooksByUserId(userId, page, size);
        return books.isEmpty() ? ResponseEntity.notFound().build() : ResponseEntity.ok(books);
    }

    @GetMapping("/user/identifier")
    public ResponseEntity<Page<BookDetailWithoutUserListDto>> getBooksByUserIdentifier(@RequestParam String identifier,
                                                                                       @RequestParam(name = "page", defaultValue = "0") int page,
                                                                                       @RequestParam(name = "size", defaultValue = "10") int size) {

        Page<BookDetailWithoutUserListDto> books = bookService.getBooksByUserIdentifier(identifier, page, size);
        return books.isEmpty() ? ResponseEntity.notFound().build() : ResponseEntity.ok(books);
    }

    @PostMapping(value = "/new")
    @PreAuthorize("hasRole('SUPER') or hasRole('ADMIN')")
    public ResponseEntity<?> createBook(@RequestBody BookDetailWithoutUserListDto book) throws IOException {
        return bookService.createBook(book);
    }

    @PutMapping("/{type}/{identifier}")
    public ResponseEntity<?> updateBook(@PathVariable String type, @PathVariable String identifier,
                                        @RequestBody Book bookDetails) throws IOException {

        if (bookDetails == null) {
            return ResponseEntity.badRequest().body("The 'book' part is required.");
        }

        return bookService.updateBook(identifier, type, bookDetails);
    }

    @DeleteMapping("/{type}/{identifier}")
    public ResponseEntity<Void> deleteBook(@PathVariable String type, @PathVariable String identifier) {

        return bookService.deleteBook(identifier, type);
    }

    @PutMapping("/addtouser/{type}/{identifier}")
    public ResponseEntity<?> addBookToCurrentUser(@PathVariable String type, @PathVariable String identifier) {

        return bookService.addBookToCurrentUser(identifier, type);
    }

    @DeleteMapping("/delfromuser/{type}/{identifier}")
    public ResponseEntity<?> deleteBookFromCurrentUser(@PathVariable String type, @PathVariable String identifier) {

        return bookService.deleteBookFromCurrentUser(identifier, type);
    }
}
