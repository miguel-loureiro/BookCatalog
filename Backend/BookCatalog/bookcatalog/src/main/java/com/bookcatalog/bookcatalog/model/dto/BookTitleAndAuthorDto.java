package com.bookcatalog.bookcatalog.model.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BookTitleAndAuthorDto {

    // Getters and setters
    private String title;
    private String author;

    public BookTitleAndAuthorDto(String title, String author) {
        this.title = title;
        this.author = author;
    }
}
