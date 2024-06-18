package com.bookcatalog.bookcatalog.service;

import com.bookcatalog.bookcatalog.model.User;
import com.bookcatalog.bookcatalog.model.dto.RegisterUserDto;
import com.bookcatalog.bookcatalog.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public List<User> allUsers() {

        return (List<User>) userRepository.findAll();
    }

    public User createAdministrator(RegisterUserDto input) {

        var user = new User(input.getUsername(), input.getEmail(), input.getPassword(), input.getRole());

        return userRepository.save(user);
    }
}
