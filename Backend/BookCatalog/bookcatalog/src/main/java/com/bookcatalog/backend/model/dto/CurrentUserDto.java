package com.bookcatalog.backend.model.dto;

import com.bookcatalog.backend.model.Role;

public class CurrentUserDto {

    private String username;
    private String email;
    private Role role;

    public CurrentUserDto(String username, String email, Role role) {
        this.username = username;
        this.email = email;
        this.role = role;
    }

    // Getters and setters
    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }
}
