package com.bookcatalog.bookcatalog.service.strategy.delete;

import com.bookcatalog.bookcatalog.model.Book;
import com.bookcatalog.bookcatalog.model.User;
import com.bookcatalog.bookcatalog.repository.BookRepository;
import com.bookcatalog.bookcatalog.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;

import java.io.IOException;

public class DeleteUserByIdStrategy implements DeleteStrategy<User> {

    private final UserRepository userRepository;

    public DeleteUserByIdStrategy(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public void delete(User entity) throws IOException {

        if (entity == null || entity.getId() == null) {
            throw new EntityNotFoundException("User not found");
        }

        try {
            userRepository.delete(entity);
        } catch (EntityNotFoundException e) {
            throw new EntityNotFoundException("User with ID " + entity.getId() + " not found", e);
        } catch (Exception e) {
            throw new RuntimeException("An error occurred while deleting the user", e);
        }

    }
}
