package com.bookcatalog.bookcatalog.model.dto;

import com.bookcatalog.bookcatalog.model.Book;
import com.bookcatalog.bookcatalog.model.Role;
import com.bookcatalog.bookcatalog.model.User;
import lombok.Getter;
import lombok.Setter;

import java.util.Objects;
import java.util.Set;

@Setter
@Getter
public class UserDto {

    private String username;
    private String email;
    private Role role;
    private String coverImage;
    private Set<Book> books;

    public UserDto() {}

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

    @Override
    public boolean equals(Object o) {

        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UserDto userDto = (UserDto) o;
        return Objects.equals(username, userDto.username) &&
                Objects.equals(email, userDto.email) &&
                role == userDto.role &&
                Objects.equals(coverImage, userDto.coverImage) &&
                Objects.equals(books, userDto.books);
    }

    @Override
    public int hashCode() {

        return Objects.hash(username, email, role, coverImage, books);
    }
}
