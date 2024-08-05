package com.bookcatalog.bookcatalog.model;
import java.io.IOException;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import com.bookcatalog.bookcatalog.helpers.DateHelper;
import com.bookcatalog.bookcatalog.model.dto.BookShortDto;
import com.bookcatalog.bookcatalog.model.dto.UserShortDto;
import com.fasterxml.jackson.annotation.JsonFormat;

import jakarta.persistence.*;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "books")
public class Book {
    @Setter
    @Getter
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    @Setter
    @Getter
    private String title;
    @Getter
    @Setter
    private String author;
    @Setter
    @Getter
    @Pattern(regexp = "^(97([89]))?\\d{9}(\\d|X)$", message = "Invalid ISBN number. Must be a valid ISBN-10 or ISBN-13 format.")
    private String isbn;
    @Setter
    @Getter
    private String price;

    @JsonFormat(pattern = "MM/yyyy")
    private Date publishDate;

    @Setter
    @Getter
    private String coverImage;

    @Getter
    @ManyToMany(mappedBy = "books")
    private Set<User> users = new HashSet<>();

    /*
    public List<UserShortDto> getUsersShort() {
        return users.stream()
                .map(user -> new UserShortDto(
                        user.getUsername(),
                        user.getEmail(),
                        user.getRole()))
                .collect(Collectors.toList());
    }
*/
    public Book() {

    }

    public Book(Integer id, String title, String author, String isbn, String price, Date publishDate, String coverImage, Set<User> users) {
        this.id = id;
        this.title = title;
        this.author = author;
        this.isbn = isbn;
        this.price = price;
        this.publishDate = publishDate;
        this.coverImage = coverImage;
        this.users = users;
    }

    public Book(Integer id, String title, String author, String isbn, String price, Date publishDate, String coverImage) {
        this.id = id;
        this.title = title;
        this.author = author;
        this.isbn = isbn;
        this.price = price;
        this.publishDate = publishDate;
        this.coverImage = coverImage;
    }

    public String getPublishDate() {
        return DateHelper.serialize(publishDate);
    }

    public void setPublishDate(String publishDate) throws IOException {
        this.publishDate = DateHelper.deserialize(publishDate);
    }
}
