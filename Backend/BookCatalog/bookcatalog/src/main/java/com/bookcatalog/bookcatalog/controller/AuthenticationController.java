package com.bookcatalog.bookcatalog.controller;

import com.bookcatalog.bookcatalog.model.LoginResponse;
import com.bookcatalog.bookcatalog.model.Role;
import com.bookcatalog.bookcatalog.model.User;
import com.bookcatalog.bookcatalog.model.dto.LoginUserDto;
import com.bookcatalog.bookcatalog.model.dto.RegisterUserDto;
import com.bookcatalog.bookcatalog.service.AuthenticationService;
import com.bookcatalog.bookcatalog.service.JwtService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
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

    @PostMapping(value = "/signup/guest", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<User> registerGuest(@RequestBody RegisterUserDto registerUserDto) {
        registerUserDto.setRole(Role.GUEST);
        User registeredUser = authenticationService.signup(registerUserDto);
        return ResponseEntity.ok(registeredUser);
    }

    @PostMapping(value = "/signup/reader", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<User> registerReader(@RequestBody RegisterUserDto registerUserDto) {
        registerUserDto.setRole(Role.READER);
        User registeredUser = authenticationService.signup(registerUserDto);
        return ResponseEntity.ok(registeredUser);
    }

    @PostMapping(value = "/signup/admin", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<User> registerAdmin(@RequestBody RegisterUserDto registerUserDto) {
        registerUserDto.setRole(Role.ADMIN);
        User registeredUser = authenticationService.signup(registerUserDto);
        return ResponseEntity.ok(registeredUser);
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> authenticate(@RequestBody LoginUserDto loginUserDto) {
        User authenticatedUser = authenticationService.authenticate(loginUserDto);

        String jwtToken = jwtService.generateToken(authenticatedUser);
        long expirationTime = jwtService.getExpirationTime();

        LoginResponse response = new LoginResponse()
                .setToken(jwtToken)
                .setExpiresIn(expirationTime);

        return ResponseEntity.ok(response);
    }
}
