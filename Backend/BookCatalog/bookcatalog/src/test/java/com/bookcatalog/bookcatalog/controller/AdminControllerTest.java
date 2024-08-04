package com.bookcatalog.bookcatalog.controller;

import com.bookcatalog.bookcatalog.model.User;
import com.bookcatalog.bookcatalog.model.dto.RegisterUserDto;
import com.bookcatalog.bookcatalog.model.dto.UserShortDto;
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

        currentUser = new User();
        when(authentication.getPrincipal()).thenReturn(currentUser);

    }

    @Test
    void testCreateAdministrator_Success() {

        // Arrange
        RegisterUserDto registerUserDto = new RegisterUserDto();
        User createdAdmin = new User();

        when(userService.createAdministrator(any(RegisterUserDto.class))).thenReturn(createdAdmin);

        // Act
        ResponseEntity<User> response = adminController.createAdministrator(registerUserDto);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(createdAdmin, response.getBody());
    }

    @Test
    void testDeleteAdministratorById_Success() {

        // Arrange
        Integer id = 1;
        ResponseEntity<Void> expectedResponse = ResponseEntity.ok().build();
        when(userService.deleteAdministratorById(id)).thenReturn(expectedResponse);

        // Act
        ResponseEntity<?> response = adminController.deleteAdministratorById(id);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNull(response.getBody());
    }

    @Test
    void testDeleteAdministratorByUsernameOrEmail_Success() {

        // Arrange
        String identifier = "admin@example.com";
        ResponseEntity<Void> expectedResponse = ResponseEntity.ok().build();
        when(userService.deleteAdministratorByUsernameOrEmail(identifier)).thenReturn(expectedResponse);

        // Act
        ResponseEntity<Void> response = adminController.deleteAdministratorByUsernameOrEmail(identifier);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNull(response.getBody());
    }

    @Test
    void testUpdateAdministratorById_Success() {

        // Arrange
        Integer id = 1;
        UserShortDto input = new UserShortDto();
        ResponseEntity<Object> expectedResponse = ResponseEntity.ok().build();
        when(userService.updateAdministratorById(id, input)).thenReturn(expectedResponse);

        // Act
        ResponseEntity<Object> response = adminController.updateAdministratorById(id, input);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    void testUpdateAdministratorByUsernameOrEmail_Success() {

        // Arrange
        String identifier = "admin@example.com";
        UserShortDto input = new UserShortDto();
        ResponseEntity<Object> expectedResponse = ResponseEntity.ok().build();
        when(userService.updateAdministratorByUsernameOrEmail(identifier, input)).thenReturn(expectedResponse);

        // Act
        ResponseEntity<Object> response = adminController.updateAdministratorByUsernameOrEmail(identifier, input);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }
}