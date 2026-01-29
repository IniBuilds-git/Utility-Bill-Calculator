package com.utilitybill.dao;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.utilitybill.exception.DataPersistenceException;
import com.utilitybill.util.AppLogger;

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

public abstract class AbstractJsonDAO<T, ID> implements DataPersistence<T, ID> {

    private static final String CLASS_NAME = AbstractJsonDAO.class.getName();

    protected static final Gson GSON = new GsonBuilder()
            .setPrettyPrinting()
            .registerTypeAdapter(LocalDate.class, new LocalDateAdapter())
            .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter())
            .create();

    protected static final String DATA_DIR = "data";
    protected final String filePath;
    protected Map<ID, T> cache;
    protected final ReadWriteLock lock = new ReentrantReadWriteLock();
    protected boolean cacheInitialized = false;

    protected AbstractJsonDAO(String fileName) {
        this.filePath = DATA_DIR + File.separator + fileName;
        this.cache = new LinkedHashMap<>();
        ensureDataDirectoryExists();
    }

    protected abstract ID getId(T entity);

    protected abstract Type getEntityListType();

    private void ensureDataDirectoryExists() {
        try {
            Path dataPath = Paths.get(DATA_DIR);
            if (!Files.exists(dataPath)) {
                Files.createDirectories(dataPath);
            }
        } catch (IOException e) {
            AppLogger.error(CLASS_NAME, "Warning: Could not create data directory: " + e.getMessage(), e);
        }
    }

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

    protected void saveToFile() throws DataPersistenceException {
        try (Writer writer = new FileWriter(filePath)) {
            GSON.toJson(new ArrayList<>(cache.values()), writer);
            writer.flush(); // Explicit flush to ensure data is written immediately
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

    public String getFilePath() {
        return filePath;
    }
}
