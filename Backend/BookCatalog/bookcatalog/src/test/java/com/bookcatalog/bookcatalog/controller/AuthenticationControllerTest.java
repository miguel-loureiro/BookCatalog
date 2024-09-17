package com.bookcatalog.bookcatalog.controller;

import com.bookcatalog.bookcatalog.exceptions.InvalidUserRoleException;
import com.bookcatalog.bookcatalog.model.CustomUserDetails;
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
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import static org.assertj.core.api.Assertions.assertThat;
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
    public void testAuthenticate_Success() {
        // Arrange
        LoginUserDto loginUserDto = new LoginUserDto();
        loginUserDto.setUsername("testuser");
        loginUserDto.setPassword("password");

        User authenticatedUser = new User();
        authenticatedUser.setUsername("testuser");
        authenticatedUser.setRole(Role.READER);

        CustomUserDetails customUserDetails = new CustomUserDetails(authenticatedUser);
        String token = "jwt-token";
        long expirationTime = 3600000L; // 1 hour

        when(authenticationService.authenticate(loginUserDto)).thenReturn(authenticatedUser);
        when(jwtService.generateToken(customUserDetails)).thenReturn(token);
        when(jwtService.getExpirationTime()).thenReturn(expirationTime);

        // Act
        ResponseEntity<LoginResponse> response = authenticationController.authenticate(loginUserDto);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(token, response.getBody().getToken());
        assertEquals(expirationTime, response.getBody().getExpiresIn());

        verify(securityContext, times(1)).setAuthentication(any(UsernamePasswordAuthenticationToken.class));
        verify(jwtService, times(1)).generateToken(customUserDetails);
        verify(authenticationService, times(1)).authenticate(loginUserDto);
    }

    @Test
    public void testAuthenticate_GuestUser_Success() {
        // Arrange
        LoginUserDto loginUserDto = new LoginUserDto();
        loginUserDto.setUsername("guestuser");
        loginUserDto.setPassword("guestpassword");

        User guestUser = new User();
        guestUser.setUsername("guestuser");
        guestUser.setRole(Role.GUEST);

        CustomUserDetails customUserDetails = new CustomUserDetails(guestUser);
        String token = "jwt-token";
        long expirationTime = 3600000L; // 1 hour

        when(authenticationService.authenticate(loginUserDto)).thenReturn(guestUser);
        when(jwtService.generateToken(customUserDetails)).thenReturn(token);
        when(jwtService.getExpirationTime()).thenReturn(expirationTime);

        // Act
        ResponseEntity<LoginResponse> response = authenticationController.authenticate(loginUserDto);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(token, response.getBody().getToken());
        assertEquals(expirationTime, response.getBody().getExpiresIn());

        verify(securityContext, times(1)).setAuthentication(any(UsernamePasswordAuthenticationToken.class));
        verify(jwtService, times(1)).generateToken(customUserDetails);
        verify(authenticationService, times(1)).authenticate(loginUserDto);
    }

    @Test
    public void testAuthenticate_BadRequest_NullLoginDto() {
        // Act
        ResponseEntity<LoginResponse> response = authenticationController.authenticate(null);

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNull(response.getBody());

        verify(jwtService, times(0)).generateToken(any(CustomUserDetails.class));
        verify(authenticationService, times(0)).authenticate(any(LoginUserDto.class));
    }

    @Test
    public void testAuthenticate_Unauthorized_InvalidCredentials() {
        // Arrange
        LoginUserDto loginUserDto = new LoginUserDto();
        loginUserDto.setUsername("wronguser");
        loginUserDto.setPassword("wrongpassword");

        when(authenticationService.authenticate(loginUserDto)).thenReturn(null);  // Authentication failed

        // Act
        ResponseEntity<LoginResponse> response = authenticationController.authenticate(loginUserDto);

        // Assert
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertNull(response.getBody());

        verify(jwtService, times(0)).generateToken(any(CustomUserDetails.class));
        verify(authenticationService, times(1)).authenticate(loginUserDto);
    }

    @Test
    public void testAuthenticate_Success_WithEmail() {
        // Arrange
        LoginUserDto loginUserDto = new LoginUserDto();
        loginUserDto.setEmail("testuser@example.com");
        loginUserDto.setPassword("password");

        User authenticatedUser = new User();
        authenticatedUser.setEmail("testuser@example.com");
        authenticatedUser.setRole(Role.READER);

        CustomUserDetails customUserDetails = new CustomUserDetails(authenticatedUser);
        String token = "jwt-token";
        long expirationTime = 3600000L; // 1 hour

        when(authenticationService.authenticate(loginUserDto)).thenReturn(authenticatedUser);
        when(jwtService.generateToken(customUserDetails)).thenReturn(token);
        when(jwtService.getExpirationTime()).thenReturn(expirationTime);

        // Act
        ResponseEntity<LoginResponse> response = authenticationController.authenticate(loginUserDto);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(token, response.getBody().getToken());
        assertEquals(expirationTime, response.getBody().getExpiresIn());

        verify(securityContext, times(1)).setAuthentication(any(UsernamePasswordAuthenticationToken.class));
        verify(jwtService, times(1)).generateToken(customUserDetails);
        verify(authenticationService, times(1)).authenticate(loginUserDto);
    }
}