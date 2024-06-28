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

        if (input.getRole() == Role.READER || input.getRole() == Role.GUEST) {
            User user = new User();
            user.setUsername(input.getUsername());
            user.setEmail(input.getEmail());
            user.setPassword(passwordEncoder.encode(input.getPassword()));
            user.setRole(input.getRole());

            return userRepository.save(user);
        } else {
            throw new InvalidUserRoleException("Only READER or GUEST roles are allowed for signup.");
        }
    }

    public User authenticate(LoginUserDto input) {
        String identifier = input.getUsername() != null && !input.getUsername().isEmpty() ? input.getUsername() : input.getEmail();
        UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(identifier, input.getPassword());

        authenticationManager.authenticate(authenticationToken);

        Optional<User> user = input.getUsername() != null && !input.getUsername().isEmpty()
                ? userRepository.findByUsername(input.getUsername())
                : userRepository.findByEmail(input.getEmail());

        User authenticatedUser = user.orElseThrow(() -> new UsernameNotFoundException("User not found with identifier: " + identifier));

        if (authenticatedUser.getRole() == Role.GUEST) {
            throw new InvalidUserRoleException("Cannot log in with role GUEST via this endpoint");
        }

        return authenticatedUser;
    }

    public User authenticateGuest(LoginUserDto loginUserDto) {

        Optional<User> optionalUser = userRepository.findByUsername(loginUserDto.getUsername());
        return optionalUser.filter(user -> user.getRole() == Role.GUEST)
                .filter(user -> passwordEncoder.matches(loginUserDto.getPassword(), user.getPassword()))
                .orElseThrow(() -> new UsernameNotFoundException("Guest user not found or invalid credentials"));
    }
}
