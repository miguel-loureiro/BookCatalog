package com.bookcatalog.bookcatalog.service;

import com.bookcatalog.bookcatalog.exceptions.UserNotFoundException;
import com.bookcatalog.bookcatalog.model.Role;
import com.bookcatalog.bookcatalog.model.User;
import com.bookcatalog.bookcatalog.model.dto.*;
import com.bookcatalog.bookcatalog.repository.UserRepository;
import com.bookcatalog.bookcatalog.service.strategy.delete.DeleteStrategy;
import com.bookcatalog.bookcatalog.service.strategy.update.UpdateStrategy;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Map;
import java.util.Optional;

@Service
public class UserService {

    @Autowired
    private final UserRepository userRepository;

    private final PasswordEncoder passwordEncoder;

    private final Map<String, UpdateStrategy<User>> updateStrategiesMap;
    private final Map<String, DeleteStrategy<User>> deleteStrategiesMap;

    @Autowired
    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder,
                       Map<String, UpdateStrategy<User>> updateStrategiesMap, Map<String, DeleteStrategy<User>> deleteStrategiesMap) {

        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.updateStrategiesMap = updateStrategiesMap;
        this.deleteStrategiesMap = deleteStrategiesMap;
    }

    public ResponseEntity<Page<UserDto>> getAllUsers(int page, int size) {

        Sort usernameSort = Sort.by("username");
        Pageable paging = PageRequest.of(page, size, usernameSort.ascending());

        Page<User> usersPage = userRepository.findAll(paging);
        Page<UserDto> userDtosPage = usersPage.map(UserDto::new);

        return ResponseEntity.ok(userDtosPage);
    }

    /*
    Reasons for Direct Mapping from RegisterUserDto to User:
Security Concerns: Since passwords are sensitive data, handling them as little as possible is a good practice. By directly mapping from RegisterUserDto to User, you ensure that the password is only processed when necessary—during the creation of the User entity—and then immediately encoded. Introducing a UserDto in between could increase the risk of mishandling or inadvertently exposing the password.

Single Responsibility: The RegisterUserDto is specifically designed to capture the input needed for user registration, including the password. This aligns well with creating a User entity, which requires the password. The UserDto, on the other hand, is meant to encapsulate user data without sensitive information like the password. Thus, involving UserDto in the registration process could violate the single responsibility principle by forcing it to handle data it’s not designed for.

Avoiding Unnecessary Complexity: Mapping directly from RegisterUserDto to User keeps the code simple and clear. Introducing an extra step where RegisterUserDto is first converted to UserDto and then to User would add unnecessary complexity without significant benefits. It could also lead to potential issues, such as forgetting to encode the password properly during the conversion.

Performance: Directly converting RegisterUserDto to User avoids an extra transformation step, which might be negligible in terms of performance but still contributes to overall efficiency.
     */
    public UserDto createAdministrator(RegisterUserDto input) {

        User user = new User(input.getUsername(), input.getEmail(), passwordEncoder.encode(input.getPassword()), Role.ADMIN);
        User savedUser = userRepository.save(user);

        return new UserDto(savedUser);
    }

    public ResponseEntity<Void> deleteUser(String identifier, String type) throws IOException {

        try {
            DeleteStrategy<User> strategy = deleteStrategiesMap.get(type);

            if (strategy == null) {
                return ResponseEntity.badRequest().build();
            }

            User currentUser = getCurrentUser();
            User userToDelete = getUserByIdentifier(identifier, type);

            if (hasPermissionToDelete(currentUser, userToDelete)) {
                strategy.delete(userToDelete);
                return ResponseEntity.ok().build();
            } else {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }
        } catch (UserNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    public ResponseEntity<Void> deleteAdministrator(String identifier, String type) throws IOException {

        try {

            DeleteStrategy<User> strategy = deleteStrategiesMap.get(type);

            if (strategy == null) {
                return ResponseEntity.badRequest().build();
            }

            User currentUser = getCurrentUser();
            User adminToDelete = getUserByIdentifier(identifier, type);

            if (adminToDelete.getRole() != Role.ADMIN) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }

            if (hasPermissionToDelete(currentUser, adminToDelete)) {

                strategy.delete(adminToDelete);
                return ResponseEntity.ok().build();
            } else {

                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    public ResponseEntity<Void> updateUser(String identifier, String type, UserDto input) throws IOException {

        try {
            UpdateStrategy<User> strategy = updateStrategiesMap.get(type);

            if (strategy == null) {
                return ResponseEntity.badRequest().build();
            }

            User currentUser = getCurrentUser();
            User userToUpdate = getUserByIdentifier(identifier, type);

            if (hasPermissionToUpdate(currentUser, userToUpdate)) {

                User newDetails = new User(input);
                strategy.update(userToUpdate, newDetails, null);
                return ResponseEntity.ok().build();
            } else {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }
        } catch (UserNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    public ResponseEntity<Void> updateAdministrator(String identifier, String type, UserDto input) throws IOException {

        try {

            UpdateStrategy<User> strategy = updateStrategiesMap.get(type);

            if (strategy == null) {
                return ResponseEntity.badRequest().build();
            }

            User currentUser = getCurrentUser();
            User adminToUpdate = getUserByIdentifier(identifier, type);

            if (adminToUpdate.getRole() != Role.ADMIN) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }

            if (hasPermissionToUpdate(currentUser, adminToUpdate)) {

                User newDetails = new User(input);
                strategy.update(adminToUpdate, newDetails, null);
                return ResponseEntity.ok().build();
            } else {

                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }
        } catch (UserNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    public ResponseEntity<Void> changeUserPassword(String username, String newPassword) {

        User currentUser = getCurrentUser();

        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        if (!currentUser.getUsername().equals(username)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        Optional<User> userOptional = userRepository.findByUsername(username);
        if (userOptional.isEmpty()) {

            return ResponseEntity.notFound().build();
        }

        User user = userOptional.get();
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        return ResponseEntity.ok().build();
    }

    public User getUserByIdentifier(String identifier, String type) {

        switch (type) {
            case "id":
                try {
                    return userRepository.getReferenceById(Integer.parseInt(identifier));
                } catch (EntityNotFoundException | NumberFormatException e) {
                    throw new UserNotFoundException("User not found with id: " + identifier, e);
                }

            case "username":
                return userRepository.findByUsername(identifier)
                        .orElseThrow(() -> new UserNotFoundException("User with username " + identifier + " not found", null));

            case "email":
                return userRepository.findByEmail(identifier)
                        .orElseThrow(() -> new UserNotFoundException("User with email " + identifier + " not found", null));

            default:
                throw new IllegalArgumentException("Invalid identifier type: " + type);
        }
    }

    public User getCurrentUser() {

        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        if (principal instanceof UserDetails) {

            String username = ((UserDetails) principal).getUsername();
            return userRepository.findByUsername(username).orElse(null);
        }

        return null;
    }


    private boolean hasPermissionToDelete(User currentUser, User targetUser) {

        boolean isSameUser = currentUser.getUsername().equals(targetUser.getUsername());
        boolean hasHigherRank = currentUser.getRole().getRank() > targetUser.getRole().getRank();

        return isSameUser || hasHigherRank;
    }

    private boolean hasPermissionToUpdate(User currentUser, User targetUser) {

        boolean isSameUser = currentUser.getUsername().equals(targetUser.getUsername());
        boolean hasHigherRank = currentUser.getRole().getRank() > targetUser.getRole().getRank();

        return isSameUser || hasHigherRank;
    }
}
