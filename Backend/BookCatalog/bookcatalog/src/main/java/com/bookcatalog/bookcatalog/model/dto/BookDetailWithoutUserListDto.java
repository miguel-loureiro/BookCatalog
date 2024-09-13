package com.bookcatalog.bookcatalog.model.dto;

import com.bookcatalog.bookcatalog.model.Book;
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
    private String coverImage;

    public BookDetailWithoutUserListDto(Book book) {
        this.title = book.getTitle();
        this.author = book.getAuthor();
        this.isbn = book.getIsbn();
        this.price = book.getPrice();
        this.publishDate = book.getPublishDate();
        this.coverImage = book.getCoverImage();
    }

    public BookDetailWithoutUserListDto(String title, String author, String isbn, String price, String publishDate, String coverImage) {
        this.title = title;
        this.author = author;
        this.isbn = isbn;
        this.price = price;
        this.publishDate = publishDate;
        this.coverImage = coverImage;
    }
}
