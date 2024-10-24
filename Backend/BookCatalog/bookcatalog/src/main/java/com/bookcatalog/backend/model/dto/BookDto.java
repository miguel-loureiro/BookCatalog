package com.newbookcatalog.newbookcatalog.model.dto;

import com.newbookcatalog.newbookcatalog.model.Book;
import lombok.Getter;
import lombok.Setter;
import java.util.Set;
import java.util.stream.Collectors;

@Getter
@Setter
public class BookDto {

    private String title;
    private String author;
    private String isbn;
    private String price;
    private String publishDate;
    private String coverImage;
    private Set<String> users;

    public BookDto(Book book) {
        this.title = book.getTitle();
        this.author = book.getAuthor();
        this.isbn = book.getIsbn();
        this.price = book.getPrice();
        this.publishDate = book.getPublishDate();
        this.coverImage = book.getCoverImageUrl();
        this.users = book.getUsers().stream()
                .map(user -> user.getUsername())
                .collect(Collectors.toSet());
    }
}

