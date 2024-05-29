package com.bookcatalog.bookcatalog.repository;

import org.springframework.data.repository.CrudRepository;

import com.bookcatalog.bookcatalog.dao.DAOUser;

public interface UserRepository extends CrudRepository<DAOUser, Integer> {

    UserRepository findByUsername(String username);

}
