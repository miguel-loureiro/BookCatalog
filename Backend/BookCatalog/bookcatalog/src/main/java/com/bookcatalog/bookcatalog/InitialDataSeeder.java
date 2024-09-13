package com.bookcatalog.bookcatalog;

import com.bookcatalog.bookcatalog.helpers.DateHelper;
import com.bookcatalog.bookcatalog.model.Book;
import com.bookcatalog.bookcatalog.model.Role;
import com.bookcatalog.bookcatalog.model.User;
import com.bookcatalog.bookcatalog.model.dto.RegisterUserDto;
import com.bookcatalog.bookcatalog.repository.BookRepository;
import com.bookcatalog.bookcatalog.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

@Component
public class InitialDataSeeder implements ApplicationListener<ContextRefreshedEvent> {

    private final UserRepository userRepository;
    private final BookRepository bookRepository;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public InitialDataSeeder(UserRepository userRepository, BookRepository bookRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.bookRepository = bookRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {
        this.createSuperUser();
        List<User> users = this.seedUsers();
        List<Book> books = this.createBooks();
        this.assignBooksToUsers(users, books);

    }

    private void createSuperUser() {

        String superUsername = "superuser";
        String superEmail = "superuser@email.com";

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

    //private void seedUsers() {
    private List<User> seedUsers() {

        List<User> allUsers = new ArrayList<>();

        Random random = new Random();

        // Create 9 ADMIN users
        for (int i = 1; i <= 9; i++) {
            String adminUsername = "adminuser" + i;
            String adminEmail = "adminuser" + i + "@email.com";

            if (userRepository.findByUsername(adminUsername).isPresent()) {
                System.out.println("Admin user " + adminUsername + " already exists");
                continue;
            }

            RegisterUserDto userDto = new RegisterUserDto();
            userDto.setUsername(adminUsername);
            userDto.setEmail(adminEmail);
            userDto.setPassword("adminpassword" + i);
            userDto.setRole(Role.ADMIN);

            var newAdminUser = new User();
            newAdminUser.setUsername(userDto.getUsername());
            newAdminUser.setEmail(userDto.getEmail());
            newAdminUser.setPassword(passwordEncoder.encode(userDto.getPassword()));
            newAdminUser.setRole(userDto.getRole());

            allUsers.add(userRepository.save(newAdminUser));
        }

        // Create 90 READER users
        for (int i = 1; i <= 89; i++) {
            String readerUsername = "readeruser" + i;
            String readerEmail = "readeruser" + i + "@email.com";

            // Check if the adminuser already exists
            if (userRepository.findByUsername(readerUsername).isPresent()) {
                System.out.println("Reader user " + readerUsername + " already exists");
                continue; // Skip to the next iteration
            }

            RegisterUserDto userDto = new RegisterUserDto();
            userDto.setUsername(readerUsername);
            userDto.setEmail(readerEmail);
            userDto.setPassword("readerpassword" + i);
            userDto.setRole(Role.READER);

            var newReaderUser = new User();
            newReaderUser.setUsername(userDto.getUsername());
            newReaderUser.setEmail(userDto.getEmail());
            newReaderUser.setPassword(passwordEncoder.encode(userDto.getPassword()));
            newReaderUser.setRole(userDto.getRole());

            allUsers.add(userRepository.save(newReaderUser));
        }

        return allUsers;
    }

    private List<Book> createBooks() {
        List<Book> books = new ArrayList<>();

        for (int i = 1; i <= 100; i++) {
            Book book = new Book("Title " + i, "Author " + i, generateRandomISBN13(), "Price " + i, new Date(), "coverImageUrl" + i + ".jpg", null);
            books.add(book);
        }
        return bookRepository.saveAll(books);
    }

    private static String generateRandomISBN13() {
        Random random = new Random();
        StringBuilder isbn = new StringBuilder("978"); // ISBN-13 typically starts with 978 or 979

        // Generate first 9 digits randomly
        for (int i = 0; i < 9; i++) {
            isbn.append(random.nextInt(10));
        }

        // Calculate the check digit
        int checkDigit = calculateCheckDigit(isbn.toString());
        isbn.append(checkDigit);

        return isbn.toString();
    }

    private static int calculateCheckDigit(String isbn) {
        int sum = 0;
        for (int i = 0; i < isbn.length(); i++) {
            int digit = Character.getNumericValue(isbn.charAt(i));
            sum += (i % 2 == 0) ? digit : digit * 3;
        }
        int mod = sum % 10;
        return (mod == 0) ? 0 : 10 - mod;
    }

    private void assignBooksToUsers(List<User> users, List<Book> books) {
        Random random = new Random();

        for (User user : users) {
            int bookCount = random.nextInt(19) + 2; // Number of books to assign (between 2 and 20)
            Set<Book> assignedBooks = new HashSet<>();

            for (int i = 0; i < bookCount; i++) {
                Book randomBook = books.get(random.nextInt(books.size()));
                assignedBooks.add(randomBook);
            }

            user.setBooks(assignedBooks);
            userRepository.save(user);
        }
    }
}


