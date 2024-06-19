package com.bookcatalog.bookcatalog.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThrows;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import com.bookcatalog.bookcatalog.helpers.DateHelper;

public class BookTest {

    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("MM/yyyy");

    @Test
    public void testDefaultConstructor() {
        Book book = new Book();
        assertNotNull(book);
        assertNull(book.getTitle());
        assertNull(book.getAuthor());
        assertNull(book.getPrice());
        assertNull(book.getPublishDate());
        assertNull(book.getCoverImage());
    }

    @Test
    public void testParameterizedConstructor() throws ParseException {

        String title = "Effective Java";
        String author = "Joshua Bloch";
        String price = "45.00";
        Date publishDate = dateFormat.parse("05/2008");
        String coverImage = "cover.jpg";
        User user = new User("abc" ,"abc@example.com", "1234", Role.READER);

        Book book = new Book();

        book.setTitle(title);

        Assertions.assertEquals(title, book.getTitle());
        Assertions.assertEquals(author, book.getAuthor());
        Assertions.assertEquals(price, book.getPrice());
        Assertions.assertEquals("05/2008", book.getPublishDate());
        Assertions.assertEquals(coverImage, book.getCoverImage());
        Assertions.assertEquals(user, book.getUser());
    }

    @Test
    public void testSetAndGetId() {
          // Arrange
          Book book = new Book();
          Integer expectedId = 1;
  
          // Act
          book.setId(expectedId);
          Integer actualId = book.getId();
  
          // Assert
          assertEquals(expectedId, actualId);
    }

    @Test
    public void testSetAndGetTitle() {
        Book book = new Book();
        String title = "Effective Java";
        book.setTitle(title);
        assertEquals(title, book.getTitle());
    }

    @Test
    public void testSetAndGetAuthor() {
        Book book = new Book();
        String author = "Joshua Bloch";
        book.setAuthor(author);
        assertEquals(author, book.getAuthor());
    }

    @Test
    public void testSetAndGetPrice() {
        Book book = new Book();
        String price = "45.00";
        book.setPrice(price);
        assertEquals(price, book.getPrice());
    }

    @Test
    public void testSetAndGetPublishDate() throws IOException, ParseException {
        Book book = new Book();
        String dateString = "05/2008";
        Date date = dateFormat.parse(dateString);
        book.setPublishDate(dateString);
        assertEquals(dateString, book.getPublishDate());
        assertEquals(date, DateHelper.deserialize(book.getPublishDate()));
    }

    @Test
    public void testSetAndGetCoverImage() {
        Book book = new Book();
        String coverImage = "cover.jpg";
        book.setCoverImage(coverImage);
        assertEquals(coverImage, book.getCoverImage());
    }

    @Test
    public void testSetInvalidPublishDate() {
        Book book = new Book();
        String invalidDate = "invalid date";
        IOException exception = assertThrows(IOException.class, () -> book.setPublishDate(invalidDate));
        assertEquals("Error parsing date", exception.getMessage());
    }
}