package com.bookcatalog.bookcatalog;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;
import com.bookcatalog.bookcatalog.model.Role;
import com.bookcatalog.bookcatalog.model.User;
import com.bookcatalog.bookcatalog.repository.UserRepository;

@Configuration
public class DataLoader {

    @Bean
    public CommandLineRunner loadData(UserRepository repository, PasswordEncoder passwordEncoder) {
        return args -> {
            long userCount = repository.count();

            boolean superUserExists = repository.findByRole(Role.SUPER).stream().findAny().isPresent();

            if (userCount == 0 && !superUserExists) {
                User superUser = new User("superadmin", "superadmin@example.com", passwordEncoder.encode("superpassword"), Role.SUPER);
                repository.save(superUser);
            }
        };
    }
}
