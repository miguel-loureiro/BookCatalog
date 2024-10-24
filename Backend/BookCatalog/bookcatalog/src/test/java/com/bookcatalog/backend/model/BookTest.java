package com.newbookcatalog.newbookcatalog.model;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.newbookcatalog.newbookcatalog.helpers.DateHelper;

import static org.junit.jupiter.api.Assertions.*;

public class BookTest {

    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("MM/yyyy");
    private Book book;
    private Set<User> users;

    @BeforeEach
    public void setUp() {

        users = new HashSet<>();
        users.add(new User("username1", "email1@example.com", "password1", Role.READER));
        users.add(new User("username2", "email2@example.com", "password2", Role.READER));

        book = new Book("Title", "Author", "9781234567890", "19.99", new Date(), "cover.jpg", users);
    }

    @Test
    public void testDefaultConstructor() {

        Book emptyBook = new Book();

        assertNotNull(emptyBook );
        assertNull(emptyBook.getTitle());
        assertNull(emptyBook.getAuthor());
        assertNull(emptyBook.getPrice());
        assertNull(emptyBook.getPublishDate());
        assertNull(emptyBook.getCoverImageUrl());
    }

    @Test
    public void testParameterizedConstructor() throws ParseException {

        assertEquals("Title", book.getTitle());
        assertEquals("Author", book.getAuthor());
        assertEquals("9781234567890", book.getIsbn());
        assertEquals("19.99", book.getPrice());
        assertEquals("cover.jpg", book.getCoverImageUrl());
        assertEquals(users, book.getUsers());
    }

    @Test
    public void testGettersAndSetters() {

        Book book = new Book();
        book.setId(2);
        book.setTitle("New Title");
        book.setAuthor("New Author");
        book.setIsbn("9789876543210");
        book.setPrice("29.99");
        book.setCoverImageUrl("newcover.jpg");

        assertNotNull(book.getId());
        assertEquals(book.getId(),2);
        assertEquals("New Title", book.getTitle());
        assertEquals("New Author", book.getAuthor());
        assertEquals("9789876543210", book.getIsbn());
        assertEquals("29.99", book.getPrice());
        assertEquals("newcover.jpg", book.getCoverImageUrl());
    }

    @Test
    public void testSetPublishDate() throws IOException {
        Book book = new Book();
        book.setPublishDate("06/2021");

        assertNotNull(book.getPublishDate());
        assertEquals("06/2021", book.getPublishDate());
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
        assertEquals("Error parsing date", exception.getMessage());
    }

    @Test
    public void testEquals_SameObject() {
        // Arrange
        Book book = new Book("Title 1", "Author 1", "1234567890", "10.00", new Date(), "url1");

        // Act and Assert
        assertEquals(book, book, "A book should be equal to itself");
    }

    @Test
    public void testEquals_SameId() {
        // Arrange
        Book book1 = new Book(1, "Title 1", "Author 1");
        Book book2 = new Book(1, "Title 2", "Author 2");

        // Act and Assert
        assertEquals(book1, book2, "Books with the same id should be equal");
    }

    @Test
    public void testEquals_SameTitle() {
        // Arrange
        Book book1 = new Book("Title 1", "Author 1", "1234567890", "10.00", new Date(), "url1");
        Book book2 = new Book("Title 1", "Author 2", "0987654321", "15.00", new Date(), "url2");

        // Act and Assert
        assertEquals(book1, book2, "Books with the same title should be equal");
    }

    @Test
    public void testEquals_SameIsbn() {
        // Arrange
        Book book1 = new Book("Title 1", "Author 1", "1234567890", "10.00", new Date(), "url1");
        Book book2 = new Book("Title 2", "Author 2", "1234567890", "15.00", new Date(), "url2");

        // Act and Assert
        assertEquals(book1, book2, "Books with the same ISBN should be equal");
    }

    @Test
    public void testNotEquals_DifferentClass() {
        // Arrange
        Book book = new Book("Title 1", "Author 1", "1234567890", "10.00", new Date(), "url1");
        String nonBookObject = "Not a Book";

        // Act and Assert
        assertNotEquals(String.valueOf(book), nonBookObject, "A book should not be equal to an object of a different class");
    }

    @Test
    public void testNotEquals_NullObject() {
        // Arrange
        Book book = new Book("Title 1", "Author 1", "1234567890", "10.00", new Date(), "url1");

        // Act and Assert
        assertNotEquals(String.valueOf(book), null, "A book should not be equal to null");
    }
}