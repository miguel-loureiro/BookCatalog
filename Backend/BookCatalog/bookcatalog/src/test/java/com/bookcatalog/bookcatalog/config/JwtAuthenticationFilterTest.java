package com.bookcatalog.bookcatalog.config;

import com.bookcatalog.bookcatalog.service.CustomUserDetailsService;
import com.bookcatalog.bookcatalog.service.JwtService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;
import org.mockito.InjectMocks;
import org.mockito.*;
import org.mockito.MockitoAnnotations;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.servlet.HandlerExceptionResolver;

import java.io.IOException;
import java.net.http.HttpHeaders;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class JwtAuthenticationFilterTest {

    @InjectMocks
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Mock
    private HandlerExceptionResolver handlerExceptionResolver;

    @Mock
    private JwtService jwtService;

    @Mock
    private CustomUserDetailsService customUserDetailsService;

    @Mock
    private FilterChain filterChain;

    @Mock
    private UserDetails userDetails;

    private MockHttpServletRequest request;
    private MockHttpServletResponse response;


    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        request = new MockHttpServletRequest();
        response = new MockHttpServletResponse();
    }

    @AfterEach
    public void tearDown() {
        request.removeHeader("Authorization");
        SecurityContextHolder.clearContext();
    }

    @Test
    void doFilterInternal_NullRequest_ShouldThrowNullPointerException() {
        MockHttpServletResponse response = new MockHttpServletResponse();

        Executable executable = () -> jwtAuthenticationFilter.doFilterInternal(null, response, filterChain);
        assertThrows(NullPointerException.class, executable);
    }

    @Test
    void doFilterInternal_NullResponse_ShouldThrowNullPointerException() {
        MockHttpServletRequest request = new MockHttpServletRequest();

        Executable executable = () -> jwtAuthenticationFilter.doFilterInternal(request, null, filterChain);
        assertThrows(NullPointerException.class, executable);
    }

    @Test
    void doFilterInternal_NullFilterChain_ShouldThrowNullPointerException() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();

        Executable executable = () -> jwtAuthenticationFilter.doFilterInternal(request, response, null);
        assertThrows(NullPointerException.class, executable);
    }

    @Test
    void doFilterInternal_NullAuthHeader_shouldDoNothing() throws ServletException, IOException {
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);
        verify(filterChain, times(1)).doFilter(request, response);
        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    void doFilterInternal_InvalidAuthHeader_shouldDoNothing() throws ServletException, IOException {

        request.addHeader("Authorization", "InvalidBearerToken");

        // Execute the filter
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // Verify that the filter chain continues and no authentication is set
        verify(filterChain, times(1)).doFilter(request, response);
        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    void doFilterInternal_UserEmailNull() throws ServletException, IOException {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer validJwtToken");
        MockHttpServletResponse response = new MockHttpServletResponse();

        when(jwtService.extractUsername("validJwtToken")).thenReturn(null);

        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        verify(filterChain, times(1)).doFilter(request, response);
        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }


    @Test
    void doFilterInternal_ValidJwt_InvalidToken() throws ServletException, IOException {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer validJwtToken");
        MockHttpServletResponse response = new MockHttpServletResponse();

        when(jwtService.extractUsername("validJwtToken")).thenReturn("user@example.com");
        when(customUserDetailsService.loadUserByUsername("user@example.com")).thenReturn(userDetails);
        when(jwtService.isTokenValid("validJwtToken", userDetails)).thenReturn(false);

        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        verify(filterChain, times(1)).doFilter(request, response);
        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    public void testDoFilterInternal_ValidAuthHeader_UserEmailNull() throws ServletException, IOException {
        request.addHeader("Authorization", "Bearer validJwt");

        when(jwtService.extractUsername("validJwt")).thenReturn(null);

        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        verify(filterChain, times(1)).doFilter(request, response);
        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    void doFilterInternal_AuthenticationNotNull() throws ServletException, IOException {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer validJwtToken");
        MockHttpServletResponse response = new MockHttpServletResponse();

        Authentication existingAuth = mock(Authentication.class);
        SecurityContextHolder.getContext().setAuthentication(existingAuth);

        when(jwtService.extractUsername("validJwtToken")).thenReturn("user@example.com");

        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        verify(filterChain, times(1)).doFilter(request, response);
        assertEquals(existingAuth, SecurityContextHolder.getContext().getAuthentication());
    }


    @Test
    public void testDoFilterInternal_ValidAuthHeader_ValidToken_AuthenticationNull() throws ServletException, IOException {
        request.addHeader("Authorization", "Bearer validJwt");

        when(jwtService.extractUsername("validJwt")).thenReturn("user@example.com");
        UserDetails userDetails = mock(UserDetails.class);
        when(customUserDetailsService.loadUserByUsername("user@example.com")).thenReturn(userDetails);
        when(jwtService.isTokenValid("validJwt", userDetails)).thenReturn(true);

        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        verify(filterChain, times(1)).doFilter(request, response);
        assertNotNull(SecurityContextHolder.getContext().getAuthentication());
    }
/*
    @Test
    void doFilterInternal_ValidJwt_UserEmailIsNull() throws ServletException, IOException {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer valid.jwt.token");
        MockHttpServletResponse response = new MockHttpServletResponse();

        when(jwtService.extractUsername("valid.jwt.token")).thenReturn(null);

        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        verify(jwtService, times(1)).extractUsername("valid.jwt.token");
        verify(filterChain, times(1)).doFilter(request, response);
        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

*/
    @Test
    public void testDoFilterInternal_ValidAuthHeader_ValidToken_ValidUserEmail_AuthenticationNull() throws ServletException, IOException {
        request.addHeader("Authorization", "Bearer validJwt");

        when(jwtService.extractUsername("validJwt")).thenReturn("user@example.com");
        UserDetails userDetails = mock(UserDetails.class);
        when(customUserDetailsService.loadUserByUsername("user@example.com")).thenReturn(userDetails);
        when(jwtService.isTokenValid("validJwt", userDetails)).thenReturn(true);

        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        verify(filterChain, times(1)).doFilter(request, response);
        assertNotNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    public void doFilterInternal_validAuthHeader_shouldAuthenticate() throws ServletException, IOException {
        String token = "valid.jwt.token";
        String userEmail = "user@example.com";

        request.addHeader("Authorization", "Bearer " + token);

        when(jwtService.extractUsername(token)).thenReturn(userEmail);
        when(customUserDetailsService.loadUserByUsername(userEmail)).thenReturn(userDetails);
        when(jwtService.isTokenValid(token, userDetails)).thenReturn(true);

        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        verify(filterChain, times(1)).doFilter(request, response);
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        assertNotNull(authentication);
        assertEquals(userDetails, authentication.getPrincipal());
    }

    @Test
    public void doFilterInternal_exceptionThrown_shouldResolveException() throws ServletException, IOException {
        String token = "invalid.jwt.token";
        String userEmail = "user@example.com";

        request.addHeader("Authorization", "Bearer " + token);

        when(jwtService.extractUsername(token)).thenThrow(new RuntimeException("Invalid token"));

        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        verify(handlerExceptionResolver, times(1)).resolveException(any(), any(), any(), any());
        verify(filterChain, times(0)).doFilter(request, response);
        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    void doFilterInternal_UserEmailNotNull_AuthenticationNotNull() throws ServletException, IOException {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer validJwtToken");
        MockHttpServletResponse response = new MockHttpServletResponse();

        when(jwtService.extractUsername("validJwtToken")).thenReturn("user@example.com");

        Authentication existingAuthentication = new UsernamePasswordAuthenticationToken("user", "password");
        SecurityContextHolder.getContext().setAuthentication(existingAuthentication);

        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        verify(filterChain, times(1)).doFilter(request, response);
    }
}