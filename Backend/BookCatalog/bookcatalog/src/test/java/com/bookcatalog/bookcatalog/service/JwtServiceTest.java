package com.bookcatalog.bookcatalog.service;

import com.bookcatalog.bookcatalog.model.Role;
import com.bookcatalog.bookcatalog.model.User;
import com.bookcatalog.bookcatalog.repository.UserRepository;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

class JwtServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserDetails userDetails;

    @Mock
    private User mockUser;

    @Mock
    private Claims claims;

    @InjectMocks
    private JwtService jwtService;

    @Value("${security.jwt.secret}")
    private String secretKey = "supersecretkeyforjwtservicegeneratetokenforuser"; // Mocked value

    @Value("${security.jwt.expiration}")
    private long jwtExpiration = 86400000; // Mocked value (1 hour)

    @BeforeEach
    public void setUp() {

        MockitoAnnotations.openMocks(this);
        jwtService = new JwtService(userRepository);
        jwtService.secretKey = secretKey;
        jwtService.jwtExpiration = jwtExpiration;

        when(userDetails.getUsername()).thenReturn("testuser");
        mockUser = new User("testuser", "testuser@example.com", "password", Role.READER);
    }

    private String generateMockToken(Date expirationDate) {
        return Jwts.builder()
                .setSubject("testuser")
                .setExpiration(expirationDate)
                .signWith(Keys.hmacShaKeyFor(secretKey.getBytes()), SignatureAlgorithm.HS256)
                .compact();
    }

    private String generateTokenWithUsername(String username) {
        return Jwts.builder()
                .setClaims(new HashMap<>())
                .setSubject(username)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + jwtService.jwtExpiration))
                .signWith(Keys.hmacShaKeyFor(secretKey.getBytes()), SignatureAlgorithm.HS256)
                .compact();
    }

    private String generateTokenWithCustomClaims(String username, long expiration) {
        return Jwts.builder()
                .setSubject(username)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(jwtService.getSignInKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    @Test
    public void testGenerateToken_UserExists() {
        // Arrange
        User user = new User();
        user.setUsername("testuser");
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));

        Map<String, Object> extraClaims = new HashMap<>();

        // Act
        String mockToken = jwtService.generateToken(extraClaims, userDetails);

        // Assert
        assertNotNull(mockToken);
    }

    @Test
    public void testGenerateToken_UserNotFound() {
        // Arrange
        when(userRepository.findByUsername(anyString())).thenReturn(Optional.empty());

        // Act and Assert
        UsernameNotFoundException exception = assertThrows(UsernameNotFoundException.class, () -> {
            jwtService.generateToken(userDetails);
        });

        assertEquals("User not found with username: testuser", exception.getMessage());
        verify(userRepository, times(1)).findByUsername("testuser");
    }

    @Test
    public void testGetExpirationTime() {

        assertEquals(jwtExpiration, jwtService.getExpirationTime());
    }

    @Test
    public void testIsTokenExpired() {
        // Arrange
        UserDetails userDetails = mock(UserDetails.class);
        when(userDetails.getUsername()).thenReturn("testUser");

        long pastExpiration = -1000 * 60 * 60;

        String token = jwtService.buildToken(new HashMap<>(), userDetails, pastExpiration);

        // Act and Assert
        assertThrows(ExpiredJwtException.class, () -> {
            jwtService.isTokenExpired(token);
        });
    }

    @Test
    public void testIsTokenExpired_NotExpired() {
        // Arrange
        UserDetails userDetails = mock(UserDetails.class);
        when(userDetails.getUsername()).thenReturn("testUser");

        long expirationInPast = System.currentTimeMillis() + 1;
        String token = jwtService.buildToken(new HashMap<>(), userDetails, expirationInPast);

        // Act
        boolean isExpired = jwtService.isTokenExpired(token);

        // Assert
        assertFalse(isExpired);
    }

    @Test
    public void testExtractExpiration() {
        // Arrange
        UserDetails mockUserDetails = mock(UserDetails.class);
        when(mockUserDetails.getUsername()).thenReturn("testUser");

        String token = jwtService.buildToken(new HashMap<>(), mockUserDetails, 10000);

        // Act
        Date expiration = jwtService.extractExpiration(token);

        // Assert
        assertTrue(expiration.after(new Date(System.currentTimeMillis())));
    }

    @Test
    public void testGetSignInKey() {
        // Act
        Key key = jwtService.getSignInKey();

        // Assert
        System.out.println(key.getAlgorithm());
        assertNotNull(key);
        assertEquals(Keys.hmacShaKeyFor(secretKey.getBytes()).getAlgorithm(), key.getAlgorithm());
    }


    @Test
    void testIsTokenValidWithFutureToken() {
        // Arrange
        long futureExpiration = 1000 * 60 * 60; // 1 hour in the future
        String token = jwtService.buildToken(new HashMap<>(), userDetails, futureExpiration);

        // Act
        boolean isValid = jwtService.isTokenValid(token, userDetails);

        // Assert
        assertTrue(isValid, "The token should be valid because it is not expired yet");
    }


    //TODO: unit tests to fix in future iteration

    @Test
    void testIsTokenValid_UsernameMatchesAndTokenNotExpired() {
        // Arrange
        when(userDetails.getUsername()).thenReturn("testUser");
        String token = generateTokenWithCustomClaims("testUser", 1000 * 60 * 60); // Valid for 1 hour

        // Act
        boolean isValid = jwtService.isTokenValid(token, userDetails);

        // Assert
        assertTrue(isValid, "Token should be valid when username matches and token is not expired.");
    }

    @Test
    void testIsTokenValid_UsernameDoesNotMatchAndTokenNotExpired() {
        // Arrange
        when(userDetails.getUsername()).thenReturn("anotherUser");
        String token = generateTokenWithCustomClaims("testUser", 1000 * 60 * 60); // Valid for 1 hour

        // Act
        boolean isValid = jwtService.isTokenValid(token, userDetails);

        // Assert
        assertFalse(isValid, "Token should not be valid when username does not match.");
    }

}