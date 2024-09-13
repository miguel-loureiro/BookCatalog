package com.bookcatalog.bookcatalog.model.dto;

import com.bookcatalog.bookcatalog.model.Book;
import com.bookcatalog.bookcatalog.model.Role;
import com.bookcatalog.bookcatalog.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

class UserDtoTest {

    private UserDto mockUserDto;
    private Book mockBook1;
    private Book mockBook2;
    private UserDto userDto1;
    private UserDto userDto2;
    private Set<Book> books1;
    private Set<Book> books2;

    @Mock
    private User mockUser;

    @Mock
    private Book mockBook;

    @Mock
    private Set<Book> mockBooks ;

    @BeforeEach
    void setUp() {

        mockBook1 = new Book("Title1", "Author1");
        mockBook2 = new Book("Title2", "Author2");

        mockUserDto = new UserDto();

        mockUser = new User();

        mockBooks = new HashSet<>();
        mockBooks.add(mockBook);

        books1 = new HashSet<>();
        books2 = new HashSet<>();
        books2.add(mockBook2);

        userDto1 = new UserDto("username", "email@example.com", Role.ADMIN);
        userDto1.setCoverImage("coverImageUrl.jpg");
        userDto1.setBooks(books1);

        userDto2 = new UserDto("username", "email@example.com", Role.ADMIN);
        userDto2.setCoverImage("coverImageUrl.jpg");
        userDto2.setBooks(books2);
    }

    @Test
    public void testDefaultConstructor() {

        UserDto userDto = new UserDto();
        assertNull(userDto.getUsername());
        assertNull(userDto.getEmail());
        assertNull(userDto.getRole());
        assertNull(userDto.getCoverImage());
        assertNull(userDto.getBooks());
    }

    @Test
    public void testConstructorWithUser() {
        // Arrange
        User user = new User("username 1", "user@email.com", "1234", Role.READER);

        UserDto userDto = new UserDto(user);

        assertEquals("username 1", userDto.getUsername());
        assertEquals("user@email.com", userDto.getEmail());
        assertEquals(Role.READER, userDto.getRole());
        assertNull(userDto.getCoverImage());
        assertEquals(new HashSet<>(), userDto.getBooks());
    }

    @Test
    public void testConstructorWithParams() {

        UserDto userDto = new UserDto("testUser", "test@example.com", Role.ADMIN);

        assertEquals("testUser", userDto.getUsername());
        assertEquals("test@example.com", userDto.getEmail());
        assertEquals(Role.ADMIN, userDto.getRole());
        assertNull(userDto.getCoverImage());
        assertNull(userDto.getBooks());
    }

    @Test
    void getUsername() {
        mockUserDto.setUsername("newname");
        assertEquals("newname", mockUserDto.getUsername());
    }

    @Test
    void getEmail() {
        mockUserDto.setEmail("newemail@email.com");
        assertEquals("newemail@email.com", mockUserDto.getEmail());
    }

    @Test
    void getRole() {
        mockUserDto.setRole(Role.ADMIN);
        assertEquals(Role.ADMIN, mockUserDto.getRole());
    }

    @Test
    void getCoverImage() {

        mockUserDto.setCoverImage("image.jpg");
        assertEquals("image.jpg", mockUserDto.getCoverImage());
    }

    @Test
    void getBooks() {

        Set<Book> mockedBooks = new HashSet<>();
        mockedBooks.add(mockBook1);
        mockedBooks.add(mockBook2);

        mockUserDto.setBooks(mockedBooks);
        assertEquals(mockedBooks, mockUserDto.getBooks());
    }

    @Test
    void setUsername() {

        mockUserDto.setUsername("othername");
        assertEquals("othername", mockUserDto.getUsername());
    }

    @Test
    void setEmail() {

        mockUserDto.setEmail("otheremail@email.com");
        assertEquals("otheremail@email.com", mockUserDto.getEmail());
    }

    @Test
    void setRole() {

        mockUserDto.setRole(Role.ADMIN);
        assertEquals(Role.ADMIN, mockUserDto.getRole());
    }

    @Test
    void setCoverImage() {

        mockUserDto.setCoverImage("image.jpg");
        assertEquals("image.jpg", mockUserDto.getCoverImage());
    }

    @Test
    void setBooks() {

        Set<Book> mockedBooks = new HashSet<>();
        mockedBooks.add(mockBook1);
        mockedBooks.add(mockBook2);

        mockUserDto.setBooks(mockedBooks);
        assertEquals(mockedBooks, mockUserDto.getBooks());
    }

    @Test
    public void testEqualsAndHashCode() {
        UserDto userDto1 = new UserDto("testUser", "test@example.com", Role.READER);
        userDto1.setCoverImage("coverImageUrl.jpg");
        userDto1.setBooks(mockBooks);

        UserDto userDto2 = new UserDto("testUser", "test@example.com", Role.READER);
        userDto2.setCoverImage("coverImageUrl.jpg");
        userDto2.setBooks(mockBooks);

        UserDto userDto3 = new UserDto("differentUser", "different@example.com", Role.READER);

        assertEquals(userDto1, userDto2);
        assertNotEquals(userDto1, userDto3);

        assertEquals(userDto1.hashCode(), userDto2.hashCode());
        assertNotEquals(userDto1.hashCode(), userDto3.hashCode());
    }

    @Test
    public void testEquals_SameInstance() {
        // Assert
        assertEquals(userDto1, userDto1);
    }

    @Test
    public void testEquals_NullObject() {
        // Assert
        assertNotEquals(null, userDto1);
    }

    @Test
    public void testEquals_DifferentClass() {
        // Assert
        assertNotEquals("Some String", userDto1);
    }

    @Test
    public void testEquals_DifferentUsername() {
        // Different usernames should return false
        userDto2.setUsername("differentUsername");
        assertNotEquals(userDto1, userDto2);
    }

    @Test
    public void testEquals_DifferentEmail() {
        // Different emails should return false
        userDto2.setEmail("different@example.com");
        assertNotEquals(userDto1, userDto2);
    }

    @Test
    public void testEquals_DifferentRole() {
        // Different roles should return false
        userDto2.setRole(Role.SUPER);
        assertNotEquals(userDto1, userDto2);
    }

    @Test
    public void testEquals_DifferentCoverImage() {
        // Different cover images should return false
        userDto2.setCoverImage("differentCoverImage.jpg");
        assertNotEquals(userDto1, userDto2);
    }

    @Test
    public void testEquals_DifferentBooks() {
        // Different sets of books should return false
        userDto2.setBooks(books2);
        assertNotEquals(userDto1, userDto2);
    }

    @Test
    public void testEquals_AllFieldsEqual() {
        // Arrange
        userDto1 = new UserDto("username", "email@example.com", Role.ADMIN);
        userDto1.setCoverImage("coverImageUrl.jpg");
        userDto1.setBooks(books1);

        userDto2 = new UserDto("username", "email@example.com", Role.ADMIN);
        userDto2.setCoverImage("coverImageUrl.jpg");
        userDto2.setBooks(books1);

        // Assert
        assertEquals(userDto1, userDto2);
    }
}