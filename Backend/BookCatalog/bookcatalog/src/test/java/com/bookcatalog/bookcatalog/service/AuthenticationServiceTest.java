package com.bookcatalog.bookcatalog.service;

import com.bookcatalog.bookcatalog.model.Role;
import com.bookcatalog.bookcatalog.model.User;
import com.bookcatalog.bookcatalog.model.dto.RegisterUserDto;
import com.bookcatalog.bookcatalog.repository.UserRepository;
import org.hamcrest.Matcher;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.mockito.verification.VerificationMode;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;


import static org.hamcrest.Matchers.any;
import static org.hamcrest.Matchers.isA;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.internal.verification.VerificationModeFactory.times;

class AuthenticationServiceTest {

    @Mock
    private UserRepository mockUserRepository;

    @Mock
    private PasswordEncoder mockPasswwordEncoder;

    @Mock
    private AuthenticationManager mockAuthenticationManager;

    @InjectMocks
    private AuthenticationService authenticationService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }
/*
    @Test
    void signupWithValidRoleTest() {

        RegisterUserDto mockRegisterUserDto = new RegisterUserDto("username", "email@example.com", "password", Role.READER);
        User user = new User();
        user.setUsername("username");
        user.setEmail("email@example.com");
        user.setPassword("encodedPassword");
        user.setRole(Role.READER);

        when(mockPasswwordEncoder.encode(mockRegisterUserDto.getPassword())).thenReturn("encodedPassword");
        when(mockUserRepository.save(isA(User.class))).thenReturn(user);

        User result = authenticationService.signup(mockRegisterUserDto);

        assertNotNull(result);
        assertEquals("username", result.getUsername());
        assertEquals("email@example.com", result.getEmail());
        assertEquals("encodedPassword", result.getPassword());
        assertEquals(Role.READER, result.getRole());

        //verify(mockUserRepository, Mockito.times(1)).save(any(User.class)));
        verify(mockUserRepository, times(1)).save(User.class);
    }

    @Test
    void authenticate() {
    }

    @Test
    void authenticateGuest() {
    }

 */
}