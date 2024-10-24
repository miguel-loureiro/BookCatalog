package com.bookcatalog.backend.controller;

import com.bookcatalog.backend.model.Role;
import com.bookcatalog.backend.model.User;
import com.bookcatalog.backend.model.dto.UserDto;
import com.bookcatalog.backend.service.UserService;
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

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class UserControllerTest {

    @Mock
    private UserService userService;

    @InjectMocks
    private UserController userController;
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
    void testAllUsers_Success() {
        // Arrange
        UserDto user1 = new UserDto("user1", "user1@example.com", Role.READER);
        UserDto user2 = new UserDto("user2", "user2@example.com", Role.ADMIN);
        List<UserDto> userList = Arrays.asList(user1, user2);
        Page<UserDto> userPage = new PageImpl<>(userList, PageRequest.of(0, 10), userList.size());

        when(userService.getAllUsers(anyInt(), anyInt())).thenReturn(ResponseEntity.ok(userPage));

        // Act
        ResponseEntity<Page<UserDto>> response = userController.allUsers();

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(userList.size(), Objects.requireNonNull(response.getBody()).getTotalElements());
        verify(userService, times(1)).getAllUsers(0, 10);
    }

    @Test
    void testGetUser_Success() {
        // Arrange
        String identifier = "user1";
        String type = "username";
        User user = new User("user1", "user1@example.com", "password", Role.READER);
        UserDto userDto = new UserDto(user);

        when(userService.getUserByIdentifier(eq(identifier), eq(type))).thenReturn(Optional.of(user));

        // Act
        ResponseEntity<UserDto> response = userController.getUser(type, identifier);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(userDto, response.getBody());
        verify(userService, times(1)).getUserByIdentifier(identifier, type);
    }

    @Test
    void testGetUser_NotFound() {
        // Arrange
        String identifier = "nonexistentUser";
        String type = "username";

        when(userService.getUserByIdentifier(eq(identifier), eq(type))).thenReturn(Optional.empty());

        // Act
        ResponseEntity<UserDto> response = userController.getUser(type, identifier);

        // Assert
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        verify(userService, times(1)).getUserByIdentifier(identifier, type);
    }


    @Test
    void testGetUser_IllegalArgumentException() {
        // Arrange
        String type = "invalidType";
        String identifier = "user1";

        when(userService.getUserByIdentifier(eq(identifier), eq(type))).thenThrow(new IllegalArgumentException("Invalid type"));

        // Act
        ResponseEntity<UserDto> response = userController.getUser(type, identifier);

        // Assert
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        verify(userService, times(1)).getUserByIdentifier(identifier, type);
    }

    @Test
    void testGetUser_GenericException() {
        // Arrange
        String type = "username";
        String identifier = "user1";

        when(userService.getUserByIdentifier(eq(identifier), eq(type))).thenThrow(new RuntimeException("Unexpected error"));

        // Act
        ResponseEntity<UserDto> response = userController.getUser(type, identifier);

        // Assert
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        verify(userService, times(1)).getUserByIdentifier(identifier, type);
    }

    @Test
    void testDeleteUser_Success() throws IOException {
        // Arrange
        String identifier = "user1";
        String type = "username";

        when(userService.deleteUser(eq(identifier), eq(type))).thenReturn(ResponseEntity.ok().build());

        // Act
        ResponseEntity<Void> response = userController.deleteUser(type, identifier);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(userService, times(1)).deleteUser(identifier, type);
    }

    @Test
    void testDeleteUser_BadRequest() throws IOException {
        // Arrange
        String identifier = "user1";
        String type = "invalidType";

        when(userService.deleteUser(eq(identifier), eq(type))).thenReturn(ResponseEntity.badRequest().build());

        // Act
        ResponseEntity<Void> response = userController.deleteUser(type, identifier);

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        verify(userService, times(1)).deleteUser(identifier, type);
    }

    @Test
    void testUpdateUser_Success() throws IOException {
        // Arrange
        String identifier = "user1";
        String type = "username";
        UserDto input = new UserDto("user1", "user1@example.com", Role.READER);

        when(userService.updateUser(eq(identifier), eq(type), eq(input))).thenReturn(ResponseEntity.ok().build());

        // Act
        ResponseEntity<UserDto> response = userController.updateUser(type, identifier, input);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(userService, times(1)).updateUser(identifier, type, input);
    }

    @Test
    void testUpdateUser_BadRequest() throws IOException {
        // Arrange
        String identifier = "user1";
        String type = "invalidType";
        UserDto input = new UserDto("user1", "user1@example.com", Role.READER);

        when(userService.updateUser(eq(identifier), eq(type), eq(input))).thenReturn(ResponseEntity.badRequest().build());

        // Act
        ResponseEntity<UserDto> response = userController.updateUser(type, identifier, input);

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        verify(userService, times(1)).updateUser(identifier, type, input);
    }
}