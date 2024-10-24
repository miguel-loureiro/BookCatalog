package com.bookcatalog.backend.controller;

import com.bookcatalog.backend.model.Book;
import com.bookcatalog.backend.model.Role;
import com.bookcatalog.backend.model.User;
import com.bookcatalog.backend.model.dto.BookDetailWithoutUserListDto;
import com.bookcatalog.backend.repository.UserRepository;
import com.bookcatalog.backend.service.AuthenticationService;
import com.bookcatalog.backend.service.BookService;
import com.bookcatalog.backend.service.JwtService;
import com.bookcatalog.backend.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class GuestControllerTest {

    @Mock
    private BookService bookService;

    @Mock
    private UserService userService;

    @Mock
    private JwtService jwtService;

    @Mock
    private AuthenticationService authenticationService;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private GuestController guestController;

    private SecurityContext securityContext;
    private Authentication authentication;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);

        securityContext = mock(SecurityContext.class);
        SecurityContextHolder.setContext(securityContext);

        authentication = mock(Authentication.class);
        when(securityContext.getAuthentication()).thenReturn(authentication);
    }
/*
    @Test
    public void testAuthenticateGuest_Success() {
        // Arrange
        User guestUser = new User();
        guestUser.setUsername("guestuser");
        guestUser.setRole(Role.GUEST);

        String token = "jwt-token";
        long expirationTime = 3600000L; // 1 hour

        CustomUserDetails customUserDetails = new CustomUserDetails(guestUser);

        when(jwtService.generateToken(customUserDetails)).thenReturn(token);
        when(jwtService.getExpirationTime()).thenReturn(expirationTime);

        // Act
        ResponseEntity<LoginResponse> response = guestController.authenticateGuest();

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(token, Objects.requireNonNull(response.getBody()).getToken());
        assertEquals(expirationTime, response.getBody().getExpiresIn());

        // Verify that the security context was cleared and set with a new guest user
        verify(securityContext, times(1)).setAuthentication((Authentication) any(UsernamePasswordAuthenticationToken.class));
    }

 */

    @Test
    public void testGetAvailableBooks_Success_ForGuestUser() {
        // Arrange
        User guestUser = new User();
        guestUser.setUsername("guestuser");
        guestUser.setRole(Role.GUEST);

        Pageable pageable = PageRequest.of(0, 10);
        Set<BookDetailWithoutUserListDto> books = Set.of(new BookDetailWithoutUserListDto(new Book("Title", "Author")));

        when(userService.getCurrentUser()).thenReturn(Optional.of(guestUser));
        when(bookService.getOnlyBooks(0, 10)).thenReturn(ResponseEntity.ok(books));

        // Act
        ResponseEntity<?> response = guestController.getAvailableBooks(0, 10);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(books, response.getBody());
    }

    @Test
    public void testGetAvailableBooks_Unauthorized_NoGuestUser() {
        // Arrange
        when(userService.getCurrentUser()).thenReturn(Optional.empty());

        // Act
        ResponseEntity<?> response = guestController.getAvailableBooks(0, 10);

        // Assert
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertEquals("Restricted to logged Guest users", response.getBody());
    }

    @Test
    public void testGetAvailableBooks_Forbidden_NotGuestUser() {
        // Arrange
        User adminUser = new User();
        adminUser.setUsername("admin");
        adminUser.setRole(Role.ADMIN);

        when(userService.getCurrentUser()).thenReturn(Optional.of(adminUser));

        // Act
        ResponseEntity<?> response = guestController.getAvailableBooks(0, 10);

        // Assert
        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        assertEquals("Access restricted to Guest users only", response.getBody());
    }
}