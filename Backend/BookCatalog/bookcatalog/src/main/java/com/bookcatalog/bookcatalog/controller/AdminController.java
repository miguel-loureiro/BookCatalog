package com.bookcatalog.bookcatalog.controller;

import com.bookcatalog.bookcatalog.model.User;
import com.bookcatalog.bookcatalog.model.dto.RegisterUserDto;
import com.bookcatalog.bookcatalog.model.dto.UserShortDto;
import com.bookcatalog.bookcatalog.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RequestMapping("/admin")
@RestController
public class AdminController {

    private final UserService userService;

    public AdminController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/signup")
    @PreAuthorize("hasRole('SUPER')")
    public ResponseEntity<User> createAdministrator(@RequestBody RegisterUserDto registerUserDto) {
        User createdAdmin = userService.createAdministrator(registerUserDto);

        return ResponseEntity.ok(createdAdmin);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('SUPER')")
    public ResponseEntity<Void> deleteAdministratorById(@PathVariable Integer id) {

        return userService.deleteAdministratorById(id);
    }

    @DeleteMapping("/delete")
    @PreAuthorize("hasRole('SUPER')")
    public ResponseEntity<Void> deleteAdministratorByUsernameOrEmail(@RequestParam String identifier) {

        return userService.deleteAdministratorByUsernameOrEmail(identifier);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('SUPER')")
    public ResponseEntity<Object> updateAdministratorById(@PathVariable Integer id, @RequestBody UserShortDto input) {

        return userService.updateAdministratorById(id, input);
    }

    @PutMapping("/update")
    @PreAuthorize("hasRole('SUPER')")
    public ResponseEntity<Object> updateAdministratorByUsernameOrEmail(@RequestParam String identifier, @RequestBody UserShortDto input) {

        return userService.updateAdministratorByUsernameOrEmail(identifier, input);
    }
}
