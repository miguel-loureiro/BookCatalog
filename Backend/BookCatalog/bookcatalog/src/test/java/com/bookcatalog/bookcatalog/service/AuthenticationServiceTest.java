package com.bookcatalog.bookcatalog.service;

import com.bookcatalog.bookcatalog.exceptions.InvalidUserRoleException;
import com.bookcatalog.bookcatalog.model.Role;
import com.bookcatalog.bookcatalog.model.User;
import com.bookcatalog.bookcatalog.model.dto.LoginUserDto;
import com.bookcatalog.bookcatalog.model.dto.RegisterUserDto;
import com.bookcatalog.bookcatalog.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;


import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AuthenticationServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private RegisterUserDto registerUserDto;

    @Mock
    private AuthenticationManager authenticationManager;

    @InjectMocks
    private AuthenticationService authenticationService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testSignup_Success() {
        // Arrange
        registerUserDto = new RegisterUserDto("username", "email@example.com", "password", Role.READER);
        User user = new User();
        user.setUsername("username");
        user.setEmail("email@example.com");
        user.setPassword("encodedPassword");
        user.setRole(Role.READER);

        when(passwordEncoder.encode("password")).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(user);

        // Act
        User result = authenticationService.signup(registerUserDto);

        // Assert
        assertNotNull(result);
        assertEquals("username", result.getUsername());
        assertEquals("email@example.com", result.getEmail());
        assertEquals(Role.READER, result.getRole());
        verify(userRepository).save(any(User.class));
    }

    @Test
    void testSignup_InvalidRole() {
        // Arrange
        RegisterUserDto input = new RegisterUserDto("username", "email@example.com", "password", Role.ADMIN);

        // Act & Assert
        InvalidUserRoleException exception = assertThrows(InvalidUserRoleException.class, () -> {
            authenticationService.signup(input);
        });
        assertEquals("Only READER or GUEST roles are allowed for signup.", exception.getMessage());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void testSignupWithValidInputValuesAndUserHasNullValues() {
        // Arrange
        RegisterUserDto input = new RegisterUserDto("username", "email@example.com", "password", Role.READER);
        User user = new User();

        user.setEmail(null);

        // Act and Assert
        assertThrows(IllegalStateException.class, () -> {
            authenticationService.signup(input);
        });

        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void testValidateInput_NullUsername_ThrowsIllegalArgumentException() {
        // Arrange
        RegisterUserDto input = new RegisterUserDto(null, "testEmail@example.com", "testPassword", Role.READER);

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> authenticationService.signup(input));
    }

    @Test
    void testValidateInput_NullEmail_ThrowsIllegalArgumentException() {
        // Arrange
        RegisterUserDto input = new RegisterUserDto("testUser", null, "testPassword", Role.READER);

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> authenticationService.signup(input));
    }

    @Test
    void testValidateInput_NullPassword_ThrowsIllegalArgumentException() {
        // Arrange
        RegisterUserDto input = new RegisterUserDto("testUser", "testEmail@example.com", null, Role.READER);

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> authenticationService.signup(input));
    }

    @Test
    void testValidateInput_NullRole_ThrowsIllegalArgumentException() {
        // Arrange
        RegisterUserDto input = new RegisterUserDto("testUser", "testEmail@example.com", "testPassword", null);

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> authenticationService.signup(input));
    }

    @Test
    void testValidateInput_NullUsernameAndEmail_ThrowsIllegalArgumentException() {
        // Arrange
        RegisterUserDto input = new RegisterUserDto(null, null, "testPassword", Role.READER);

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> authenticationService.signup(input));
    }

    @Test
    void testValidateInput_NullUsernameAndPassword_ThrowsIllegalArgumentException() {
        // Arrange
        RegisterUserDto input = new RegisterUserDto(null, "testEmail@example.com", null, Role.READER);

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> authenticationService.signup(input));
    }

    @Test
    void testValidateInput_NullAllFields_ThrowsIllegalArgumentException() {
        // Arrange
        RegisterUserDto input = new RegisterUserDto(null, null, null, null);

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> authenticationService.signup(input));
    }

    @Test
    void testAuthenticate_Success_Username() {
        // Arrange
        LoginUserDto input = new LoginUserDto("username", "email@email.com","password");
        User user = new User();
        user.setUsername("username");
        user.setPassword("encodedPassword");
        user.setRole(Role.READER);

        when(passwordEncoder.matches("password", "encodedPassword")).thenReturn(true);
        when(userRepository.findByUsername("username")).thenReturn(Optional.of(user));
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(mock(Authentication.class));

        // Act
        User result = authenticationService.authenticate(input);

        // Assert
        assertNotNull(result);
        assertEquals("username", result.getUsername());
        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(userRepository).findByUsername("username");
    }

    @Test
    void testAuthenticate_Success_Email() {
        // Arrange
        LoginUserDto input = new LoginUserDto(null, "email@example.com", "password");
        User user = new User();
        user.setEmail("email@example.com");
        user.setPassword("encodedPassword");
        user.setRole(Role.READER);

        when(passwordEncoder.matches("password", "encodedPassword")).thenReturn(true);
        when(userRepository.findByEmail("email@example.com")).thenReturn(Optional.of(user));
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(mock(Authentication.class));

        // Act
        User result = authenticationService.authenticate(input);

        // Assert
        assertNotNull(result);
        assertEquals("email@example.com", result.getEmail());
        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(userRepository).findByEmail("email@example.com");
    }

    @Test
    void testAuthenticate_Failure_InvalidRole() {
        // Arrange
        LoginUserDto input = new LoginUserDto("username", "email@email.com", "password");
        User user = new User();
        user.setUsername("username");
        user.setPassword("encodedPassword");
        user.setRole(Role.GUEST);

        when(passwordEncoder.matches("password", "encodedPassword")).thenReturn(true);
        when(userRepository.findByUsername("username")).thenReturn(Optional.of(user));
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(mock(Authentication.class));

        // Act and Assert
        InvalidUserRoleException exception = assertThrows(InvalidUserRoleException.class, () -> {
            authenticationService.authenticate(input);
        });
        assertEquals("Cannot log in with role GUEST via this endpoint", exception.getMessage());
    }

    @Test
    void testAuthenticate_Failure_InvalidCredentials() {
        // Arrange
        LoginUserDto input = new LoginUserDto("username", "useremail@email.com", "wrongPassword");
        String identifier = "username";
        UsernamePasswordAuthenticationToken authenticationToken =
                new UsernamePasswordAuthenticationToken(identifier, input.getPassword());

        when(authenticationManager.authenticate(authenticationToken))
                .thenThrow(new UsernameNotFoundException("User not found with identifier: " + identifier));

        // Act and Assert
        assertThrows(UsernameNotFoundException.class, () -> {
            authenticationService.authenticate(input);
        });
        verify(userRepository, never()).findByUsername(anyString());
        verify(userRepository, never()).findByEmail(anyString());
    }

    @Test
    void testAuthenticateGuest_Success() {
        // Arrange
        LoginUserDto input = new LoginUserDto("guestUsername", "guestemail@email.com", "guestPassword");
        User guestUser = new User();
        guestUser.setUsername("guestUsername");
        guestUser.setPassword("encodedGuestPassword");
        guestUser.setRole(Role.GUEST);

        when(passwordEncoder.matches("guestPassword", "encodedGuestPassword")).thenReturn(true);
        when(userRepository.findByUsername("guestUsername")).thenReturn(Optional.of(guestUser));

        // Act
        User result = authenticationService.authenticateGuest(input);

        // Assert
        assertNotNull(result);
        assertEquals("guestUsername", result.getUsername());
        verify(userRepository).findByUsername("guestUsername");
    }

    @Test
    void testAuthenticateGuest_Failure_InvalidCredentials() {
        // Arrange
        LoginUserDto input = new LoginUserDto("guestUsername","guestemail@email.com",  "guestPassword");
        when(userRepository.findByUsername("guestUsername")).thenReturn(Optional.empty());

        // Act and Assert
        UsernameNotFoundException exception = assertThrows(UsernameNotFoundException.class, () -> {
            authenticationService.authenticateGuest(input);
        });
        assertEquals("Guest user not found or invalid credentials", exception.getMessage());
    }
}