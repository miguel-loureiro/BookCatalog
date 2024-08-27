package com.bookcatalog.bookcatalog.controller;

import com.bookcatalog.bookcatalog.exceptions.UserNotFoundException;
import com.bookcatalog.bookcatalog.model.CustomUserDetails;
import com.bookcatalog.bookcatalog.model.Role;
import com.bookcatalog.bookcatalog.model.dto.*;
import com.bookcatalog.bookcatalog.service.AuthenticationService;
import com.bookcatalog.bookcatalog.service.BookService;
import com.bookcatalog.bookcatalog.service.UserService;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import com.bookcatalog.bookcatalog.model.User;

import java.io.IOException;
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
/*
    @PostMapping("/signup")
    public ResponseEntity<String> createUserNonAdmin(@RequestBody RegisterUserDto input)  {

        if (input.getRole() != Role.READER && input.getRole() != Role.GUEST) {

            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Invalid role. Only READER or GUEST are allowed");
        }

        boolean userExistsInDatabase =
        return ResponseEntity.ok(createdUser);
    }
*/
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
        } catch (UserNotFoundException | IllegalArgumentException e) {

            return ResponseEntity.notFound().build();
        } catch (Exception e) {

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }



    @DeleteMapping("/{type}/{identifier}")
    @PreAuthorize("hasRole('SUPER') or hasRole('ADMIN')")
    public ResponseEntity<Void> deleteUser(@PathVariable String type, @PathVariable String identifier) throws IOException {

        return userService.deleteUser(identifier, type);
    }



}
