package com.newbookcatalog.newbookcatalog.exceptions;

public class InvalidIsbnException extends RuntimeException{

    public InvalidIsbnException(String message, Throwable cause) {

        super(message, cause);
    }
}
