package com.bookcatalog.backend.model;

import java.util.*;
import java.util.stream.Collectors;

import com.bookcatalog.backend.model.dto.UserDto;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

@Setter
@Entity
@Table(name = "users")
public class User implements UserDetails {

    @Getter
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(nullable = false)
    private Integer id;

    @Getter
    @Column(unique = true, nullable = false)
    private String username;

    @Getter
    @Column(unique = true, length = 100, nullable = false)
    private String email;

    @Getter
    @Column(nullable = false)
    private String password;

    @Getter
    @Enumerated(EnumType.STRING)
    private Role role;

    @Setter
    @Getter
    private String coverImage;

    @Version
    @Getter
    private Long version;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name="user_books", joinColumns = @JoinColumn(name= "user_id"), inverseJoinColumns = @JoinColumn(name= "book_id"))
    private Set<Book> books = new HashSet<>();

    public Set<Book> getBooks() {

        if(books == null) {

            books = new HashSet<>();
        }
        return books;
    }

    // No-argument constructor
    public User() {
    }

    // Constructor with all fields except id and books
    public User(String username, String email, String password, Role role) {
        this.username = username;
        this.email = email;
        this.password = password;
        this.role = role;
    }

    // Constructor that accepts a UserDto
    public User(UserDto userDto) {
        this.username = userDto.getUsername();
        this.email = userDto.getEmail();
        this.role = userDto.getRole();
        this.coverImage = userDto.getCoverImage() != null ? userDto.getCoverImage() : null;
        this.books = userDto.getBooks() != null
                ? userDto.getBooks().stream()
                .map(bookDto -> new Book(bookDto.getTitle(), bookDto.getAuthor()))
                .collect(Collectors.toSet())
                : new HashSet<>();
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + role));
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

    @Override
    public boolean equals(Object o) {

        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        User user = (User) o;
        return Objects.equals(username, user.username) &&
                Objects.equals(email, user.email);
    }

    @Override
    public int hashCode() {
        return Objects.hash(username, email);
    }
}
