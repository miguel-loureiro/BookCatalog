package com.bookcatalog.bookcatalog.model.dto;

import com.bookcatalog.bookcatalog.model.Book;
import com.bookcatalog.bookcatalog.model.Role;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

public class UserDto {

    @Getter
    @Setter
    private String username;
    @Getter
    @Setter
    private String email;
    @Getter
    @Setter
    private Role role;
    @Getter
    @Setter
    private List<Book> books;
}
