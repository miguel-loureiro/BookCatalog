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
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
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

        User authenticatedUser = authenticationService.authenticate(loginUserDto);

        if (authenticatedUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();  // Authentication failed
        }

        CustomUserDetails customUserDetails = new CustomUserDetails(authenticatedUser);

        String jwtToken = jwtService.generateToken(customUserDetails);
        long expirationTime = jwtService.getExpirationTime();

        LoginResponse response = new LoginResponse()
                .setToken(jwtToken)
                .setExpiresIn(expirationTime);

        return ResponseEntity.ok(response);
    }
}
