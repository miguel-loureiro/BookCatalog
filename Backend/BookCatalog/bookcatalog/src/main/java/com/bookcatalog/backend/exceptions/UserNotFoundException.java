package com.newbookcatalog.newbookcatalog.exceptions;

public class UserNotFoundException extends RuntimeException {

    public UserNotFoundException(String message, Throwable cause) {

        super(message, cause);
    }
}
