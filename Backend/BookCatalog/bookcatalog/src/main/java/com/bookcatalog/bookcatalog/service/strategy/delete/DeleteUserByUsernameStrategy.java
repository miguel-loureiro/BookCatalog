package com.bookcatalog.bookcatalog.service.strategy.delete;

import com.bookcatalog.bookcatalog.model.Book;
import com.bookcatalog.bookcatalog.model.User;
import com.bookcatalog.bookcatalog.repository.BookRepository;
import com.bookcatalog.bookcatalog.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;

import java.io.IOException;
import java.util.Optional;


public class DeleteUserByUsernameStrategy implements  DeleteStrategy<User> {

    private final UserRepository userRepository;

    public DeleteUserByUsernameStrategy(UserRepository userRepository) {

        this.userRepository = userRepository;
    }

    @Override
    public void delete(User entity) throws IOException {

        Optional<User> userOptional = userRepository.findByUsername(entity.getUsername());

        if (userOptional.isEmpty()) {
            throw new EntityNotFoundException("User with username " + entity.getUsername() + " not found");
        }

        try {
            userRepository.delete(entity);
        } catch (Exception e) {

            throw new RuntimeException("An error occurred while deleting the user", e);
        }
    }
}



