package com.bookcatalog.bookcatalog.controller;

import com.bookcatalog.bookcatalog.exceptions.UserNotFoundException;
import com.bookcatalog.bookcatalog.model.Book;
import com.bookcatalog.bookcatalog.model.User;
import com.bookcatalog.bookcatalog.model.dto.UserDto;
import com.bookcatalog.bookcatalog.service.AuthenticationService;
import com.bookcatalog.bookcatalog.service.BookService;
import com.bookcatalog.bookcatalog.service.UserService;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;

@RequestMapping("/guest")
@RestController
public class GuestController {

    private final BookService bookService;

    public GuestController(BookService bookService) {

        this.bookService = bookService;
    }

    @GetMapping("/books")
    public ResponseEntity<Page<Book>> getAvailableBooks(
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "10") int size) {

        ResponseEntity<Page<Book>> books = null;
        try {

            books = bookService.getAllBooks(page, size);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return ResponseEntity.ok(books.getBody());
    }
}
