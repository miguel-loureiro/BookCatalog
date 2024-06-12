package com.bookcatalog.bookcatalog.repository;

import org.springframework.data.repository.CrudRepository;
import com.bookcatalog.bookcatalog.model.User;

public interface UserRepository extends CrudRepository<User, Long>{

    User findByEmail(String email);
    User findByUsername(String username);

}
