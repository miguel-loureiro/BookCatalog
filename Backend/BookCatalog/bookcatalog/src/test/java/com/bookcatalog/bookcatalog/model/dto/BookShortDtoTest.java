package com.bookcatalog.bookcatalog.model.dto;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class BookShortDtoTest {

    @Test
    public void testObjectCreationAndGettersSetters() {
        // Create an instance of BookShortDto
        BookShortDto bookShortDto = new BookShortDto();

        // Set values using setters
        bookShortDto.setTitle("Test Title");
        bookShortDto.setAuthor("Test Author");
        bookShortDto.setIsbn("123456789X");
        bookShortDto.setPublishDate("06/2023");
        bookShortDto.setPrice("19.99");

        // Validate using getters
        assertEquals("Test Title", bookShortDto.getTitle());
        assertEquals("Test Author", bookShortDto.getAuthor());
        assertEquals("123456789X", bookShortDto.getIsbn());
        assertEquals("06/2023", bookShortDto.getPublishDate());
        assertEquals("19.99", bookShortDto.getPrice());
    }

    @Test
    public void testDefaultConstructor() {
        // Create an instance of BookShortDto using default constructor
        BookShortDto bookShortDto = new BookShortDto();

        // Validate that all fields are initially null
        assertNull(bookShortDto.getTitle());
        assertNull(bookShortDto.getAuthor());
        assertNull(bookShortDto.getIsbn());
        assertNull(bookShortDto.getPublishDate());
        assertNull(bookShortDto.getPrice());
    }

    @Test
    public void testConstructorWithParameters() {
        // Create an instance of BookShortDto using parameterized constructor
        BookShortDto bookShortDto = new BookShortDto("Test Title", "Test Author", "123456789X", "06/2023", "19.99");

        // Validate using getters
        assertEquals("Test Title", bookShortDto.getTitle());
        assertEquals("Test Author", bookShortDto.getAuthor());
        assertEquals("123456789X", bookShortDto.getIsbn());
        assertEquals("06/2023", bookShortDto.getPublishDate());
        assertEquals("19.99", bookShortDto.getPrice());
    }
}