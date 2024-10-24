package com.newbookcatalog.newbookcatalog.model.dto;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class BookTitleAndAuthorDtoTest {

    @Test
    public void testObjectCreationAndGettersSetters() {
        // Arrange
        BookTitleAndAuthorDto bookTitleAndAuthorDto = new BookTitleAndAuthorDto("newtitle1", "author1");

        // Assert
        assertEquals("newtitle1", bookTitleAndAuthorDto.getTitle());
        assertEquals("author1", bookTitleAndAuthorDto.getAuthor());

        // Act
        bookTitleAndAuthorDto.setTitle("newtitle2");
        bookTitleAndAuthorDto.setAuthor("author2");

        // Assert
        assertEquals("newtitle2", bookTitleAndAuthorDto.getTitle());
        assertEquals("author2", bookTitleAndAuthorDto.getAuthor());
    }

    @Test
    public void testDefaultConstructor() {
        // Arrange
        BookTitleAndAuthorDto bookTitleAndAuthorDto = new BookTitleAndAuthorDto();
        assertNotNull(bookTitleAndAuthorDto);

        // Act
        bookTitleAndAuthorDto.setTitle("The Pragmatic Programmer");
        bookTitleAndAuthorDto.setAuthor("Andrew Hunt");

        // Assert
        assertEquals("The Pragmatic Programmer", bookTitleAndAuthorDto.getTitle());
        assertEquals("Andrew Hunt", bookTitleAndAuthorDto.getAuthor());
    }

    @Test
    public void testEquals_SameObject() {
        // Arrange
        BookTitleAndAuthorDto book1 = new BookTitleAndAuthorDto("Title1", "Author1");

        // Act and Assert
        assertEquals(book1, book1);
    }

    @Test
    public void testEquals_EqualObjects() {
        // Arrange
        BookTitleAndAuthorDto book1 = new BookTitleAndAuthorDto("Title1", "Author1");
        BookTitleAndAuthorDto book2 = new BookTitleAndAuthorDto("Title1", "Author1");

        // Act and Assert
        assertEquals(book1, book2);  // Two objects with the same fields should be equal
        assertEquals(book1.hashCode(), book2.hashCode());  // Hash codes should be equal
    }

    @Test
    public void testEquals_DifferentTitle() {
        // Arrange
        BookTitleAndAuthorDto book1 = new BookTitleAndAuthorDto("Title1", "Author1");
        BookTitleAndAuthorDto book2 = new BookTitleAndAuthorDto("Title2", "Author1");

        // Act and Assert
        assertNotEquals(book1, book2);
    }

    @Test
    public void testEquals_DifferentAuthor() {
        // Arrange
        BookTitleAndAuthorDto book1 = new BookTitleAndAuthorDto("Title1", "Author1");
        BookTitleAndAuthorDto book2 = new BookTitleAndAuthorDto("Title1", "Author2");

        // Act and Assert
        assertNotEquals(book1, book2);
    }

    @Test
    public void testEquals_DifferentObjectType() {
        // Arrange
        BookTitleAndAuthorDto book = new BookTitleAndAuthorDto("Title1", "Author1");
        String differentObject = "Not a Book";

        // Act and Assert
        assertNotEquals(book, differentObject);
    }

    @Test
    public void testEquals_NullObject() {
        // Arrange
        BookTitleAndAuthorDto book = new BookTitleAndAuthorDto("Title1", "Author1");

        // Act and Assert
        assertNotEquals(book, null);
    }
}