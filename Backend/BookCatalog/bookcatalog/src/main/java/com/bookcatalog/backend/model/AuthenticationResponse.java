package com.newbookcatalog.newbookcatalog.model;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AuthenticationResponse {

    private User user;
    private String token;

    public AuthenticationResponse(User user, String token) {
        this.user = user;
        this.token = token;
    }
}
