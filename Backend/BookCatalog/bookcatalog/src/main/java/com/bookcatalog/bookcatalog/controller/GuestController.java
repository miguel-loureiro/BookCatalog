package com.bookcatalog.bookcatalog.controller;

import com.bookcatalog.bookcatalog.exceptions.UserNotFoundException;
import com.bookcatalog.bookcatalog.model.*;
import com.bookcatalog.bookcatalog.model.dto.BookDto;
import com.bookcatalog.bookcatalog.model.dto.UserDto;
import com.bookcatalog.bookcatalog.repository.UserRepository;
import com.bookcatalog.bookcatalog.service.AuthenticationService;
import com.bookcatalog.bookcatalog.service.BookService;
import com.bookcatalog.bookcatalog.service.JwtService;
import com.bookcatalog.bookcatalog.service.UserService;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

@RequestMapping("/guest")
@RestController
public class GuestController {

    private final BookService bookService;
    private final UserService userService;
    private final JwtService jwtService;
    private final AuthenticationService authenticationService;
    private final UserRepository userRepository;

    public GuestController(BookService bookService, UserService userService, JwtService jwtService, AuthenticationService authenticationService, UserRepository userRepository) {

        this.bookService = bookService;
        this.userService = userService;
        this.jwtService = jwtService;
        this.authenticationService = authenticationService;
        this.userRepository = userRepository;
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> authenticateGuest() {

        SecurityContextHolder.clearContext();

        User guestUser = new User();
        guestUser.setUsername("guestuser");
        guestUser.setRole(Role.GUEST);

        String jwtToken = jwtService.generateToken(new CustomUserDetails(guestUser));
        long expirationTime = jwtService.getExpirationTime();

        UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                new CustomUserDetails(guestUser), null, List.of(new SimpleGrantedAuthority("ROLE_GUEST")));
        SecurityContextHolder.getContext().setAuthentication(authToken);

        LoginResponse response = new LoginResponse()
                .setToken(jwtToken)
                .setExpiresIn(expirationTime);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/books")
    public ResponseEntity<?> getAvailableBooks(
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "10") int size) {

        Optional<User> currentUserOpt = userService.getCurrentUser();

        if (currentUserOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Restricted to logged Guest users");
        }

        User currentUser = currentUserOpt.get();

        if (currentUser.getRole() != Role.GUEST) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Access restricted to Guest users only");
        }

        return bookService.getOnlyBooks(page, size);
    }
}
