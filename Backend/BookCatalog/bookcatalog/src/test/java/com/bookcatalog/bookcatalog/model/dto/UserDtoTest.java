package com.bookcatalog.bookcatalog.model.dto;

import com.bookcatalog.bookcatalog.model.Book;
import com.bookcatalog.bookcatalog.model.Role;
import com.bookcatalog.bookcatalog.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

class UserDtoTest {

    private UserDto mockUserDto;
    private Book mockBook1;
    private Book mockBook2;


    @BeforeEach
    void setUp() {

        mockBook1 = new Book("Title1", "Author1");
        mockBook2 = new Book("Title2", "Author2");

        mockUserDto = new UserDto();
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
    void setBooks() {

        Set<Book> mockedBooks = new HashSet<>();
        mockedBooks.add(mockBook1);
        mockedBooks.add(mockBook2);

        mockUserDto.setBooks(mockedBooks);
        assertEquals(mockedBooks, mockUserDto.getBooks());
    }
}