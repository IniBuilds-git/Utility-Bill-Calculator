package com.utilitybill.dao;

import com.google.gson.reflect.TypeToken;
import com.utilitybill.exception.DataPersistenceException;
import com.utilitybill.model.MeterReading;

import java.lang.reflect.Type;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

/**
 * Data Access Object for MeterReading entities.
 * Handles persistence of meter reading data to JSON files.
 *
 * <p>Design Pattern: Singleton - Only one instance manages meter reading data.</p>
 *
 * @author Utility Bill Management System
 * @version 1.0
 * @since 2024
 */
public class MeterReadingDAO extends AbstractJsonDAO<MeterReading, String> {

    /** Singleton instance */
    private static volatile MeterReadingDAO instance;

    /** Type token for JSON deserialization */
    private static final Type READING_LIST_TYPE = new TypeToken<List<MeterReading>>(){}.getType();

    /**
     * Private constructor for singleton pattern.
     */
    private MeterReadingDAO() {
        super("meter_readings.json");
    }

    /**
     * Gets the singleton instance.
     *
     * @return the MeterReadingDAO instance
     */
    public static MeterReadingDAO getInstance() {
        if (instance == null) {
            synchronized (MeterReadingDAO.class) {
                if (instance == null) {
                    instance = new MeterReadingDAO();
                }
            }
        }
        return instance;
    }

    @Override
    protected String getId(MeterReading entity) {
        return entity.getReadingId();
    }

    @Override
    protected Type getEntityListType() {
        return READING_LIST_TYPE;
    }

    /**
     * Finds all readings for a meter.
     *
     * @param meterId the meter ID
     * @return list of readings for the meter, sorted by date
     * @throws DataPersistenceException if the operation fails
     */
    public List<MeterReading> findByMeterId(String meterId) throws DataPersistenceException {
        initializeCache();
        lock.readLock().lock();
        try {
            return cache.values().stream()
                    .filter(r -> r.getMeterId().equals(meterId))
                    .sorted(Comparator.comparing(MeterReading::getReadingDate))
                    .toList();
        } finally {
            lock.readLock().unlock();
        }
    }

    /**
     * Finds all readings for a customer.
     *
     * @param customerId the customer ID
     * @return list of readings for the customer
     * @throws DataPersistenceException if the operation fails
     */
    public List<MeterReading> findByCustomerId(String customerId) throws DataPersistenceException {
        initializeCache();
        lock.readLock().lock();
        try {
            return cache.values().stream()
                    .filter(r -> r.getCustomerId().equals(customerId))
                    .sorted(Comparator.comparing(MeterReading::getReadingDate))
                    .toList();
        } finally {
            lock.readLock().unlock();
        }
    }

    /**
     * Finds the latest reading for a meter.
     *
     * @param meterId the meter ID
     * @return an Optional containing the latest reading
     * @throws DataPersistenceException if the operation fails
     */
    public Optional<MeterReading> findLatestByMeterId(String meterId) throws DataPersistenceException {
        initializeCache();
        lock.readLock().lock();
        try {
            return cache.values().stream()
                    .filter(r -> r.getMeterId().equals(meterId))
                    .max(Comparator.comparing(MeterReading::getReadingDate));
        } finally {
            lock.readLock().unlock();
        }
    }

    /**
     * Finds unbilled readings for a meter.
     *
     * @param meterId the meter ID
     * @return list of unbilled readings
     * @throws DataPersistenceException if the operation fails
     */
    public List<MeterReading> findUnbilledByMeterId(String meterId) throws DataPersistenceException {
        initializeCache();
        lock.readLock().lock();
        try {
            return cache.values().stream()
                    .filter(r -> r.getMeterId().equals(meterId) && !r.isBilled())
                    .sorted(Comparator.comparing(MeterReading::getReadingDate))
                    .toList();
        } finally {
            lock.readLock().unlock();
        }
    }

    /**
     * Finds readings within a date range for a meter.
     *
     * @param meterId   the meter ID
     * @param startDate the start date (inclusive)
     * @param endDate   the end date (inclusive)
     * @return list of readings in the range
     * @throws DataPersistenceException if the operation fails
     */
    public List<MeterReading> findByMeterIdAndDateRange(String meterId, LocalDate startDate, LocalDate endDate)
            throws DataPersistenceException {
        initializeCache();
        lock.readLock().lock();
        try {
            return cache.values().stream()
                    .filter(r -> r.getMeterId().equals(meterId) &&
                            !r.getReadingDate().isBefore(startDate) &&
                            !r.getReadingDate().isAfter(endDate))
                    .sorted(Comparator.comparing(MeterReading::getReadingDate))
                    .toList();
        } finally {
            lock.readLock().unlock();
        }
    }

    /**
     * Finds readings by type.
     *
     * @param readingType the reading type
     * @return list of readings of that type
     * @throws DataPersistenceException if the operation fails
     */
    public List<MeterReading> findByType(MeterReading.ReadingType readingType) throws DataPersistenceException {
        initializeCache();
        lock.readLock().lock();
        try {
            return cache.values().stream()
                    .filter(r -> r.getReadingType() == readingType)
                    .toList();
        } finally {
            lock.readLock().unlock();
        }
    }

    /**
     * Gets the previous reading value for validation.
     *
     * @param meterId the meter ID
     * @return the previous reading value, or 0 if no previous reading
     * @throws DataPersistenceException if the operation fails
     */
    public double getPreviousReadingValue(String meterId) throws DataPersistenceException {
        Optional<MeterReading> latest = findLatestByMeterId(meterId);
        return latest.map(MeterReading::getReadingValue).orElse(0.0);
    }

    /**
     * Calculates total consumption for a meter within a date range.
     *
     * @param meterId   the meter ID
     * @param startDate the start date
     * @param endDate   the end date
     * @return the total consumption
     * @throws DataPersistenceException if the operation fails
     */
    public double getTotalConsumption(String meterId, LocalDate startDate, LocalDate endDate)
            throws DataPersistenceException {
        List<MeterReading> readings = findByMeterIdAndDateRange(meterId, startDate, endDate);
        return readings.stream()
                .mapToDouble(MeterReading::getConsumption)
                .sum();
    }
}

