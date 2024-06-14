package com.bookcatalog.bookcatalog.service;

import com.bookcatalog.bookcatalog.model.User;
import com.bookcatalog.bookcatalog.model.dto.LoginUserDto;
import com.bookcatalog.bookcatalog.model.dto.RegisterUserDto;
import com.bookcatalog.bookcatalog.repository.UserRepository;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

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

        User user = new User();
        user.setUsername(input.getUsername());
        user.setEmail(input.getEmail());
        user.setPassword(passwordEncoder.encode(input.getPassword()));
        user.setRole(input.getRole());

        return userRepository.save(user);
    }

    public User authenticate(LoginUserDto input) {
        String identifier = input.getUsername() != null && !input.getUsername().isEmpty() ? input.getUsername() : input.getEmail();
        UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(identifier, input.getPassword());

        authenticationManager.authenticate(authenticationToken);

        Optional<User> user = input.getUsername() != null && !input.getUsername().isEmpty()
                ? userRepository.findByUsername(input.getUsername())
                : userRepository.findByEmail(input.getEmail());

        return user.orElseThrow(() -> new UsernameNotFoundException("User not found with identifier: " + identifier));
    }
}
