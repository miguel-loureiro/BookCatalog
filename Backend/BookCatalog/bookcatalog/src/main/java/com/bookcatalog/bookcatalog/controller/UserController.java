package com.bookcatalog.bookcatalog.controller;

import com.bookcatalog.bookcatalog.model.CustomUserDetails;
import com.bookcatalog.bookcatalog.model.Role;
import com.bookcatalog.bookcatalog.model.dto.*;
import com.bookcatalog.bookcatalog.service.AuthenticationService;
import com.bookcatalog.bookcatalog.service.BookService;
import com.bookcatalog.bookcatalog.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import com.bookcatalog.bookcatalog.model.User;

import java.util.List;
import java.util.Optional;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;

@RequestMapping("/user")
@RestController
public class UserController {

    private final UserService userService;
    private final AuthenticationService authenticationService;

    public UserController(UserService userService, AuthenticationService authenticationService) {
        this.userService = userService;
        this.authenticationService = authenticationService;
    }

    @GetMapping("/all")
    @PreAuthorize("hasRole('SUPER') or hasRole('ADMIN')")
    public ResponseEntity<List<UserShortDto>> allUsers() {

        UserDto currentUser = userService.getCurrentUser();
        if (currentUser == null || (currentUser.getRole() != Role.SUPER && currentUser.getRole() != Role.ADMIN)) {

            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        List<UserShortDto> users = userService.getUsersShortList();

        return ResponseEntity.ok(users);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('SUPER') or hasRole('ADMIN')")
    public ResponseEntity<UserDto> getUserById(@PathVariable Integer id) {

        Optional<UserDto> user = userService.getUserById(id);

        return user.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/search")
    @PreAuthorize("hasRole('SUPER') or hasRole('ADMIN')")
    public ResponseEntity<UserDto> getUserByUsernameOrEmail(@RequestParam String identifier) {

        if (identifier == null) {
            return ResponseEntity.badRequest().build();
        }
        Optional<UserDto> user = userService.getUserByUsernameOrEmail(identifier);

        return user.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('SUPER') or hasRole('ADMIN')")
    public ResponseEntity<Void> deleteUserById(@PathVariable Integer id) {

        return userService.deleteUserById(id);
    }

    @DeleteMapping("/delete")
    @PreAuthorize("hasRole('SUPER') or hasRole('ADMIN')")
    public ResponseEntity<Void> deleteUserByUsernameOrEmail(@RequestParam String identifier) {

        return userService.deleteUserByUsernameOrEmail(identifier);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('SUPER') or hasRole('ADMIN')")
    public ResponseEntity<Object> updateUserById(@PathVariable Integer id, @RequestBody UserShortDto userShortDto) {

        return userService.updateUserById(id, userShortDto);
    }

    @PutMapping("/update")
    @PreAuthorize("hasRole('SUPER') or hasRole('ADMIN')")
    public ResponseEntity<Object> updateUserByUsernameOrEmail(@RequestParam String identifier, @RequestBody UserShortDto userShortDto) {

        return userService.updateUserByUsernameOrEmail(identifier, userShortDto);
    }
}
