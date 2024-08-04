package com.bookcatalog.bookcatalog.controller;

import com.bookcatalog.bookcatalog.exceptions.InvalidUserRoleException;
import com.bookcatalog.bookcatalog.model.LoginResponse;
import com.bookcatalog.bookcatalog.model.Role;
import com.bookcatalog.bookcatalog.model.User;
import com.bookcatalog.bookcatalog.model.dto.LoginUserDto;
import com.bookcatalog.bookcatalog.model.dto.RegisterUserDto;
import com.bookcatalog.bookcatalog.service.AuthenticationService;
import com.bookcatalog.bookcatalog.service.JwtService;
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
import static org.mockito.Mockito.*;

class AuthenticationControllerTest {

    @Mock
    private JwtService jwtService;

    @Mock
    private AuthenticationService authenticationService;

    @InjectMocks
    private AuthenticationController authenticationController;
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
    void testRegisterUser_UserRoleReaderSuccess() {

        // Arrange
        RegisterUserDto registerUserDto = new RegisterUserDto();
        registerUserDto.setRole(Role.READER);
        User user = new User();
        when(authenticationService.signup(any(RegisterUserDto.class))).thenReturn(user);

        // Act
        ResponseEntity<User> response = authenticationController.registerUser(registerUserDto);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(user, response.getBody());

    }

    @Test
    void testRegisterUser_UserRoleAdmin_Failure() {

        // Arrange
        RegisterUserDto registerUserDto = new RegisterUserDto();
        registerUserDto.setRole(Role.ADMIN);

        // Act
        InvalidUserRoleException exception = assertThrows(InvalidUserRoleException.class, () -> {
            authenticationController.registerUser(registerUserDto);
        });

        // Assert
        assertEquals("Cannot sign up with role SUPER or ADMIN", exception.getMessage());
        verify(authenticationService, never()).signup(any(RegisterUserDto.class));

    }

    @Test
    void testRegisterUser_UserRoleSuper_Failure() {

        // Arrange
        RegisterUserDto registerUserDto = new RegisterUserDto();
        registerUserDto.setRole(Role.SUPER);

        // Act
        InvalidUserRoleException exception = assertThrows(InvalidUserRoleException.class, () -> {
            authenticationController.registerUser(registerUserDto);
        });

        // Assert
        assertEquals("Cannot sign up with role SUPER or ADMIN", exception.getMessage());
        verify(authenticationService, never()).signup(any(RegisterUserDto.class));

    }

    @Test
    void testGuestLogin_Success() {

        // Arrange
        LoginUserDto loginUserDto = new LoginUserDto();
        User guestUser = new User();
        guestUser.setRole(Role.GUEST);
        guestUser.setUsername("guestUser");
        when(authenticationService.authenticateGuest(any(LoginUserDto.class))).thenReturn(guestUser);

        // Act
        ResponseEntity<String> response = authenticationController.guestLogin(loginUserDto);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("The GUEST user: guestUser is logged in !", response.getBody());
    }

    @Test
    void testGuestLogin_UserNull_Failure() {

        // Arrange
        LoginUserDto loginUserDto = new LoginUserDto();
        User adminUser = new User();
        adminUser.setRole(Role.ADMIN);
        adminUser.setUsername("adminUser");
        when(authenticationService.authenticateGuest(any(LoginUserDto.class))).thenReturn(adminUser);

        // Act
        ResponseEntity<?> response = authenticationController.guestLogin(loginUserDto);

        // Assert
        assertEquals((HttpStatus.UNAUTHORIZED), response.getStatusCode());
    }

    @Test
    void testGuestLogin_UserNotGuest_Failure() {

        // Arrange
        when(authenticationService.authenticateGuest(any(LoginUserDto.class))).thenReturn(null);

        // Act
        ResponseEntity<?> response = authenticationController.guestLogin(null);

        // Assert
        assertEquals((HttpStatus.UNAUTHORIZED), response.getStatusCode());
    }

    @Test
    void testAuthenticate_Success() {

        // Arrange
        LoginUserDto loginUserDto = new LoginUserDto();
        User authenticatedUser = new User();
        String jwtToken = "jwt-token";
        long expirationTime = 3600L;

        when(authenticationService.authenticate(any(LoginUserDto.class))).thenReturn(authenticatedUser);
        when(jwtService.generateToken(any(User.class))).thenReturn(jwtToken);
        when(jwtService.getExpirationTime()).thenReturn(expirationTime);

        // Act
        ResponseEntity<LoginResponse> response = authenticationController.authenticate(loginUserDto);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(jwtToken, response.getBody().getToken());
        assertEquals(expirationTime, response.getBody().getExpiresIn());
    }
}