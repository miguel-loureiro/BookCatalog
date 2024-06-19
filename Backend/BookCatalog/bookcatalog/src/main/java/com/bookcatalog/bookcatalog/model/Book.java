package com.bookcatalog.bookcatalog.model;
import java.io.IOException;
import java.util.Date;

import com.bookcatalog.bookcatalog.helpers.DateHelper;
import com.fasterxml.jackson.annotation.JsonFormat;

import jakarta.persistence.*;
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
    @Getter
    @Setter
    private String price;

    @JsonFormat(pattern = "MM/yyyy")
    private Date publishDate;

    @Setter
    @Getter
    private String coverImage;

    @Setter
    @Getter
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

    public String getPublishDate() {
        return DateHelper.serialize(publishDate);
    }

    public void setPublishDate(String publishDate) throws IOException {
        this.publishDate = DateHelper.deserialize(publishDate);
    }
}
