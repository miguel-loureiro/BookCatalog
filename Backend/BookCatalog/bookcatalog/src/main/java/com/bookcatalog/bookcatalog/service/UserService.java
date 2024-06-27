package com.bookcatalog.bookcatalog.service;

import com.bookcatalog.bookcatalog.model.Role;
import com.bookcatalog.bookcatalog.model.User;
import com.bookcatalog.bookcatalog.model.dto.*;
import com.bookcatalog.bookcatalog.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public List<UserShortDto> getUsersShortList() {

        List<User> users = (List<User>) userRepository.findAll();
        return users.stream()
                .filter(user -> user.getRole() != Role.SUPER)
                .map(user -> new UserShortDto(user.getUsername(), user.getEmail(), user.getRole()))
                .collect(Collectors.toList());
    }

    public User createAdministrator(RegisterUserDto input) {

        input.setRole(Role.ADMIN);
        var user = new User(input.getUsername(), input.getEmail(), passwordEncoder.encode(input.getPassword()), input.getRole());

        return userRepository.save(user);
    }

    public ResponseEntity<Void> deleteAdministratorById(Integer id) {

        Optional<User> userOptional = userRepository.findById(id);

        if(userOptional.isPresent()) {

            User user = userOptional.get();

            if (user.getRole() == Role.ADMIN ) {
                userRepository.deleteById(id);

                return ResponseEntity.ok().build();
            } else {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    public ResponseEntity<Void> deleteAdministratorByUsernameOrEmail(String identifier) {

        Optional<User> userOptional = userRepository.findByUsername(identifier);

        if (userOptional.isEmpty()) {
            userOptional = userRepository.findByEmail(identifier);
        }
        if (userOptional.isPresent()) {

            User user = userOptional.get();

            if (user.getRole() == Role.ADMIN) {
                userRepository.deleteById(user.getId());
                return ResponseEntity.ok().build();
            } else {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    public Optional<UserDto> getUserById(Integer id) {

        Optional<User> foundUser = userRepository.findById(id);

        if(foundUser.isPresent()) {

            UserDto currentUser = getCurrentUser();

            if (currentUser != null && (currentUser.getRole() == Role.READER || currentUser.getRole() == Role.GUEST)) {
                if (foundUser.get().getRole() == Role.ADMIN || foundUser.get().getRole() == Role.SUPER) {
                    return Optional.empty(); // Don't return user if they have a restricted role
                }
            }
            return foundUser.map(this::fromUserToUserDto);
        }
        return Optional.empty();

    }

    public Optional<UserDto> getUserByUsernameOrEmail(String identifier) {

        if (identifier != null) {
            Optional<User> userOptional = userRepository.findByUsername(identifier);
            if (userOptional.isEmpty()) {
                userOptional = userRepository.findByEmail(identifier);
            }
            if (userOptional.isPresent()) {

                UserDto currentUser = getCurrentUser();

                if (currentUser != null && (currentUser.getRole() == Role.READER || currentUser.getRole() == Role.GUEST)) {

                    if (userOptional.get().getRole() == Role.ADMIN || userOptional.get().getRole() == Role.SUPER) {
                        return Optional.empty(); // Don't return user if they have a restricted role
                    }
                }
                return userOptional.map(this::fromUserToUserDto);
            }
        }
        return Optional.empty();
    }

    public ResponseEntity<Void> deleteUserById(Integer id) {

        Optional<User> userOptional = userRepository.findById(id);
        UserDto currentUser = getCurrentUser();

        if (userOptional.isEmpty() || currentUser == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }

        User user = userOptional.get();

        if (currentUser.getRole() != Role.SUPER && currentUser.getRole() != Role.ADMIN) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        if (user.getRole() != Role.READER && user.getRole() != Role.GUEST) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        userRepository.deleteById(id);
        return ResponseEntity.ok().build();
    }


    public ResponseEntity<Void> deleteUserByUsernameOrEmail(String identifier) {

        Optional<User> userOptional = userRepository.findByUsername(identifier);
        if (userOptional.isEmpty()) {
            userOptional = userRepository.findByEmail(identifier);
        }
        UserDto currentUser = getCurrentUser();

        if (userOptional.isPresent() && currentUser != null) {
            User user = userOptional.get();

            if ((currentUser.getRole() == Role.READER || currentUser.getRole() == Role.GUEST)
                    && (user.getRole() == Role.ADMIN || user.getRole() == Role.SUPER)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }

            if ((currentUser.getRole() == Role.GUEST && user.getRole() == Role.READER) ||
                    (currentUser.getRole() == Role.READER && user.getRole() == Role.GUEST)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }

            if (currentUser.getRole() == Role.ADMIN || currentUser.getRole() == Role.SUPER ||
                    currentUser.getRole() == user.getRole()) {
                userRepository.deleteById(user.getId());
                return ResponseEntity.ok().build();
            } else {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    public ResponseEntity<Object> updateUserById(Integer id, UserShortDto input) {

        UserDto currentUser = getCurrentUser();

        return userRepository.findById(id).map(user -> {

            if (currentUser.getRole() != Role.SUPER && currentUser.getRole() != Role.ADMIN) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }

            if (user.getRole() != Role.READER && user.getRole() != Role.GUEST) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }

            if (input.getRole() != null) {
                if (input.getRole() == Role.SUPER || input.getRole() == Role.ADMIN) {
                    return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
                }
                user.setRole(input.getRole());
            }

            user.setUsername(input.getUsername());
            user.setEmail(input.getEmail());
            userRepository.save(user);

            return ResponseEntity.ok().build();
        }).orElseGet(() -> ResponseEntity.notFound().build());
    }

    public ResponseEntity<Object> updateUserByUsernameOrEmail(String identifier, UserShortDto input) {

        Optional<User> userOptional = userRepository.findByUsername(identifier);
        if (userOptional.isEmpty()) {
            userOptional = userRepository.findByEmail(identifier);
        }

        UserDto currentUser = getCurrentUser();

        return userOptional.map(user -> {
            if (currentUser.getRole() != Role.SUPER && currentUser.getRole() != Role.ADMIN) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }

            if (user.getRole() != Role.READER && user.getRole() != Role.GUEST) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }

            if (input.getRole() != null) {
                if (input.getRole() == Role.SUPER || input.getRole() == Role.ADMIN) {
                    return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
                }
                user.setRole(input.getRole());
            }

            user.setUsername(input.getUsername());
            user.setEmail(input.getEmail());
            userRepository.save(user);

            return ResponseEntity.ok().build();
        }).orElseGet(() -> ResponseEntity.notFound().build());
    }

    public ResponseEntity<Object> updateAdministratorById(Integer id, UserShortDto input) {

        return userRepository.findById(id)
                .map(user -> {

                    if (user.getRole() != Role.ADMIN) {
                        return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
                    }

                    if (input.getRole() != null && input.getRole() == Role.SUPER) {

                        return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
                    }

                    if (input.getRole() != null) {

                        user.setRole(input.getRole());
                    }

                    user.setUsername(input.getUsername());
                    user.setEmail(input.getEmail());
                    userRepository.save(user);
                    return ResponseEntity.ok().build();

                })
                .orElse(ResponseEntity.notFound().build());
    }

    public ResponseEntity<Object> updateAdministratorByUsernameOrEmail(String identifier, UserShortDto input) {

        Optional<User> userOptional = userRepository.findByUsername(identifier);

        if (userOptional.isEmpty()) {
            userOptional = userRepository.findByEmail(identifier);
        }

        return userOptional.map(user -> {
            if (user.getRole() != Role.ADMIN) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }

            if (input.getRole() != null && input.getRole() == Role.SUPER) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }

            if (input.getRole() != null) {
                user.setRole(input.getRole());
            }

            user.setUsername(input.getUsername());
            user.setEmail(input.getEmail());
            userRepository.save(user);
            return ResponseEntity.ok().build();
        }).orElse(ResponseEntity.notFound().build());
    }

    public UserDto getCurrentUser() {

        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        if (principal instanceof UserDetails) {
            String username = ((UserDetails) principal).getUsername();
            return userRepository.findByUsername(username).map(this::fromUserToUserDto).orElse(null);
        }
        return null;
    }

    private UserDto fromUserToUserDto(User user) {

        UserDto userDto = new UserDto();
        userDto.setUsername(user.getUsername());
        userDto.setEmail(user.getEmail());
        userDto.setRole(user.getRole());
        userDto.setBooks(user.getBooks());

        return userDto;
    }

    private UserShortDto fromUserToUserShortDto(User user) {

        UserShortDto userShortDto = new UserShortDto();
        userShortDto.setUsername(user.getUsername());
        userShortDto.setEmail(user.getEmail());
        userShortDto.setRole(user.getRole());

        return userShortDto;
    }
}
