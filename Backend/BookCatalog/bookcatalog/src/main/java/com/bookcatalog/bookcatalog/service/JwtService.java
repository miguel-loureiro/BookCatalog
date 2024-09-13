package com.bookcatalog.bookcatalog.service;

import com.bookcatalog.bookcatalog.model.CustomUserDetails;
import com.bookcatalog.bookcatalog.model.Role;
import com.bookcatalog.bookcatalog.model.User;
import com.bookcatalog.bookcatalog.repository.UserRepository;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

@Service
public class JwtService {

    @Value("${security.jwt.secret}")
    String secretKey;

    @Value("${security.jwt.expiration}")
    long jwtExpiration;

    @Autowired
    private UserRepository userRepository;

    public JwtService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public String extractUsername(String token) {

        return extractClaim(token, Claims::getSubject);
    }

    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {

        final Claims claims = extractAllClaimsAllowExpired(token);
        return claimsResolver.apply(claims);
    }

    public String generateToken(CustomUserDetails userDetails) {

        if (userDetails == null || "guestuser".equals(userDetails.getUsername())) {

            CustomUserDetails guestUserDetails = createGuestUserDetails();
            return generateToken(new HashMap<>(), guestUserDetails);
        }

        Optional<User> userOptional = userRepository.findByUsername(userDetails.getUsername());
        if (userOptional.isPresent()) {
            return generateToken(new HashMap<>(), userDetails);
        } else {
            throw new UsernameNotFoundException("User not found or invalid role for token generation: " + userDetails.getUsername());
        }
    }

    public String generateToken(Map<String, Object> extraClaims, CustomUserDetails userDetails) {
        return buildToken(extraClaims, userDetails, jwtExpiration);
    }

    public long getExpirationTime() {
        return jwtExpiration;
    }

    public boolean isTokenValid(String token, UserDetails userDetails) {

        final String username = extractUsername(token);
        return (username.equals(userDetails.getUsername())) && !isTokenExpired(token);
    }

    String buildToken(
            Map<String, Object> extraClaims,
            CustomUserDetails userDetails,
            long expiration
    ) {
        return Jwts
                .builder()
                .setClaims(extraClaims)
                .setSubject(userDetails.getUsername())
                .claim("role", userDetails.getUser().getRole().name())
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(getSignInKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    Claims extractAllClaimsAllowExpired(String token) {
        try {
            return Jwts
                    .parserBuilder()
                    .setSigningKey(getSignInKey())
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
        } catch (ExpiredJwtException e) {
            return e.getClaims();  // Return claims even if the token is expired
        }
    }

    Key getSignInKey() {
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    private CustomUserDetails createGuestUserDetails() {
        // Create a User object for the GUEST user
        User guestUser = new User();
        guestUser.setUsername("guestuser");
        guestUser.setPassword(""); //
        guestUser.setRole(Role.GUEST);

        return new CustomUserDetails(guestUser);
    }
}
