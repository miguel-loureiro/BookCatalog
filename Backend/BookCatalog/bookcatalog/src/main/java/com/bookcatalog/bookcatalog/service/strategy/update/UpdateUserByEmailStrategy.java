package com.bookcatalog.bookcatalog.service.strategy.update;

import com.bookcatalog.bookcatalog.model.User;
import com.bookcatalog.bookcatalog.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;

import java.io.IOException;
import java.util.Optional;

public class UpdateUserByEmailStrategy implements UpdateStrategy<User>{

    private final UserRepository userRepository;

    public UpdateUserByEmailStrategy(UserRepository userRepository) {

        this.userRepository = userRepository;
    }

    @Override
    public User update(User entity, User newDetails, String filename) throws IOException {

        Optional<User> userOptional = userRepository.findByEmail(entity.getEmail());

        if (userOptional.isEmpty()) {
            throw new EntityNotFoundException("User with email " + entity.getEmail() + " not found");
        }

        // Update the 'entity' (is a User) argument with 'newDetails'
        entity.setUsername(newDetails.getUsername());
        entity.setEmail(newDetails.getEmail());
        entity.setRole(newDetails.getRole());

        return userRepository.save(entity);
    }
}
