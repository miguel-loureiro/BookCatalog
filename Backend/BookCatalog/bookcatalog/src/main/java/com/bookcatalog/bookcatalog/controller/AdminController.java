package com.bookcatalog.bookcatalog.controller;

import com.bookcatalog.bookcatalog.exceptions.UserNotFoundException;
import com.bookcatalog.bookcatalog.model.Role;
import com.bookcatalog.bookcatalog.model.User;
import com.bookcatalog.bookcatalog.model.dto.RegisterUserDto;
import com.bookcatalog.bookcatalog.model.dto.UserDto;
import com.bookcatalog.bookcatalog.service.UserService;
import org.springframework.data.domain.Page;
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

    @PostMapping("/new")
    @PreAuthorize("hasRole('SUPER')")
    public ResponseEntity<UserDto> createAdministrator(@RequestBody RegisterUserDto registerUserDto) throws IOException {
        UserDto createdAdmin = userService.createAdministrator(registerUserDto);

        return ResponseEntity.ok(createdAdmin);
    }

    @GetMapping("/all")
    @PreAuthorize("hasRole('SUPER') or hasRole('ADMIN')")
    public ResponseEntity<Page<UserDto>> allUsers()  {

        Page<UserDto> users = userService.getAllUsers(0, 10).getBody();

        return ResponseEntity.ok(users);
    }

    @GetMapping("/{type}/{identifier}")
    @PreAuthorize("hasRole('SUPER') or hasRole('ADMIN')")
    public ResponseEntity<UserDto> getUser(@PathVariable String type, @PathVariable String identifier) {
        try {
            User user = userService.getUserByIdentifier(identifier, type);
            UserDto userDto = new UserDto(user);
            return ResponseEntity.ok(userDto);
        } catch (UserNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(null);
        }
    }

    @DeleteMapping("/{type}/{identifier}")
    @PreAuthorize("hasRole('SUPER')")
    public ResponseEntity<Void> deleteAdministrator(@PathVariable String type, @PathVariable String identifier) throws IOException {

       return userService.deleteAdministrator(identifier, type);
    }

    @PutMapping(value = "/{id}/{identifier}" , consumes = {"multipart/form-data"})
    @PreAuthorize("hasRole('SUPER')")
    public ResponseEntity<Void> updateAdministrator(@PathVariable String type, @PathVariable String identifier, @RequestPart(name = "user") UserDto userDetails) throws IOException {

        return userService.updateAdministrator(identifier, type, userDetails);
    }
}
