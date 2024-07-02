package com.bookcatalog.bookcatalog.model.dto;

import lombok.Getter;
import lombok.Setter;

public class BookShortDto {

    @Getter
    @Setter
    private String title;
    @Getter
    @Setter
    private String author;
    @Getter
    @Setter
    private String isbn;
    @Getter
    @Setter
    private String publishDate;
    @Getter
    @Setter
    private String price;

    public BookShortDto(String title, String author, String isbn,String publishDate, String price) {
        this.title = title;
        this.author = author;
        this.isbn = isbn;
        this.publishDate = publishDate;
        this.price = price;
    }

    public BookShortDto() {
    }
}
