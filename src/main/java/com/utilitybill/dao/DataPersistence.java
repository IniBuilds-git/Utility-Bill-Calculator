package com.utilitybill.dao;

import com.utilitybill.exception.DataPersistenceException;

import java.util.List;
import java.util.Optional;

public interface DataPersistence<T, ID> {

    void save(T entity) throws DataPersistenceException;

    Optional<T> findById(ID id) throws DataPersistenceException;

    List<T> findAll() throws DataPersistenceException;

    void update(T entity) throws DataPersistenceException;

    void delete(ID id) throws DataPersistenceException;

    boolean exists(ID id) throws DataPersistenceException;

    long count() throws DataPersistenceException;

    void deleteAll() throws DataPersistenceException;

    void saveAll(List<T> entities) throws DataPersistenceException;

    void refresh() throws DataPersistenceException;
}

