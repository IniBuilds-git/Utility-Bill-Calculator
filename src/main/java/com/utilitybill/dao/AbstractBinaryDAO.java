package com.utilitybill.dao;

import com.utilitybill.exception.DataPersistenceException;
import com.utilitybill.util.AppLogger;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Abstract Data Access Object that uses binary serialization (.dat files) for
 * persistence.
 * Provides thread-safe CRUD operations with an in-memory cache for better
 * performance.
 *
 * @param <T>  The entity type
 * @param <ID> The ID type
 */
public abstract class AbstractBinaryDAO<T extends Serializable, ID> implements DataPersistence<T, ID> {

    private static final String CLASS_NAME = AbstractBinaryDAO.class.getName();
    protected static final String DATA_DIR = "data";
    protected final String filePath;
    protected Map<ID, T> cache;
    protected final ReadWriteLock lock = new ReentrantReadWriteLock();
    protected boolean cacheInitialized = false;

    protected AbstractBinaryDAO(String fileName) {
        this.filePath = DATA_DIR + File.separator + fileName;
        this.cache = new LinkedHashMap<>();
        ensureDataDirectoryExists();
    }

    protected abstract ID getId(T entity);

    private void ensureDataDirectoryExists() {
        try {
            Path dataPath = Paths.get(DATA_DIR);
            if (!Files.exists(dataPath)) {
                Files.createDirectories(dataPath);
                AppLogger.info(CLASS_NAME, "Created data directory: " + dataPath.toAbsolutePath());
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
                    load();
                    cacheInitialized = true;
                }
            } finally {
                lock.writeLock().unlock();
            }
        }
    }

    @SuppressWarnings("unchecked")
    protected void load() throws DataPersistenceException {
        File file = new File(filePath);
        if (!file.exists()) {
            AppLogger.info(CLASS_NAME, "File does not exist, starting with empty cache: " + filePath);
            cache = new HashMap<>();
            return;
        }

        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file))) {
            cache = (Map<ID, T>) ois.readObject();
            AppLogger.info(CLASS_NAME, "Loaded " + cache.size() + " entities from " + filePath);
        } catch (EOFException e) {
            AppLogger.info(CLASS_NAME, "Empty file detected, starting with empty cache: " + filePath);
        } catch (IOException e) {
            throw DataPersistenceException.readError(filePath, e);
        } catch (ClassNotFoundException e) {
            throw DataPersistenceException.deserializationError(filePath, e);
        }
    }

    protected void saveToFile() throws DataPersistenceException {
        File file = new File(filePath);
        File tempFile = new File(filePath + ".tmp");

        try {
            try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(tempFile))) {
                oos.writeObject(cache);
                oos.flush();
            }

            if (file.exists() && !file.delete()) {
                throw new IOException("Could not delete old file: " + file.getAbsolutePath());
            }

            if (!tempFile.renameTo(file)) {
                throw new IOException("Could not rename temp file to: " + file.getAbsolutePath());
            }

            AppLogger.info(CLASS_NAME, "Saved " + cache.size() + " entities to " + filePath);
        } catch (IOException e) {
            if (tempFile.exists()) {
                tempFile.delete();
            }
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
            load();
            cacheInitialized = true;
        } finally {
            lock.writeLock().unlock();
        }
    }

    public String getFilePath() {
        return filePath;
    }
}