package com.bookcatalog.bookcatalog.service.strategy.update;

import com.bookcatalog.bookcatalog.model.User;
import com.bookcatalog.bookcatalog.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;

import java.io.IOException;
import java.util.Optional;

public class UpdateUserByIdStrategy implements UpdateStrategy<User> {

    private final UserRepository userRepository;

    public UpdateUserByIdStrategy(UserRepository userRepository) {

        this.userRepository = userRepository;
    }

    @Override
    public User update(User user, User newDetails, String filename) throws IOException {

        if (user == null) {
            throw new EntityNotFoundException("User not found");
        }

        if (newDetails == null) {
            throw new IllegalArgumentException("New details cannot be null");
        }

        user.setEmail(newDetails.getEmail());
        user.setPassword(newDetails.getPassword());
        user.setRole(newDetails.getRole());
        user.setBooks(newDetails.getBooks());


        try {
            return userRepository.save(user);
        } catch (Exception e) {

            throw new RuntimeException("An error occurred while updating the user", e);
        }
    }
}
