package com.bookcatalog.bookcatalog.model.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
public class BookTitleAndAuthorDto {

    @Getter
    @Setter
    private String title;
    @Getter
    @Setter
    private String author;

    public BookTitleAndAuthorDto(String title, String author) {
        this.title = title;
        this.author = author;
    }
}
