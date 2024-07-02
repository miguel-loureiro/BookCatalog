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
    private BookShortDto mockBook1;
    private BookShortDto mockBook2;

    @BeforeEach
    void setUp() {

        mockBook1 = new BookShortDto("Title1", "Author1", "ISBN1", "01/2020", "9.99");
        mockBook2 = new BookShortDto("Title2", "Author2", "ISBN2", "02/2021", "19.99");

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
        List<BookShortDto> mockedBooks = Arrays.asList(mockBook1, mockBook2);
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
        List<BookShortDto> mockedBooks = Arrays.asList(mockBook1, mockBook2);
        mockUserDto.setBooks(mockedBooks);
        assertEquals(mockedBooks, mockUserDto.getBooks());
    }
}