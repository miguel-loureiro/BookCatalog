package com.bookcatalog.bookcatalog.model.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Objects;

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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BookTitleAndAuthorDto that = (BookTitleAndAuthorDto) o;
        return Objects.equals(title, that.title) && Objects.equals(author, that.author);
    }

    @Override
    public int hashCode() {
        return Objects.hash(title, author);
    }
}
