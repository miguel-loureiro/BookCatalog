package com.bookcatalog.bookcatalog.controller;

import com.bookcatalog.bookcatalog.model.Role;
import com.bookcatalog.bookcatalog.model.User;
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

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

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
/*
    @Test
    public void testAllUsers_WithSuperRole_ReturnsUserList() {

        UserDto currentUser = new UserDto();
        currentUser.setRole(Role.SUPER);
        when(userService.getCurrentUser()).thenReturn(currentUser);

        List<UserDto> users = Arrays.asList(new UserDto(), new UserDto());
        when(userService.getUsersShortList()).thenReturn(users);

        ResponseEntity<List<UserDto>> response = userController.allUsers();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(users, response.getBody());
    }

    @Test
    public void testAllUsers_WithAdminRole_ReturnsUserList() {

        UserDto currentUser = new UserDto();
        currentUser.setRole(Role.ADMIN);
        when(userService.getCurrentUser()).thenReturn(currentUser);

        List<UserDto> users = Arrays.asList(new UserDto(), new UserDto());
        when(userService.getUsersShortList()).thenReturn(users);

        ResponseEntity<List<UserDto>> response = userController.allUsers();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(users, response.getBody());
    }

    @Test
    public void testAllUsers_WithoutAdminRole_ReturnsForbidden() {

        UserDto currentUser = new UserDto();
        currentUser.setRole(Role.READER);
        when(userService.getCurrentUser()).thenReturn(currentUser);

        ResponseEntity<List<UserDto>> response = userController.allUsers();

        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
    }

    @Test
    public void testAllUsers_UserNull_ReturnsForbidden() {

        when(userService.getCurrentUser()).thenReturn(null);

        ResponseEntity<List<UserDto>> response = userController.allUsers();

        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
    }
*/
    @Test
    public void testGetUserById_UserExists_ReturnsUser() {

        User user = new User();
        user.setUsername("testusername");
        when(userService.getUserByIdentifier(anyString(), anyString())).thenReturn(Optional.of(user));

        ResponseEntity<UserDto> response = userController.getUser("username", "testusername");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(user, response.getBody());
    }
/*
    @Test
    public void testGetUserById_UserDoesNotExist_ReturnsNotFound() {

        when(userService.getUserById(anyInt())).thenReturn(Optional.empty());

        ResponseEntity<UserDto> response = userController.getUserById(1);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    public void testGetUserByUsernameOrEmail_IdentifierIsNull_ReturnsBadRequest() {

        ResponseEntity<UserDto> response = userController.getUserByUsernameOrEmail(null);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    public void testGetUserByUsernameOrEmail_UserExists_ReturnsUser() {

        UserDto user = new UserDto();
        when(userService.getUserByUsernameOrEmail(anyString())).thenReturn(Optional.of(user));

        ResponseEntity<UserDto> response = userController.getUserByUsernameOrEmail("identifier");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(user, response.getBody());
    }

    @Test
    public void testGetUserByUsernameOrEmail_UserDoesNotExist_ReturnsNotFound() {

        when(userService.getUserByUsernameOrEmail(anyString())).thenReturn(Optional.empty());

        ResponseEntity<UserDto> response = userController.getUserByUsernameOrEmail("identifier");

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    public void testDeleteUserById_Success() {

        ResponseEntity<Void> expectedResponse = ResponseEntity.ok().build();
        when(userService.deleteUserById(anyInt())).thenReturn(expectedResponse);

        ResponseEntity<Void> response = userController.deleteUserById(1);

        assertEquals(expectedResponse, response);
    }

    @Test
    public void testDeleteUserByUsernameOrEmail_Success() {
        ResponseEntity<Void> expectedResponse = ResponseEntity.ok().build();
        when(userService.deleteUserByUsernameOrEmail(anyString())).thenReturn(expectedResponse);

        ResponseEntity<Void> response = userController.deleteUserByUsernameOrEmail("identifier");

        assertEquals(expectedResponse, response);
    }

    @Test
    public void testUpdateUserById_Success() {

        ResponseEntity<Object> expectedResponse = ResponseEntity.ok().build();
        when(userService.updateUserById(anyInt(), any(UserDto.class))).thenReturn(expectedResponse);

        UserDto UserDto = new UserDto();
        ResponseEntity<Object> response = userController.updateUserById(1, UserDto);

        assertEquals(expectedResponse, response);
    }

    @Test
    public void testUpdateUserByUsernameOrEmail_Success() {

        ResponseEntity<Object> expectedResponse = ResponseEntity.ok().build();
        when(userService.updateUserByUsernameOrEmail(anyString(), any(UserDto.class))).thenReturn(expectedResponse);

        UserDto UserDto = new UserDto();
        ResponseEntity<Object> response = userController.updateUserByUsernameOrEmail("identifier", UserDto);

        assertEquals(expectedResponse, response);
    }


 */
}