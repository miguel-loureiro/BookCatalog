package com.bookcatalog.bookcatalog.controller;

import com.bookcatalog.bookcatalog.exceptions.UserNotFoundException;
import com.bookcatalog.bookcatalog.model.Role;
import com.bookcatalog.bookcatalog.model.User;
import com.bookcatalog.bookcatalog.model.dto.RegisterUserDto;
import com.bookcatalog.bookcatalog.model.dto.UserDto;
import com.bookcatalog.bookcatalog.repository.UserRepository;
import com.bookcatalog.bookcatalog.service.UserService;
import com.bookcatalog.bookcatalog.service.strategy.delete.DeleteStrategy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.client.HttpClientErrorException;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class AdminControllerTest {

    @Mock
    private UserService userService;

    @Mock
    private DeleteStrategy<User> deleteStrategy;

    @Mock
    private Map<String, DeleteStrategy<User>> deleteStrategiesMap;

    @Mock
    UserRepository userRepository;

    @Mock
    User adminToDelete;

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

    private void mockCurrentUser(User user) {

        if (user == null) {

            SecurityContextHolder.clearContext();
            when(securityContext.getAuthentication()).thenReturn(null);

        } else {

            UserDetails userDetails = mock(UserDetails.class);
            when(userDetails.getUsername()).thenReturn(user.getUsername());

            SecurityContextHolder.setContext(securityContext);

            when(authentication.getPrincipal()).thenReturn(userDetails);
            when(securityContext.getAuthentication()).thenReturn(authentication);

            when(userRepository.findByUsername(user.getUsername())).thenReturn(Optional.of(user));
        }
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

    @Test
    void testDeleteAdministrator_Success() throws IOException {
        // Arrange
        ResponseEntity<Void> expectedResponse = ResponseEntity.ok().build();
        when(userService.deleteAdministrator("newadmin" , "username")).thenReturn(expectedResponse);

        // Act
        ResponseEntity<Void> response = adminController.deleteAdministrator("username", "newadmin");

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(userService, times(1)).deleteAdministrator("newadmin",  "username");
    }

    @Test
    void deleteAdministrator_BadRequest() throws IOException {
        // Arrange
        when(userService.deleteAdministrator("admin@example.com", "invalidType")).thenAnswer(invocation -> {

            return ResponseEntity.badRequest().build();
        });

        // Act
        ResponseEntity<Void> response = adminController.deleteAdministrator("invalidType", "admin@example.com");

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        verify(userService, times(1)).deleteAdministrator("admin@example.com", "invalidType");
    }

    @Test
    void testDeleteAdministrator_Forbidden() throws IOException {
        // Arrange
        String type = "email";
        String identifier = "admin@example.com";

        when(userService.deleteAdministrator(identifier, type)).thenAnswer(invocation -> {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        });

        // Act
        ResponseEntity<Void> response = adminController.deleteAdministrator("email", "admin@example.com");

        // Assert
        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        verify(userService, times(1)).deleteAdministrator(identifier, type);
    }

    @Test
    void testDeleteAdministrator_InternalServerError() throws IOException {
        when(userService.deleteAdministrator("admin@example.com", "email")).thenAnswer(invocation -> {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        });

        // Act
        ResponseEntity<Void> response = adminController.deleteAdministrator("email", "admin@example.com");

        // Assert
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        verify(userService, times(1)).deleteAdministrator("admin@example.com", "email");
    }

    @Test
    void getUser_Success() {
        // Arrange
        String type = "email";
        String identifier = "admin@example.com";
        User user = new User();
        UserDto userDto = new UserDto(user);

        when(userService.getUserByIdentifier(identifier, type)).thenReturn(user);

        // Act
        ResponseEntity<UserDto> response = adminController.getUser(type, identifier);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(userDto, response.getBody());
        verify(userService, times(1)).getUserByIdentifier(identifier, type);
    }

    @Test
    void getUser_UserNotFound() {
        // Arrange
        String type = "email";
        String identifier = "nonexistent@example.com";

        when(userService.getUserByIdentifier(identifier, type)).thenThrow(new UserNotFoundException("User not found", null));

        // Act
        ResponseEntity<UserDto> response = adminController.getUser(type, identifier);

        // Assert
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals(null, response.getBody());
        verify(userService, times(1)).getUserByIdentifier(identifier, type);
    }

    @Test
    void getUser_InvalidIdentifierType() {
        // Arrange
        String type = "invalidType";
        String identifier = "someIdentifier";

        when(userService.getUserByIdentifier(identifier, type)).thenThrow(new IllegalArgumentException("Invalid identifier type"));

        // Act
        ResponseEntity<UserDto> response = adminController.getUser(type, identifier);

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals(null, response.getBody());
        verify(userService, times(1)).getUserByIdentifier(identifier, type);
    }

    @Test
    void allUsers_Success() {
        // Arrange
        UserDto user1 = new UserDto("user1", "user1@example.com", Role.ADMIN);
        UserDto user2 = new UserDto("user2", "user2@example.com", Role.READER);
        List<UserDto> userList = Arrays.asList(user1, user2);
        Page<UserDto> userPage = new PageImpl<>(userList, PageRequest.of(0, 10), userList.size());

        when(userService.getAllUsers(0, 10)).thenReturn(ResponseEntity.ok(userPage));

        // Act
        ResponseEntity<Page<UserDto>> response = adminController.allUsers();

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(userPage, response.getBody());
        verify(userService, times(1)).getAllUsers(0, 10);
    }

    @Test
    void allUsers_NoUsersFound() {
        // Arrange
        Page<UserDto> emptyPage = Page.empty(PageRequest.of(0, 10));

        when(userService.getAllUsers(0, 10)).thenReturn(ResponseEntity.ok(emptyPage));

        // Act
        ResponseEntity<Page<UserDto>> response = adminController.allUsers();

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(emptyPage, response.getBody());
        verify(userService, times(1)).getAllUsers(0, 10);
    }

    @Test
    void allUsers_ServiceReturnsNull() {
        // Arrange
        when(userService.getAllUsers(0, 10)).thenReturn(ResponseEntity.ok(null));

        // Act
        ResponseEntity<Page<UserDto>> response = adminController.allUsers();

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNull( response.getBody());
        verify(userService, times(1)).getAllUsers(0, 10);
    }

    @Test
    void allUsers_ServiceThrowsException() {
        // Arrange
        when(userService.getAllUsers(0, 10)).thenThrow(new RuntimeException("Unexpected error"));

        // Act & Assert
        try {
            adminController.allUsers();
        } catch (Exception e) {
            assertEquals("Unexpected error", e.getMessage());
        }
        verify(userService, times(1)).getAllUsers(0, 10);
    }

    @Test
    void updateAdministrator_Success() throws IOException {
        // Arrange
        String type = "email";
        String identifier = "admin@example.com";
        UserDto userDto = new UserDto("admin", "admin@example.com", Role.ADMIN);

        when(userService.updateAdministrator(identifier, type, userDto)).thenReturn(ResponseEntity.ok().build());

        // Act
        ResponseEntity<Void> response = adminController.updateAdministrator(type, identifier, userDto);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(userService, times(1)).updateAdministrator(identifier, type, userDto);
    }

    @Test
    void updateAdministrator_InvalidType() throws IOException {
        // Arrange
        String type = "invalidType";
        String identifier = "admin@example.com";
        UserDto userDto = new UserDto("admin", "admin@example.com", Role.ADMIN);

        when(userService.updateAdministrator(identifier, type, userDto)).thenReturn(ResponseEntity.badRequest().build());

        // Act
        ResponseEntity<Void> response = adminController.updateAdministrator(type, identifier, userDto);

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        verify(userService, times(1)).updateAdministrator(identifier, type, userDto);
    }

    @Test
    void updateAdministrator_UserNotAdmin() throws IOException {
        // Arrange
        String type = "email";
        String identifier = "user@example.com";
        UserDto userDto = new UserDto("user", "user@example.com", Role.READER);

        when(userService.updateAdministrator(identifier, type, userDto)).thenReturn(ResponseEntity.status(HttpStatus.FORBIDDEN).build());

        // Act
        ResponseEntity<Void> response = adminController.updateAdministrator(type, identifier, userDto);

        // Assert
        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        verify(userService, times(1)).updateAdministrator(identifier, type, userDto);
    }

    @Test
    void updateAdministrator_UserNotFound() throws IOException {
        // Arrange
        String type = "email";
        String identifier = "nonexistent@example.com";
        UserDto userDto = new UserDto("nonexistent", "nonexistent@example.com", Role.ADMIN);

        when(userService.updateAdministrator(identifier, type, userDto)).thenReturn(ResponseEntity.notFound().build());

        // Act
        ResponseEntity<Void> response = adminController.updateAdministrator(type, identifier, userDto);

        // Assert
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        verify(userService, times(1)).updateAdministrator(identifier, type, userDto);
    }
/*
    @Test
    void updateAdministrator_IOException() throws IOException {
        // Arrange
        String type = "email";
        String identifier = "admin@example.com";
        UserDto userDto = new UserDto("admin", "admin@example.com", Role.ADMIN);

        when(userService.updateAdministrator(identifier, type, userDto)).thenThrow(new IOException("IO Error"));

        // Act
        ResponseEntity<Void> response = adminController.updateAdministrator(type, identifier, userDto);

        // Assert
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        verify(userService, times(1)).updateAdministrator(identifier, type, userDto);
    }*/
}