package com.bookcatalog.bookcatalog.model;

import java.util.Collections;
import java.util.List;

import org.springframework.security.core.authority.SimpleGrantedAuthority;

public enum Role {

    SUPER,
    ADMIN,
    READER,
    GUEST;
}
