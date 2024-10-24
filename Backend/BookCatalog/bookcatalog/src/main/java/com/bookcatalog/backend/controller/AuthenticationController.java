package com.newbookcatalog.newbookcatalog.controller;

import com.newbookcatalog.newbookcatalog.model.CustomUserDetails;
import com.newbookcatalog.newbookcatalog.model.LoginResponse;
import com.newbookcatalog.newbookcatalog.model.User;
import com.newbookcatalog.newbookcatalog.model.dto.LoginUserDto;
import com.newbookcatalog.newbookcatalog.service.AuthenticationService;
import com.newbookcatalog.newbookcatalog.service.JwtService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
public class AuthenticationController {
    private final JwtService jwtService;
    private final AuthenticationService authenticationService;

    public AuthenticationController(JwtService jwtService, AuthenticationService authenticationService) {
        this.jwtService = jwtService;
        this.authenticationService = authenticationService;
    }

    @PostMapping("/login")
    @PreAuthorize("hasRole('SUPER') or hasRole('ADMIN') or hasRole('READER')")
    public ResponseEntity<LoginResponse> authenticate(@RequestBody LoginUserDto loginUserDto) {

        if (loginUserDto == null) {
            return ResponseEntity.badRequest().build();
        }

        SecurityContextHolder.clearContext();

        User authenticatedUser = authenticationService.authenticate(loginUserDto);

        if (authenticatedUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        CustomUserDetails customUserDetails = new CustomUserDetails(authenticatedUser);

        String jwtToken;

        try {

            jwtToken = jwtService.generateToken(customUserDetails);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }

        if (jwtToken == null) {

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new LoginResponse().setToken(null));
        }

        long expirationTime = jwtService.getExpirationTime();

        UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                customUserDetails, null, customUserDetails.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(authToken);

        LoginResponse response = new LoginResponse()
                .setToken(jwtToken)
                .setExpiresIn(expirationTime);

        return ResponseEntity.ok(response);
    }
}
