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
            repository.save(new User("superadmin", "superadmin@example.com", passwordEncoder.encode("superpassword"), Role.SUPER));
            repository.save(new User("admin", "admin@example.com", passwordEncoder.encode("adminpassword"), Role.ADMIN));
            repository.save(new User("reader1", "reader1@example.com", passwordEncoder.encode("readerpassword"), Role.READER));
            repository.save(new User("reader2", "reader2@example.com", passwordEncoder.encode("readerpassword"), Role.READER));
            repository.save(new User("guest", "guest@example.com", passwordEncoder.encode("guestpassword"), Role.GUEST));
        };
    }
}
