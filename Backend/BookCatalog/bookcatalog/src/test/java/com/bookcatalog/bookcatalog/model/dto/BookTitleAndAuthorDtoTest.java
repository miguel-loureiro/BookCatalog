package com.bookcatalog.bookcatalog.model.dto;

import com.bookcatalog.bookcatalog.model.Role;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class BookTitleAndAuthorDtoTest {

    @Test
    public void testObjectCreationAndGettersSetters() {
        // Create an instance of BookTitleAndAuthorDto
        BookTitleAndAuthorDto bookTitleAndAuthorDto = new BookTitleAndAuthorDto("newtitle1", "author1");

        // Validate using getters
        assertEquals("newtitle1", bookTitleAndAuthorDto.getTitle());
        assertEquals("author1", bookTitleAndAuthorDto.getAuthor());

        // Set new values using setters
        bookTitleAndAuthorDto.setTitle("newtitle2");
        bookTitleAndAuthorDto.setAuthor("author2");

        // Validate using getters
        assertEquals("newtitle2", bookTitleAndAuthorDto.getTitle());
        assertEquals("author2", bookTitleAndAuthorDto.getAuthor());
    }

    @Test
    public void testDefaultConstructor() {
        // Test the no-args constructor
        BookTitleAndAuthorDto bookTitleAndAuthorDto = new BookTitleAndAuthorDto();
        assertNotNull(bookTitleAndAuthorDto);

        // Set values using setters
        bookTitleAndAuthorDto.setTitle("The Pragmatic Programmer");
        bookTitleAndAuthorDto.setAuthor("Andrew Hunt");

        // Validate using getters
        assertEquals("The Pragmatic Programmer", bookTitleAndAuthorDto.getTitle());
        assertEquals("Andrew Hunt", bookTitleAndAuthorDto.getAuthor());
    }
}