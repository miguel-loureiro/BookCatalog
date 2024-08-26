package com.bookcatalog.bookcatalog.service.strategy.delete;

import com.bookcatalog.bookcatalog.model.User;
import com.bookcatalog.bookcatalog.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;

import java.io.IOException;
import java.util.Optional;


public class DeleteUserByEmailStrategy implements  DeleteStrategy<User> {

    private final UserRepository userRepository;

    public DeleteUserByEmailStrategy(UserRepository userRepository) {

        this.userRepository = userRepository;
    }

    @Override
    public void delete(User entity) throws IOException {

        Optional<User> userOptional = userRepository.findByEmail(entity.getEmail());

        if (userOptional.isEmpty()) {
            throw new EntityNotFoundException("User with email " + entity.getEmail() + " not found");
        }

        try {
            userRepository.delete(entity);
        } catch (Exception e) {

            throw new RuntimeException("An error occurred while deleting the user", e);
        }
    }
}



