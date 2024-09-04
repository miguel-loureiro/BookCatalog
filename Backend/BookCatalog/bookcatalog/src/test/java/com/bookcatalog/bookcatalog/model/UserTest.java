package com.bookcatalog.bookcatalog.model;

import com.bookcatalog.bookcatalog.model.dto.UserDto;
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
import static org.mockito.Mockito.when;

class UserTest {

    private User user;
    private Set<Book> books;

    @BeforeEach
    public void setUp() {

        books = new HashSet<>();
        books.add(new Book("Title 1", "Author 1", "9781234567890", "19.99", new Date(), "cover1.jpg", new HashSet<>()));
        books.add(new Book("Title 2", "Author 2", "9789876543210", "29.99", new Date(), "cover2.jpg", new HashSet<>()));

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
    void testConstructor_UserDtoWithNullBooks_ShouldInitializeEmptyBooks() {
        // Arrange
        User user = new User("username", "email@example.com", "password", Role.READER);

        assertEquals("username", user.getUsername());
        assertEquals("email@example.com", user.getEmail());
        assertEquals("password", user.getPassword());
        assertEquals(Role.READER, user.getRole());
        assertEquals(new HashSet<>(), user.getBooks());
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
        user.setCoverImage("newimage");

        assertNotNull(user.getId());
        assertEquals(2, user.getId());
        assertEquals("newusername", user.getUsername());
        assertEquals("newemail@example.com", user.getEmail());
        assertEquals("newpassword", user.getPassword());
        assertEquals("newimage", user.getCoverImage());
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
    void testGetBooks_EmptySet() {
        // Arrange
        User user = new User();
        user.setBooks(new HashSet<>());

        // Act
        Set<Book> books = user.getBooks();

        // Assert
        assertNotNull(books, "Books set should not be null");
        assertTrue(books.isEmpty(), "Books set should be empty when no books are associated with the user");
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

    @Test
    void testGetBooks_WhenBooksIsNull_ShouldInitializeNewHashSet() {
        // Arrange
        User user = new User();
        user.setBooks(null);

        // Act
        Set<Book> books = user.getBooks();

        // Assert
        assertNotNull(books, "Books should not be null.");
        assertTrue(books.isEmpty(), "Books should be initialized as an empty HashSet.");
    }

    @Test
    void testGetBooks_WhenBooksIsNotNull_ShouldReturnBooksSet() {
        // Arrange
        User user = new User();
        Set<Book> expectedBooks = new HashSet<>();
        expectedBooks.add(new Book());
        user.setBooks(expectedBooks);

        // Act
        Set<Book> actualBooks = user.getBooks();

        // Assert
        assertNotNull(actualBooks, "Books should not be null.");
        assertEquals(expectedBooks, actualBooks, "Books should return the correct set.");
    }

    @Test
    void testEquals_NullObject_ShouldReturnFalse() {
        // Arrange
        User user1 = new User("username", "email@example.com", "password", Role.READER);

        // Act and Assert
        assertNotEquals(user1, null, "A user should not be equal to null.");
    }

    @Test
    void testEquals_DifferentUsernames_ShouldReturnFalse() {
        // Arrange
        User user1 = new User("username1", "email@example.com", "password", Role.READER);
        User user2 = new User("username2", "email@example.com", "password", Role.READER);

        // Act and Assert
        assertNotEquals(user1, user2, "Users with different usernames should not be equal.");
    }

    @Test
    void testEquals_DifferentEmails_ShouldReturnFalse() {
        // Arrange
        User user1 = new User("username", "email1@example.com", "password", Role.READER);
        User user2 = new User("username", "email2@example.com", "password", Role.READER);

        // Act and Assert
        assertNotEquals(user1, user2, "Users with different emails should not be equal.");
    }

    @Test
    void testEquals_SameUsernameAndEmail_ShouldReturnTrue() {
        // Arrange
        User user1 = new User("username", "email@example.com", "password", Role.READER);
        User user2 = new User("username", "email@example.com", "differentPassword", Role.READER);

        // Act and Assert
        assertEquals(user1, user2, "Users with the same username and email should be equal.");
    }

    @Test
    void testHashCode_ConsistentWithEquals() {
        // Arrange
        User user1 = new User("username", "email@example.com", "password", Role.READER);
        User user2 = new User("username", "email@example.com", "password", Role.READER);

        // Act and Assert
        assertEquals(user1.hashCode(), user2.hashCode(), "Equal users should have the same hash code.");
    }
}