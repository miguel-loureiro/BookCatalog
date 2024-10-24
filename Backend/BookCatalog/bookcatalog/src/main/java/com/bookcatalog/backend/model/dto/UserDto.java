package com.newbookcatalog.newbookcatalog.model.dto;

import com.newbookcatalog.newbookcatalog.model.Role;
import com.newbookcatalog.newbookcatalog.model.User;
import lombok.Getter;
import lombok.Setter;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Setter
@Getter
public class UserDto {

    private String username;
    private String email;
    private Role role;
    private String coverImage;
    private Set<BookTitleAndAuthorDto> books;

    public UserDto() {}

    public UserDto(User user) {

        this.username = user.getUsername();
        this.email = user.getEmail();
        this.role = user.getRole();
        this.coverImage = user.getCoverImage();
        this.books = user.getBooks() != null
                ? user.getBooks().stream()
                .map(book -> new BookTitleAndAuthorDto(book.getTitle(), book.getAuthor()))
                .collect(Collectors.toSet())
                : new HashSet<>();
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
