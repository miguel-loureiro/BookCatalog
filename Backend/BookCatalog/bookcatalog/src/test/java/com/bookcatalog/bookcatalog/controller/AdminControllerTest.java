package com.bookcatalog.bookcatalog.controller;

import com.bookcatalog.bookcatalog.model.Role;
import com.bookcatalog.bookcatalog.model.User;
import com.bookcatalog.bookcatalog.model.dto.RegisterUserDto;
import com.bookcatalog.bookcatalog.model.dto.UserDto;
import com.bookcatalog.bookcatalog.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class AdminControllerTest {

    @Mock
    private UserService userService;

    @InjectMocks
    private AdminController adminController;
    private SecurityContext securityContext;
    private Authentication authentication;
    private User currentUser;

    @BeforeEach
    void setUp() {

        MockitoAnnotations.openMocks(this);

        securityContext = mock(SecurityContext.class);
        SecurityContextHolder.setContext(securityContext);

        authentication = mock(Authentication.class);
        when(securityContext.getAuthentication()).thenReturn(authentication);

        currentUser = new User("currentUser", "currentUser@example.com", "password", Role.ADMIN);
        when(authentication.getPrincipal()).thenReturn(currentUser);

    }

    @Test
    void testCreateAdministrator_Success() throws IOException {

        // Arrange
        RegisterUserDto registerUserDto = new RegisterUserDto();
        UserDto createdAdmin = new UserDto();

        when(userService.createAdministrator(any(RegisterUserDto.class))).thenReturn(createdAdmin);

        // Act
        ResponseEntity<UserDto> response = adminController.createAdministrator(registerUserDto);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(createdAdmin, response.getBody());
    }

}