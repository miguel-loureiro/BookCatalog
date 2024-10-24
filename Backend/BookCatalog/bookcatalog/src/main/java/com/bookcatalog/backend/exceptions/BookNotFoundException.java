package com.bookcatalog.backend.exceptions;

public class BookNotFoundException extends RuntimeException {

    public BookNotFoundException(String message, Throwable cause) {

        super(message, cause);
    }
}
