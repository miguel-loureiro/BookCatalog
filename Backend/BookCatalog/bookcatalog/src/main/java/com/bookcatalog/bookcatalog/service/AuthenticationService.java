package com.bookcatalog.bookcatalog.service;

import com.bookcatalog.bookcatalog.exceptions.InvalidUserRoleException;
import com.bookcatalog.bookcatalog.model.Role;
import com.bookcatalog.bookcatalog.model.User;
import com.bookcatalog.bookcatalog.model.dto.LoginUserDto;
import com.bookcatalog.bookcatalog.model.dto.RegisterUserDto;
import com.bookcatalog.bookcatalog.repository.UserRepository;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Optional;

@Service
public class AuthenticationService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;

    public AuthenticationService(UserRepository userRepository, PasswordEncoder passwordEncoder, AuthenticationManager authenticationManager) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
    }

    public User signup(RegisterUserDto input) {

        validateInput(input);

        if (input.getRole() == Role.READER) {

            User user = new User();
            user.setUsername(input.getUsername());
            user.setEmail(input.getEmail());
            user.setPassword(passwordEncoder.encode(input.getPassword()));
            user.setRole(input.getRole());

            validateUser(user);

            return userRepository.save(user);
        } else {
            throw new InvalidUserRoleException("Only READER role are allowed for signup.");
        }
    }

    public User authenticate(LoginUserDto input) {

        String identifier = input.getUsername() != null && !input.getUsername().isEmpty() ? input.getUsername() : input.getEmail();

        Optional<User> user = input.getUsername() != null && !input.getUsername().isEmpty()
                ? userRepository.findByUsername(input.getUsername())
                : userRepository.findByEmail(input.getEmail());

        User authenticatedUser = user.orElseThrow(() -> new UsernameNotFoundException("User not found with identifier: " + identifier));

        if (authenticatedUser.getRole() == Role.GUEST) {
            return authenticatedUser;
        }

        UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(identifier, input.getPassword());
        authenticationManager.authenticate(authenticationToken);

        return authenticatedUser;
    }

    public User getGuestUser() {
        // Return a user with GUEST role
        User guestUser = new User();
        guestUser.setUsername("guestuser");
        guestUser.setRole(Role.GUEST);
        return guestUser;
    }

    private void validateInput(RegisterUserDto input) {
        if (input == null || input.getUsername() == null || input.getEmail() == null || input.getPassword() == null || input.getRole() == null) {
            throw new IllegalArgumentException("All fields are required for registration.");
        }
    }

    private void validateUser(User user) {
        if (user == null || user.getUsername() == null || user.getEmail() == null || user.getPassword() == null || user.getRole() == null) {
            throw new IllegalStateException("User fields are not properly set.");
        }
    }
}
