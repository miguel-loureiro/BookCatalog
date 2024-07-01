package com.bookcatalog.bookcatalog.model;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class UserTest {

    private User user;
    private Set<Book> books;

    @BeforeEach
    public void setUp() {

        books = new HashSet<>();
        books.add(new Book(1, "Title 1", "Author 1", "9781234567890", "19.99", new Date(), "cover1.jpg", new HashSet<>()));
        books.add(new Book(2, "Title 2", "Author 2", "9789876543210", "29.99", new Date(), "cover2.jpg", new HashSet<>()));

        user = new User("username", "email@example.com", "password", Role.READER);
        user.setId(1);
        user.setBooks(books);
    }

    @Test
    public void testConstructor() {
        User user = new User("username", "email@example.com", "password", Role.READER);

        assertEquals("username", user.getUsername());
        assertEquals("email@example.com", user.getEmail());
        assertEquals("password", user.getPassword());
        assertEquals(Role.READER, user.getRole());
    }

    @Test
    public void testGettersAndSetters() {

        User user = new User();
        user.setId(2);
        user.setUsername("newusername");
        user.setEmail("newemail@example.com");
        user.setPassword("newpassword");
        user.setRole(Role.ADMIN);
        user.setBooks(books);

        assertNotNull(user.getId());
        assertEquals(2, user.getId());
        assertEquals("newusername", user.getUsername());
        assertEquals("newemail@example.com", user.getEmail());
        assertEquals("newpassword", user.getPassword());
        Assertions.assertEquals(user.getRole(), Role.ADMIN);
        assertEquals(2, user.getBooks().size());
    }

    @Test
    public void testGetBooks() {

        var bookShortDtos = user.getBooks();
        assertEquals(2, bookShortDtos.size());
        assertTrue(bookShortDtos.stream().anyMatch(dto -> dto.getTitle().equals("Title 1")));
        assertTrue(bookShortDtos.stream().anyMatch(dto -> dto.getTitle().equals("Title 2")));
    }

    @Test
    public void testGetAuthorities() {
        User adminUser = new User("adminuser", "admin@example.com", "password", Role.ADMIN);
        Collection<? extends GrantedAuthority> authorities = adminUser.getAuthorities();

        assertNotNull(authorities);
        assertEquals(1, authorities.size());
        assertTrue(authorities.contains(new SimpleGrantedAuthority("ROLE_ADMIN")));
    }

    @Test
    public void testIsAccountNonExpired() {
        assertTrue(user.isAccountNonExpired());
    }

    @Test
    public void testIsAccountNonLocked() {
        assertTrue(user.isAccountNonLocked());
    }

    @Test
    public void testIsCredentialsNonExpired() {
        assertTrue(user.isCredentialsNonExpired());
    }

    @Test
    public void testIsEnabled() {
        assertTrue(user.isEnabled());
    }
}