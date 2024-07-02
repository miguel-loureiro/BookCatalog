package com.bookcatalog.bookcatalog.model.dto;

import com.bookcatalog.bookcatalog.model.Role;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class UserShortDto {

    private String username;
    private String email;
    private Role role;

    public UserShortDto() {
    }

    public UserShortDto(String username, String email, Role role) {
        this.username = username;
        this.email = email;
        this.role = role;
    }
}
