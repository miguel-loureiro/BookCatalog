package com.bookcatalog.bookcatalog.service;

import com.bookcatalog.bookcatalog.model.User;
import com.bookcatalog.bookcatalog.repository.UserRepository;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

class JwtServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserDetails userDetails;

    @Mock
    private Claims claims;

    @Mock
    private SecurityContext securityContext;

    @InjectMocks
    private JwtService jwtService;

    @Value("${security.jwt.secret}")
    private String secretKey = "supersecretkeyforjwtservicegeneratetokenforuser"; // Mocked value

    @Value("${security.jwt.expiration}")
    private long jwtExpiration = 3600000L; // Mocked value (1 hour)

    @BeforeEach
    public void setUp() {

        MockitoAnnotations.openMocks(this);
        jwtService = new JwtService(userRepository);
        jwtService.secretKey = secretKey;
        jwtService.jwtExpiration = jwtExpiration;
    }

    private String generateMockToken(Date expirationDate) {
        return Jwts.builder()
                .setSubject("testuser")
                .setExpiration(expirationDate)
                .signWith(Keys.hmacShaKeyFor(secretKey.getBytes()), SignatureAlgorithm.HS256)
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
        String token = jwtService.generateToken(extraClaims, userDetails);

        // Assert
        assertNotNull(token);
        verify(userRepository, never()).save(user); // Ensure save is not called
    }

    @Test
    public void testGenerateToken_UserNotFound() {
        // Arrange
        when(userRepository.findByUsername(anyString())).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(UsernameNotFoundException.class, () -> {
            jwtService.generateToken(userDetails);
        });
    }

    @Test
    public void testGetExpirationTime() {

        assertEquals(jwtExpiration, jwtService.getExpirationTime());
    }

    @Test
    public void testIsTokenExpired() {
        // Arrange
        Date pastDate = new Date(System.currentTimeMillis() - 10000); // 10 seconds in the past
        String token = generateMockToken(pastDate);
        when(jwtService.extractExpiration(token)).thenReturn(pastDate);

        // Act
        boolean isExpired = jwtService.isTokenExpired(token);

        // Assert
        assertTrue(isExpired);
    }

    @Test
    public void testIsTokenExpired_NotExpired() {
        // Arrange
        Date futureDate = new Date(System.currentTimeMillis() + 10000); // 10 seconds in the future
        String token = generateMockToken(futureDate);

        // Mock extractExpiration to return the future date
        when(jwtService.extractExpiration(token)).thenCallRealMethod();

        // Act
        boolean isExpired = jwtService.isTokenExpired(token);

        // Assert
        assertFalse(isExpired);
    }

    @Test
    public void testExtractExpiration() {
        // Arrange
        Date expectedExpiration = new Date(System.currentTimeMillis() + 10000); // 10 seconds in the future
        String token = generateMockToken(expectedExpiration);

        // Act
        Date expiration = jwtService.extractExpiration(token);

        // Assert
        assertEquals(expectedExpiration, expiration);
    }

    @Test
    public void testExtractAllClaims() {
        // Arrange
        Date expirationDate = new Date(System.currentTimeMillis() + 10000);
        String token = generateMockToken(expirationDate);

        // Act
        Claims extractedClaims = jwtService.extractAllClaims(token);

        // Assert
        assertNotNull(extractedClaims);
        assertEquals("testuser", extractedClaims.getSubject());
        assertEquals(expirationDate, extractedClaims.getExpiration());
    }


    @Test
    public void testGetSignInKey() {
        // Act
        Key key = jwtService.getSignInKey();

        // Assert
        assertNotNull(key);
        assertEquals(Keys.hmacShaKeyFor(secretKey.getBytes()).getAlgorithm(), key.getAlgorithm());
    }
}