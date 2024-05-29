package com.bookcatalog.bookcatalog.dao;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.*;

// This class is a bridge bettween the Database and the application
@Entity
@Table(name = "user")
public class DAOUser {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column
    private String username;

    @Column
    @JsonIgnore
    private String password;

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
