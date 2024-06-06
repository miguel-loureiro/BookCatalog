package com.bookcatalog.bookcatalog.model;
import java.io.IOException;
import java.util.Date;

import com.bookcatalog.bookcatalog.helpers.DateHelper;
import com.fasterxml.jackson.annotation.JsonFormat;

import jakarta.persistence.*;

@Entity
@Table(name = "books")
public class Book {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private String title;
    private String author;
    private String price;

    @JsonFormat(pattern = "MM/yyyy")
    private Date publishDate;

    private String coverImage;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    public Book(Integer id, String title, String author, String price, Date publishDate, String coverImage, User user) {
        this.id = id;
        this.title = title;
        this.author = author;
        this.price = price;
        this.publishDate = publishDate;
        this.coverImage = coverImage;
        this.user = user;
    }

    public Book() {

    }

    // Getters and setters
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getPrice() {
        return price;
    }

    public void setPrice(String price) {
        this.price = price;
    }

    public String getPublishDate() {
        return DateHelper.serialize(publishDate);
    }

    public void setPublishDate(String publishDate) throws IOException {
        this.publishDate = DateHelper.deserialize(publishDate);
    }

    public String getCoverImage() {
        return coverImage;
    }

    public void setCoverImage(String coverImage) {
        this.coverImage = coverImage;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }
}
