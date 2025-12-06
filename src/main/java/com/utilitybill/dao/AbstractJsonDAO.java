package com.utilitybill.dao;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.utilitybill.exception.DataPersistenceException;

import java.io.*;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Abstract base class for JSON-based data persistence.
 * Provides common functionality for reading/writing JSON files with caching.
 *
 * <p>Design Patterns used:</p>
 * <ul>
 *   <li>Template Method - Subclasses implement getId() and getEntityType()</li>
 *   <li>Caching - In-memory cache to reduce file I/O</li>
 *   <li>Thread Safety - Uses ReadWriteLock for concurrent access</li>
 * </ul>
 *
 * @param <T>  the entity type
 * @param <ID> the identifier type
 * @author Utility Bill Management System
 * @version 1.0
 * @since 2024
 */
public abstract class AbstractJsonDAO<T, ID> implements DataPersistence<T, ID> {

    /** Shared Gson instance with custom adapters for LocalDate/LocalDateTime */
    protected static final Gson GSON = new GsonBuilder()
            .setPrettyPrinting()
            .registerTypeAdapter(LocalDate.class, new LocalDateAdapter())
            .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter())
            .create();

    /** Base directory for data files */
    protected static final String DATA_DIR = "data";

    /** Path to the JSON file */
    protected final String filePath;

    /** In-memory cache of entities */
    protected Map<ID, T> cache;

    /** Lock for thread-safe access */
    protected final ReadWriteLock lock = new ReentrantReadWriteLock();

    /** Whether the cache is initialized */
    protected boolean cacheInitialized = false;

    /**
     * Constructs a new AbstractJsonDAO with the specified file name.
     *
     * @param fileName the name of the JSON file (without path)
     */
    protected AbstractJsonDAO(String fileName) {
        this.filePath = DATA_DIR + File.separator + fileName;
        this.cache = new LinkedHashMap<>();
        ensureDataDirectoryExists();
    }

    /**
     * Gets the unique identifier from an entity.
     * Must be implemented by subclasses.
     *
     * @param entity the entity
     * @return the entity's unique identifier
     */
    protected abstract ID getId(T entity);

    /**
     * Gets the Type for JSON deserialization.
     * Must be implemented by subclasses using TypeToken.
     *
     * @return the Type for the List of entities
     */
    protected abstract Type getEntityListType();

    /**
     * Ensures the data directory exists.
     */
    private void ensureDataDirectoryExists() {
        try {
            Path dataPath = Paths.get(DATA_DIR);
            if (!Files.exists(dataPath)) {
                Files.createDirectories(dataPath);
            }
        } catch (IOException e) {
            System.err.println("Warning: Could not create data directory: " + e.getMessage());
        }
    }

    /**
     * Initializes the cache from the file if not already done.
     *
     * @throws DataPersistenceException if reading fails
     */
    protected void initializeCache() throws DataPersistenceException {
        if (!cacheInitialized) {
            lock.writeLock().lock();
            try {
                if (!cacheInitialized) {
                    loadFromFile();
                    cacheInitialized = true;
                }
            } finally {
                lock.writeLock().unlock();
            }
        }
    }

    /**
     * Loads all entities from the JSON file into the cache.
     *
     * @throws DataPersistenceException if reading fails
     */
    protected void loadFromFile() throws DataPersistenceException {
        File file = new File(filePath);
        if (!file.exists()) {
            cache.clear();
            return;
        }

        try (Reader reader = new FileReader(file)) {
            List<T> entities = GSON.fromJson(reader, getEntityListType());
            cache.clear();
            if (entities != null) {
                for (T entity : entities) {
                    cache.put(getId(entity), entity);
                }
            }
        } catch (IOException e) {
            throw DataPersistenceException.readError(filePath, e);
        } catch (Exception e) {
            throw DataPersistenceException.deserializationError(filePath, e);
        }
    }

    /**
     * Writes all cached entities to the JSON file.
     *
     * @throws DataPersistenceException if writing fails
     */
    protected void saveToFile() throws DataPersistenceException {
        try (Writer writer = new FileWriter(filePath)) {
            GSON.toJson(new ArrayList<>(cache.values()), writer);
        } catch (IOException e) {
            throw DataPersistenceException.writeError(filePath, e);
        }
    }

    @Override
    public void save(T entity) throws DataPersistenceException {
        initializeCache();
        lock.writeLock().lock();
        try {
            ID id = getId(entity);
            cache.put(id, entity);
            saveToFile();
        } finally {
            lock.writeLock().unlock();
        }
    }

    @Override
    public Optional<T> findById(ID id) throws DataPersistenceException {
        initializeCache();
        lock.readLock().lock();
        try {
            return Optional.ofNullable(cache.get(id));
        } finally {
            lock.readLock().unlock();
        }
    }

    @Override
    public List<T> findAll() throws DataPersistenceException {
        initializeCache();
        lock.readLock().lock();
        try {
            return new ArrayList<>(cache.values());
        } finally {
            lock.readLock().unlock();
        }
    }

    @Override
    public void update(T entity) throws DataPersistenceException {
        initializeCache();
        lock.writeLock().lock();
        try {
            ID id = getId(entity);
            if (!cache.containsKey(id)) {
                throw new DataPersistenceException("Entity not found for update: " + id,
                        filePath, DataPersistenceException.Operation.WRITE);
            }
            cache.put(id, entity);
            saveToFile();
        } finally {
            lock.writeLock().unlock();
        }
    }

    @Override
    public void delete(ID id) throws DataPersistenceException {
        initializeCache();
        lock.writeLock().lock();
        try {
            cache.remove(id);
            saveToFile();
        } finally {
            lock.writeLock().unlock();
        }
    }

    @Override
    public boolean exists(ID id) throws DataPersistenceException {
        initializeCache();
        lock.readLock().lock();
        try {
            return cache.containsKey(id);
        } finally {
            lock.readLock().unlock();
        }
    }

    @Override
    public long count() throws DataPersistenceException {
        initializeCache();
        lock.readLock().lock();
        try {
            return cache.size();
        } finally {
            lock.readLock().unlock();
        }
    }

    @Override
    public void deleteAll() throws DataPersistenceException {
        lock.writeLock().lock();
        try {
            cache.clear();
            saveToFile();
        } finally {
            lock.writeLock().unlock();
        }
    }

    @Override
    public void saveAll(List<T> entities) throws DataPersistenceException {
        initializeCache();
        lock.writeLock().lock();
        try {
            for (T entity : entities) {
                cache.put(getId(entity), entity);
            }
            saveToFile();
        } finally {
            lock.writeLock().unlock();
        }
    }

    @Override
    public void refresh() throws DataPersistenceException {
        lock.writeLock().lock();
        try {
            cacheInitialized = false;
            loadFromFile();
            cacheInitialized = true;
        } finally {
            lock.writeLock().unlock();
        }
    }

    /**
     * Gets the file path for this DAO.
     *
     * @return the file path
     */
    public String getFilePath() {
        return filePath;
    }
}

