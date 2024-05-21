package com.bookcatalog.bookcatalog.controller;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.bookcatalog.bookcatalog.model.Book;
import com.bookcatalog.bookcatalog.service.BookService;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;


@RestController
public class BookController {

    private static final String UPLOAD_DIR = System.getProperty("user.dir") + "/uploads"; //uploads folder
    private static final long MAX_FILE_SIZE = 4 * 1024 * 1024; // 4MB

    @Autowired
    private BookService bookService;

    @PostMapping(consumes = {"multipart/form-data"})
    public ResponseEntity<?> createBook(@RequestPart("book") Book book, @RequestPart("file") MultipartFile file) throws IOException {
        if (file != null && !file.isEmpty()) {
            if (file.getSize() > MAX_FILE_SIZE) {
                return ResponseEntity.badRequest().body("File size exceeds 4MB size limit");
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

    /**
     * @param id
     * @return
     */
    @GetMapping("/{id}")
    public ResponseEntity<Book> getBookById(@PathVariable Integer id) {
        return bookService.getBookById(id)
        .map(ResponseEntity::ok)
        .orElseGet(() -> ResponseEntity.notFound().build());
    }

    /** Get all the books. localhost:8080/books
     * @return
     */
    @GetMapping("/books")
    public List<Book> getAllBooks() {
        return bookService.getAllBooks();
    }

    /**
     * @param id
     * @param updatedBookInfo
     * @return
     */
    @PutMapping(value = "/{id}", consumes = {"multipart/form-data"})
    public ResponseEntity<?> updateBook(
            @PathVariable Integer id,
            @RequestPart("book") Book bookDetails,
            @RequestPart(value = "file", required = false) MultipartFile file) throws IOException {

        if (file != null && !file.isEmpty()) {
            if (file.getSize() > MAX_FILE_SIZE) {
                return ResponseEntity.badRequest().body("File size exceeds the 4MB limit.");
            }

            String filename = System.currentTimeMillis() + "_" + file.getOriginalFilename();
            Path filepath = Paths.get(UPLOAD_DIR, filename);
            Files.createDirectories(filepath.getParent());
            Files.write(filepath, file.getBytes());

            bookDetails.setCoverImage(filename);
        }

        final Book updatedBook = bookService.updateBook(id, bookDetails);
        return ResponseEntity.ok(updatedBook);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Book> deleteBookById(@PathVariable Integer id) {
        bookService.deleteBook(id);
        return ResponseEntity.noContent().build();
    }
}
