package com.bookcatalog.bookcatalog.model.dto;

import com.bookcatalog.bookcatalog.model.Book;
import com.bookcatalog.bookcatalog.model.Role;
import com.bookcatalog.bookcatalog.model.User;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.Set;

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
    private String coverImage;
    @Getter
    @Setter
    private Set<Book> books;

    public UserDto() {

    }

    public UserDto(User user) {
        this.username = user.getUsername();
        this.email = user.getEmail();
        this.role = user.getRole();
        this.coverImage = user.getCoverImage();
        this.books = user.getBooks();
    }

    public UserDto(String username, String email, Role role) {
        this.username = username;
        this.email = email;
        this.role = role;
    }
}
