package com.utilitybill.dao;

import com.utilitybill.exception.DataPersistenceException;

import java.util.List;
import java.util.Optional;

/**
 * Generic interface for data persistence operations.
 * This interface defines the contract for all DAO implementations,
 * supporting CRUD operations on any entity type.
 *
 * <p>Design Pattern: DAO (Data Access Object) - Separates data access
 * logic from business logic, providing a clean abstraction layer.</p>
 *
 * @param <T>  the entity type
 * @param <ID> the identifier type
 * @author Utility Bill Management System
 * @version 1.0
 * @since 2024
 */
public interface DataPersistence<T, ID> {

    /**
     * Saves a new entity to the data store.
     *
     * @param entity the entity to save
     * @throws DataPersistenceException if the save operation fails
     */
    void save(T entity) throws DataPersistenceException;

    /**
     * Finds an entity by its unique identifier.
     *
     * @param id the unique identifier
     * @return an Optional containing the entity if found, empty otherwise
     * @throws DataPersistenceException if the find operation fails
     */
    Optional<T> findById(ID id) throws DataPersistenceException;

    /**
     * Retrieves all entities from the data store.
     *
     * @return a list of all entities
     * @throws DataPersistenceException if the find operation fails
     */
    List<T> findAll() throws DataPersistenceException;

    /**
     * Updates an existing entity in the data store.
     *
     * @param entity the entity to update
     * @throws DataPersistenceException if the update operation fails
     */
    void update(T entity) throws DataPersistenceException;

    /**
     * Deletes an entity by its unique identifier.
     *
     * @param id the unique identifier
     * @throws DataPersistenceException if the delete operation fails
     */
    void delete(ID id) throws DataPersistenceException;

    /**
     * Checks if an entity exists with the given identifier.
     *
     * @param id the unique identifier
     * @return true if the entity exists
     * @throws DataPersistenceException if the check fails
     */
    boolean exists(ID id) throws DataPersistenceException;

    /**
     * Returns the count of all entities in the data store.
     *
     * @return the count of entities
     * @throws DataPersistenceException if the count operation fails
     */
    long count() throws DataPersistenceException;

    /**
     * Deletes all entities from the data store.
     *
     * @throws DataPersistenceException if the operation fails
     */
    void deleteAll() throws DataPersistenceException;

    /**
     * Saves multiple entities to the data store.
     *
     * @param entities the entities to save
     * @throws DataPersistenceException if the save operation fails
     */
    void saveAll(List<T> entities) throws DataPersistenceException;

    /**
     * Refreshes the cache from the data store.
     *
     * @throws DataPersistenceException if the refresh fails
     */
    void refresh() throws DataPersistenceException;
}

