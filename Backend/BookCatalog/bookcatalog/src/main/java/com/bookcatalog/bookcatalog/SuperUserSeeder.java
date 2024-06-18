package com.bookcatalog.bookcatalog;

import com.bookcatalog.bookcatalog.model.Role;
import com.bookcatalog.bookcatalog.model.User;
import com.bookcatalog.bookcatalog.model.dto.RegisterUserDto;
import com.bookcatalog.bookcatalog.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class SuperUserSeeder implements ApplicationListener<ContextRefreshedEvent> {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public SuperUserSeeder(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {
        this.createSuperUser();
    }

    private void createSuperUser() {
        String superUsername = "superuser";
        String superEmail = "superuser@email.com";

        // Check if the superuser already exists
        if (userRepository.findByUsername(superUsername).isPresent()) {
            System.out.println("Superuser already exists");
            return;
        }

        RegisterUserDto userDto = new RegisterUserDto();
        userDto.setUsername(superUsername);
        userDto.setEmail(superEmail);
        userDto.setPassword("superpassword");
        userDto.setRole(Role.SUPER);

        var newSuperUser = new User();
        newSuperUser.setUsername(userDto.getUsername());
        newSuperUser.setEmail(userDto.getEmail());
        newSuperUser.setPassword(passwordEncoder.encode(userDto.getPassword()));
        newSuperUser.setRole(userDto.getRole());

        userRepository.save(newSuperUser);
    }
}
