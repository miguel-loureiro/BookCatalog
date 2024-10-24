package com.bookcatalog.backend.model;

import java.util.Collections;
import java.util.List;

import org.springframework.security.core.authority.SimpleGrantedAuthority;

public enum Role {

    SUPER(4),
    ADMIN(3),
    READER(2),
    GUEST(1);

    private final int rank;

    Role(int rank) {

        this.rank = rank;
    }

    public int getRank() {

        return this.rank;
    }
}
