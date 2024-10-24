package com.bookcatalog.backend.model.dto;

import com.bookcatalog.backend.model.Book;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BookDetailWithoutUserListDto {

    private String title;
    private String author;
    private String isbn;
    private String price;
    private String publishDate;
    private String coverImageUrl;

    public BookDetailWithoutUserListDto(Book book) {
        this.title = book.getTitle();
        this.author = book.getAuthor();
        this.isbn = book.getIsbn();
        this.price = book.getPrice();
        this.publishDate = book.getPublishDate();
        this.coverImageUrl = book.getCoverImageUrl();
    }

    public BookDetailWithoutUserListDto(String title, String author, String isbn, String price, String publishDate, String coverImageUrl) {
        this.title = title;
        this.author = author;
        this.isbn = isbn;
        this.price = price;
        this.publishDate = publishDate;
        this.coverImageUrl = coverImageUrl;
    }
}
