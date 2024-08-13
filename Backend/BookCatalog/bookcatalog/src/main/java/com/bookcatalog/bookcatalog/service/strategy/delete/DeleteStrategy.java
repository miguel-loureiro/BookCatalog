package com.bookcatalog.bookcatalog.service.strategy.delete;

import com.bookcatalog.bookcatalog.model.Book;

import java.io.IOException;

public interface DeleteStrategy<T> {

        void delete(T entity) throws IOException;
    }
