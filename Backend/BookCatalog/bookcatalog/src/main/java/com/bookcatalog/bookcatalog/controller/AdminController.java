package com.bookcatalog.bookcatalog.controller;

import com.bookcatalog.bookcatalog.exceptions.InvalidUserRoleException;
import com.bookcatalog.bookcatalog.model.Book;
import com.bookcatalog.bookcatalog.model.Role;
import com.bookcatalog.bookcatalog.model.User;
import com.bookcatalog.bookcatalog.model.dto.RegisterUserDto;
import com.bookcatalog.bookcatalog.service.BookService;
import com.bookcatalog.bookcatalog.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RequestMapping("/admins")
@RestController
public class AdminController {

    private final UserService userService;
    private final BookService bookService;

    public AdminController(UserService userService, BookService bookService) {
        this.userService = userService;
        this.bookService = bookService;
    }

    @PostMapping
    @PreAuthorize("hasRole('SUPER')")
    public ResponseEntity<User> createAdministrator(@RequestBody RegisterUserDto registerUserDto) {
        User createdAdmin = userService.createAdministrator(registerUserDto);

        return ResponseEntity.ok(createdAdmin);
    }

    @PostMapping("/newusers")
    @PreAuthorize("hasRole('SUPER') or hasRole('ADMIN')")
    public ResponseEntity<User> createUser(@RequestBody RegisterUserDto registerUserDto) {

        if(registerUserDto.getRole() != Role.READER && registerUserDto.getRole() != Role.GUEST) {
            throw new InvalidUserRoleException("Only READER or GUEST users can be created using this endpoint");
        }

        User createdUser = userService.createUser(registerUserDto);

        return ResponseEntity.ok(createdUser);
    }

    @GetMapping("/books/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPER')")
    public ResponseEntity<List<Book>> getUserBooks(@PathVariable Integer id) {
        Optional<User> user = userService.getUserById(id);
        if (user.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        List<Book> books = bookService.getBooksByUserId(id);
        return ResponseEntity.ok(books);
    }
}
