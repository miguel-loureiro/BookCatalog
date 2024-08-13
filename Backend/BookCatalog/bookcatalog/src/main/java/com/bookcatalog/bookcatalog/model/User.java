package com.bookcatalog.bookcatalog.model;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

import com.bookcatalog.bookcatalog.helpers.DateHelper;
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

    @ManyToMany
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
}
