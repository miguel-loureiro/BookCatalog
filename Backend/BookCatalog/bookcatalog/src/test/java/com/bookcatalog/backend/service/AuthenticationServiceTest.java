package com.newbookcatalog.newbookcatalog.service;

import com.newbookcatalog.newbookcatalog.exceptions.InvalidUserRoleException;
import com.newbookcatalog.newbookcatalog.model.Role;
import com.newbookcatalog.newbookcatalog.model.User;
import com.newbookcatalog.newbookcatalog.model.dto.LoginUserDto;
import com.newbookcatalog.newbookcatalog.model.dto.RegisterUserDto;
import com.newbookcatalog.newbookcatalog.repository.UserRepository;
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
    void testSignup_User_Reader_Success() {
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

        // Act and Assert
        InvalidUserRoleException exception = assertThrows(InvalidUserRoleException.class, () -> {
            authenticationService.signup(input);
        });
        assertEquals("Only READER role are allowed for signup.", exception.getMessage());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void testValidateInput_NullUsername_ThrowsIllegalArgumentException() {
        // Arrange
        RegisterUserDto input = new RegisterUserDto(null, "testEmail@example.com", "testPassword", Role.READER);

        // Act and Assert
        assertThrows(IllegalArgumentException.class, () -> authenticationService.signup(input));
    }

    @Test
    void testValidateInput_NullEmail_ThrowsIllegalArgumentException() {
        // Arrange
        RegisterUserDto input = new RegisterUserDto("testUser", null, "testPassword", Role.READER);

        // Act and Assert
        assertThrows(IllegalArgumentException.class, () -> authenticationService.signup(input));
    }

    @Test
    void testValidateInput_NullPassword_ThrowsIllegalArgumentException() {
        // Arrange
        RegisterUserDto input = new RegisterUserDto("testUser", "testEmail@example.com", null, Role.READER);

        // Act and Assert
        assertThrows(IllegalArgumentException.class, () -> authenticationService.signup(input));
    }

    @Test
    void testValidateInput_NullRole_ThrowsIllegalArgumentException() {
        // Arrange
        RegisterUserDto input = new RegisterUserDto("testUser", "testEmail@example.com", "testPassword", null);

        // Act and Assert
        assertThrows(IllegalArgumentException.class, () -> authenticationService.signup(input));
    }

    @Test
    void testValidateInput_NullUsernameAndEmail_ThrowsIllegalArgumentException() {
        // Arrange
        RegisterUserDto input = new RegisterUserDto(null, null, "testPassword", Role.READER);

        // Act and Assert
        assertThrows(IllegalArgumentException.class, () -> authenticationService.signup(input));
    }

    @Test
    void testValidateInput_NullUsernameAndPassword_ThrowsIllegalArgumentException() {
        // Arrange
        RegisterUserDto input = new RegisterUserDto(null, "testEmail@example.com", null, Role.READER);

        // Act and Assert
        assertThrows(IllegalArgumentException.class, () -> authenticationService.signup(input));
    }

    @Test
    void testValidateInput_NullAllFields_ThrowsIllegalArgumentException() {
        // Arrange
        RegisterUserDto input = new RegisterUserDto(null, null, null, null);

        // Act and Assert
        assertThrows(IllegalArgumentException.class, () -> authenticationService.signup(input));
    }

    @Test
    public void testSignup_InputIsNull_ShouldThrowIllegalArgumentException() {
        // Act and Assert
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            authenticationService.signup(null);
        });
        assertEquals("All fields are required for registration.", exception.getMessage());
    }

    @Test
    public void testSignup_RoleIsNull_ShouldThrowIllegalArgumentException() {
        // Arrange
        RegisterUserDto input = new RegisterUserDto("testuser", "test@example.com", "password", null);

        // Act and Assert
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            authenticationService.signup(input);
        });
        assertEquals("All fields are required for registration.", exception.getMessage());
    }

    @Test
    public void testSignup_InvalidRole_ShouldThrowInvalidUserRoleException() {
        // Arrange
        RegisterUserDto input = new RegisterUserDto("testuser", "test@example.com", "password", Role.ADMIN);

        // Act and Assert
        Exception exception = assertThrows(InvalidUserRoleException.class, () -> {
            authenticationService.signup(input);
        });
        assertEquals("Only READER role are allowed for signup.", exception.getMessage());
    }

    // 7. Test valid signup
    @Test
    public void testSignup_ValidInput_ShouldSaveUser() {
        // Arrange
        RegisterUserDto input = new RegisterUserDto("testuser", "test@example.com", "password", Role.READER);

        User savedUser = new User();
        savedUser.setUsername("testuser");
        savedUser.setEmail("test@example.com");
        savedUser.setRole(Role.READER);
        savedUser.setPassword("encodedPassword");

        when(passwordEncoder.encode(input.getPassword())).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(savedUser);

        // Act
        User result = authenticationService.signup(input);

        // Assert
        assertNotNull(result);
        assertEquals("testuser", result.getUsername());
        assertEquals("test@example.com", result.getEmail());
        assertEquals("encodedPassword", result.getPassword());
        assertEquals(Role.READER, result.getRole());
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    public void testSignup_InvalidUser_ShouldThrowIllegalStateException() {
        // Arrange
        RegisterUserDto input = new RegisterUserDto("testuser", "test@example.com", "password", Role.READER);
        when(passwordEncoder.encode(input.getPassword())).thenReturn("encodedPassword");

        // Act and Assert
        doThrow(new IllegalStateException("User fields are not properly set.")).when(userRepository).save(any(User.class));

        Exception exception = assertThrows(IllegalStateException.class, () -> {
            authenticationService.signup(input);
        });

        assertEquals("User fields are not properly set.", exception.getMessage());
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
    public void testAuthenticateWithValidEmail() {
        // Arrange
        LoginUserDto input = new LoginUserDto();
        input.setUsername("");
        input.setEmail("testuser@example.com");
        input.setPassword("password");

        User user = new User();
        user.setEmail("testuser@example.com");
        user.setPassword("password");
        user.setRole(Role.READER);

        when(userRepository.findByEmail(input.getEmail())).thenReturn(Optional.of(user));

        // Act
        User authenticatedUser = authenticationService.authenticate(input);

        // Assert
        assertNotNull(authenticatedUser);
        assertEquals("testuser@example.com", authenticatedUser.getEmail());
        verify(userRepository, never()).findByUsername(anyString());
        verify(userRepository, times(1)).findByEmail("testuser@example.com");
    }

    @Test
    public void testAuthenticateWithMissingIdentifier() {
        // Arrange
        LoginUserDto input = new LoginUserDto();
        input.setUsername(null);
        input.setEmail(null);
        input.setPassword("password");

        // Act and Assert
        Exception exception = assertThrows(UsernameNotFoundException.class, () -> {
            authenticationService.authenticate(input);
        });

        assertEquals("User not found with identifier: null", exception.getMessage());
        verify(userRepository, never()).findByUsername(anyString());
        verify(userRepository, never()).findByEmail(anyString());
    }

    @Test
    void testGetGuestUser_ShouldReturnGuestUserWithCorrectProperties() {
        // Act
        User guestUser = authenticationService.getGuestUser();

        // Assert
        assertNotNull(guestUser, "Guest user should not be null");
        assertEquals("guestuser", guestUser.getUsername(), "Guest user's username should be 'guestuser'");
        assertEquals(Role.GUEST, guestUser.getRole(), "Guest user's role should be GUEST");
    }

    @Test
    void testGetGuestUser_ShouldNotHaveOtherPropertiesSet() {
        // Act
        User guestUser = authenticationService.getGuestUser();

        // Assert
        assertNull(guestUser.getEmail(), "Guest user's email should be null");
        assertNull(guestUser.getPassword(), "Guest user's password should be null");
    }

    @Test
    void authenticate_GuestUser_ShouldBypassAuthentication() {
        // Arrange
        LoginUserDto loginUserDto = new LoginUserDto();
        loginUserDto.setUsername("guestUser");
        loginUserDto.setPassword("password");

        User guestUser = new User();
        guestUser.setUsername("guestUser");
        guestUser.setPassword("password");
        guestUser.setRole(Role.GUEST);

        when(userRepository.findByUsername("guestUser")).thenReturn(Optional.of(guestUser));

        // Act
        User result = authenticationService.authenticate(loginUserDto);

        // Assert
        assertNotNull(result);
        assertEquals(Role.GUEST, result.getRole());
        verify(authenticationManager, never()).authenticate(any(UsernamePasswordAuthenticationToken.class));
    }

    @Test
    void authenticate_NonExistentUser_ShouldThrowException() {
        // Arrange
        LoginUserDto loginUserDto = new LoginUserDto();
        loginUserDto.setUsername("nonExistentUser");
        loginUserDto.setPassword("password");

        when(userRepository.findByUsername("nonExistentUser")).thenReturn(Optional.empty());

        // Act
        assertThrows(UsernameNotFoundException.class, () -> authenticationService.authenticate(loginUserDto));

        // Assert
        verify(authenticationManager, never()).authenticate(any(UsernamePasswordAuthenticationToken.class));
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
    }
}