package com.bookcatalog.bookcatalog.model;

public class LoginResponse {

    private String token;
    private long expiresIn;

    public String getToken() {
        return token;
    }

    public LoginResponse setToken(String token) {

        if (token == null) {
            throw new IllegalArgumentException("Token cannot be null");
        }
        this.token = token;
        return this;
    }

    public long getExpiresIn() {
        return expiresIn;
    }

    public LoginResponse setExpiresIn(Long expiresIn) {

        if (expiresIn == null) {

            throw new IllegalArgumentException("ExpiresIn cannot be null");
        }
        if (expiresIn <= 0) {

            throw new IllegalArgumentException("expiresIn must be positive");
        }
        this.expiresIn = expiresIn;
        return this;
        }
    }

