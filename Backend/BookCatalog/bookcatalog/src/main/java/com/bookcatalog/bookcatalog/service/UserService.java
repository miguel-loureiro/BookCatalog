package com.bookcatalog.bookcatalog.service;

import com.bookcatalog.bookcatalog.exceptions.UserNotFoundException;
import com.bookcatalog.bookcatalog.model.Role;
import com.bookcatalog.bookcatalog.model.User;
import com.bookcatalog.bookcatalog.model.dto.*;
import com.bookcatalog.bookcatalog.repository.UserRepository;
import com.bookcatalog.bookcatalog.service.strategy.StrategyFactory;
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
import java.util.Optional;

@Service
public class UserService {

    @Autowired
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final StrategyFactory<User> strategyFactory;

    @Autowired
    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder, StrategyFactory<User> strategyFactory) {

        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.strategyFactory = strategyFactory;
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

    public ResponseEntity<UserDto> createUser(RegisterUserDto input) {

        Optional<User> currentUserOpt = getCurrentUser();
        if (currentUserOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(null); // No current user found
        }
        User currentUser = currentUserOpt.get();

        if (currentUser.getRole() == Role.ADMIN && input.getRole() != Role.READER) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(null);
        }
        if (currentUser.getRole() == Role.SUPER && (input.getRole() != Role.ADMIN && input.getRole() != Role.READER)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(null); // Super can't create roles other than Admin or Reader
        }

        boolean userExists = userRepository.findByEmail(input.getEmail()).isPresent() ||
                userRepository.findByUsername(input.getUsername()).isPresent();

        if (userExists) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .header("Error-Message", "User already in the database")
                    .body(null);
        }

        User user = new User(input.getUsername(), input.getEmail(),
                passwordEncoder.encode(input.getPassword()), input.getRole());
        User savedUser = userRepository.save(user);

        return ResponseEntity.status(HttpStatus.CREATED).body(new UserDto(savedUser));
    }

    public ResponseEntity<Void> deleteUser(String identifier, String type) throws IOException {

        try {

            DeleteStrategy<User> strategy = strategyFactory.getDeleteStrategy(type);
            if (strategy == null) {
                return ResponseEntity.badRequest().build();
            }

            Optional<User> currentUserOpt = getCurrentUser();
            if (currentUserOpt.isEmpty()) {
                return ResponseEntity.notFound().build();
            }
            User currentUser = currentUserOpt.get();

            Optional<User> userToDeleteOpt = getUserByIdentifier(identifier, type);
            if (userToDeleteOpt.isEmpty()) {
                return ResponseEntity.notFound().build();
            }
            User userToDelete = userToDeleteOpt.get();

            if (userToDelete.getRole() == Role.ADMIN) {

                boolean canDeleteAdmin = currentUser.getRole() == Role.SUPER ||
                        (currentUser.getRole() == Role.ADMIN && currentUser.getUsername().equals(userToDelete.getUsername()));
                if (!canDeleteAdmin) {
                    return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
                }
            }

            if (!hasPermissionToDeleteUser(currentUser, userToDelete)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }

            strategy.delete(userToDelete);
            return ResponseEntity.ok().build();

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    public ResponseEntity<Void> updateUser(String identifier, String type, UserDto input) throws IOException {
        try {

            UpdateStrategy<User> strategy = strategyFactory.getUpdateStrategy(type);
            if (strategy == null) {
                return ResponseEntity.badRequest().build();
            }

            Optional<User> currentUserOpt = getCurrentUser();
            if (currentUserOpt.isEmpty()) {
                return ResponseEntity.notFound().build();
            }
            User currentUser = currentUserOpt.get();

            Optional<User> userToUpdateOpt = getUserByIdentifier(identifier, type);
            if (userToUpdateOpt.isEmpty()) {
                return ResponseEntity.notFound().build();
            }
            User userToUpdate = userToUpdateOpt.get();

            if (userToUpdate.getRole() == Role.ADMIN) {

                boolean canUpdateAdmin = currentUser.getRole() == Role.SUPER ||
                        (currentUser.getRole() == Role.ADMIN && currentUser.getUsername().equals(userToUpdate.getUsername()));
                if (!canUpdateAdmin) {
                    return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
                }
            }

            if (!hasPermissionToUpdateUser(currentUser, userToUpdate)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }

            User newDetails = new User(input);
            strategy.update(userToUpdate, newDetails, null);
            return ResponseEntity.ok().build();

        } catch (UserNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    public ResponseEntity<Void> changeUserPassword(String username, String newPassword) {

        Optional<User> currentUserOpt = getCurrentUser();

        if (currentUserOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        User currentUser = currentUserOpt.get();

        if (!currentUser.getUsername().equals(username)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        currentUser.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(currentUser);

        return ResponseEntity.ok().build();
    }

    public Optional<User> getUserByIdentifier(String identifier, String type) {

        switch (type) {

            case "id":
                try {

                    return Optional.of(userRepository.getReferenceById(Integer.parseInt(identifier)));
                } catch (EntityNotFoundException | NumberFormatException e) {

                    return Optional.empty();
                }
            case "username":

                return userRepository.findByUsername(identifier);
            case "email":

                return userRepository.findByEmail(identifier);
            default:
                throw new IllegalArgumentException("Invalid identifier type: " + type);
        }
    }

    public Optional<User> getCurrentUser() {

        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        if (principal instanceof UserDetails) {

            String username = ((UserDetails) principal).getUsername();
            return userRepository.findByUsername(username);
        }

        return Optional.empty();
    }

    private boolean hasPermissionToDeleteUser(User currentUser, User targetUser) {

        Role currentUserRole = currentUser.getRole();
        Role targetUserRole = targetUser.getRole();
        boolean isSameUser = currentUser.getUsername().equals(targetUser.getUsername());

        return switch (currentUserRole) {
            case SUPER -> !isSameUser;
            case ADMIN -> isSameUser || targetUserRole == Role.READER;
            case READER -> isSameUser;
            default -> false;
        };
    }

    private boolean hasPermissionToUpdateUser(User currentUser, User targetUser) {

        Role currentUserRole = currentUser.getRole();
        Role targetUserRole = targetUser.getRole();
        boolean isSameUser = currentUser.getUsername().equals(targetUser.getUsername());

        return switch (currentUserRole) {
            case SUPER -> !isSameUser;
            case ADMIN -> isSameUser || targetUserRole == Role.READER;
            case READER -> isSameUser;
            default -> false;
        };
    }
}
