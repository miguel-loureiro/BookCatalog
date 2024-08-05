package com.bookcatalog.bookcatalog.model;

import static org.junit.Assert.*;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.bookcatalog.bookcatalog.helpers.DateHelper;

public class BookTest {

    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("MM/yyyy");
    private Book book;
    private Set<User> users;

    @BeforeEach
    public void setUp() {
        users = new HashSet<>();
        users.add(new User("username1", "email1@example.com", "password1", Role.READER));
        users.add(new User("username2", "email2@example.com", "password2", Role.READER));

        book = new Book(1, "Title", "Author", "9781234567890", "19.99", new Date(), "cover.jpg", users);
    }

    @Test
    public void testDefaultConstructor() {

        Book emptyBook = new Book();
        assertNotNull(emptyBook );
        Assertions.assertNull(emptyBook.getTitle());
        Assertions.assertNull(emptyBook.getAuthor());
        Assertions.assertNull(emptyBook.getPrice());
        Assertions.assertNull(emptyBook.getPublishDate());
        Assertions.assertNull(emptyBook.getCoverImage());
    }

    @Test
    public void testParameterizedConstructor() throws ParseException {

        Assertions.assertNotNull(book.getId());
        Assertions.assertEquals("Title", book.getTitle());
        Assertions.assertEquals("Author", book.getAuthor());
        Assertions.assertEquals("9781234567890", book.getIsbn());
        Assertions.assertEquals("19.99", book.getPrice());
        Assertions.assertEquals("cover.jpg", book.getCoverImage());
        Assertions.assertEquals(users, book.getUsers());
    }

    @Test
    public void testGettersAndSetters() {

        Book book = new Book();
        book.setId(2);
        book.setTitle("New Title");
        book.setAuthor("New Author");
        book.setIsbn("9789876543210");
        book.setPrice("29.99");
        book.setCoverImage("newcover.jpg");

        Assertions.assertNotNull(book.getId());
        Assertions.assertEquals(book.getId(),2);
        Assertions.assertEquals("New Title", book.getTitle());
        Assertions.assertEquals("New Author", book.getAuthor());
        Assertions.assertEquals("9789876543210", book.getIsbn());
        Assertions.assertEquals("29.99", book.getPrice());
        Assertions.assertEquals("newcover.jpg", book.getCoverImage());
    }

    /*
    @Test
    public void testGetUsersShort() {
        var userShortDtos = book.getUsersShort();
        Assertions.assertEquals(2, userShortDtos.size());
        Assertions.assertTrue(userShortDtos.stream().anyMatch(dto -> dto.getUsername().equals("username1")));
        Assertions.assertTrue(userShortDtos.stream().anyMatch(dto -> dto.getUsername().equals("username1")));
        Assertions.assertTrue(userShortDtos.stream().anyMatch(dto -> dto.getUsername().equals("username2")));
    }
*/
    @Test
    public void testSetPublishDate() throws IOException {
        Book book = new Book();
        book.setPublishDate("06/2021");

        assertNotNull(book.getPublishDate());
        Assertions.assertEquals("06/2021", book.getPublishDate());
    }

    @Test
    public void testSerializeDate() {
        Date date = new Date();
        String serializedDate = DateHelper.serialize(date);

        assertNotNull(serializedDate);
        Assertions.assertFalse(serializedDate.isEmpty());
    }

    @Test
    public void testDeserializeDate() throws IOException {
        String dateString = "06/2021";
        Date date = DateHelper.deserialize(dateString);

        assertNotNull(date);
    }

    @Test
    public void testSetInvalidPublishDate() {
        Book book = new Book();
        String invalidDate = "invalid date";
        IOException exception = assertThrows(IOException.class, () -> book.setPublishDate(invalidDate));
        Assertions.assertEquals("Error parsing date", exception.getMessage());
    }
}