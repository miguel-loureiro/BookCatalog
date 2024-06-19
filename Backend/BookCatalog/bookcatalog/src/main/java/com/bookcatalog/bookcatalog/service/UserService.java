package com.bookcatalog.bookcatalog.service;

import com.bookcatalog.bookcatalog.model.Role;
import com.bookcatalog.bookcatalog.model.User;
import com.bookcatalog.bookcatalog.model.dto.RegisterUserDto;
import com.bookcatalog.bookcatalog.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public List<User> allUsers() {

        return (List<User>) userRepository.findAll();
    }

    public User createAdministrator(RegisterUserDto input) {

        input.setRole(Role.ADMIN);
        var user = new User(input.getUsername(), input.getEmail(), passwordEncoder.encode(input.getPassword()), input.getRole());

        return userRepository.save(user);
    }

    public User createUser(RegisterUserDto input) {

        var user = new User(input.getUsername(), input.getEmail(), passwordEncoder.encode(input.getPassword()), input.getRole());

        return userRepository.save(user);
    }

    public Optional<User> getUserById(Integer id) {

        return userRepository.findById(id);
    }
}
