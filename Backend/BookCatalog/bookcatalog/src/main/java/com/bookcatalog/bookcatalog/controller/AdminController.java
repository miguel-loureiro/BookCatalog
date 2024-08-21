package com.bookcatalog.bookcatalog.controller;

import com.bookcatalog.bookcatalog.exceptions.UserNotFoundException;
import com.bookcatalog.bookcatalog.model.Role;
import com.bookcatalog.bookcatalog.model.User;
import com.bookcatalog.bookcatalog.model.dto.RegisterUserDto;
import com.bookcatalog.bookcatalog.model.dto.UserDto;
import com.bookcatalog.bookcatalog.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@RequestMapping("/admin")
@RestController
public class AdminController {

    private final UserService userService;

    public AdminController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/signup")
    @PreAuthorize("hasRole('SUPER')")
    public ResponseEntity<UserDto> createAdministrator(@RequestBody RegisterUserDto registerUserDto) throws IOException {
        UserDto createdAdmin = userService.createAdministrator(registerUserDto);

        return ResponseEntity.ok(createdAdmin);
    }

    @DeleteMapping("/{type}/{identifier}")
    @PreAuthorize("hasRole('SUPER')")
    public ResponseEntity<Void> deleteAdministrator(@PathVariable String type, @PathVariable String identifier) throws IOException {

       return userService.deleteAdministrator(identifier, type);
    }

}
