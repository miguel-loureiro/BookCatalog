package com.bookcatalog.bookcatalog.service.strategy.update;

import com.bookcatalog.bookcatalog.model.Book;

import java.io.IOException;

public interface UpdateStrategy<T> {

    T update(T entity, T newDetails, String filename) throws IOException;
}
