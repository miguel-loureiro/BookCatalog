package com.bookcatalog.bookcatalog.model;
import java.io.IOException;
import java.util.Date;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

import com.bookcatalog.bookcatalog.helpers.DateHelper;
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
    @Column(unique = true, nullable = false)
    private String title;
    @Getter
    @Setter
    private String author;
    @Setter
    @Getter
    @Column(unique = true, nullable = false)
    @Pattern(regexp = "^(97([89]))?\\d{9}(\\d|X)$", message = "Invalid ISBN number. Must be a valid ISBN-10 or ISBN-13 format.")
    private String isbn;
    @Setter
    @Getter
    private String price;

    @JsonFormat(pattern = "MM/yyyy")
    private Date publishDate;

    @Setter
    @Getter
    private String coverImageUrl;

    @Version
    private Long version;

    @Getter
    @ManyToMany(mappedBy = "books")
    private Set<User> users = new HashSet<>();

    public Book() {

    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Book book = (Book) o;
        return Objects.equals(id, book.id) && Objects.equals(title, book.title) && Objects.equals(author, book.author) && Objects.equals(isbn, book.isbn) && Objects.equals(price, book.price) && Objects.equals(publishDate, book.publishDate) && Objects.equals(coverImageUrl, book.coverImageUrl) && Objects.equals(version, book.version) && Objects.equals(users, book.users);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, title, author, isbn, price, publishDate, coverImageUrl, version, users);
    }

    public Book(String title, String author, String isbn, String price, Date publishDate, String coverImageUrl, Set<User> users) {

        this.title = title;
        this.author = author;
        this.isbn = isbn;
        this.price = price;
        this.publishDate = publishDate;
        this.coverImageUrl = coverImageUrl;
        this.users = users;
    }

    public Book(String title, String author, String isbn, String price, Date publishDate, String coverImageUrl) {

        this.title = title;
        this.author = author;
        this.isbn = isbn;
        this.price = price;
        this.publishDate = publishDate;
        this.coverImageUrl = coverImageUrl;
    }

    public Book(Integer id, String title, String author) {

        this.id = id;
        this.title = title;
        this.author = author;
        this.isbn = "000000000000";
        this.price = "0.00";
        this.publishDate = new Date();
        this.coverImageUrl = "https://example.com/image.jpg";
    }

    public Book(String title, String author) {

        this.title = title;
        this.author = author;
        this.isbn = "000000000000";
        this.price = "0.00";
        this.publishDate = new Date();
        this.coverImageUrl = "https://example.com/image.jpg";
    }

    public String getPublishDate() {
        return DateHelper.serialize(publishDate);
    }

    public void setPublishDate(String publishDate) throws IOException {
        this.publishDate = DateHelper.deserialize(publishDate);
    }
}
