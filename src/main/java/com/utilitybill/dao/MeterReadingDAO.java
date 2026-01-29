package com.utilitybill.dao;

import com.utilitybill.exception.DataPersistenceException;
import com.utilitybill.model.MeterReading;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

public class MeterReadingDAO extends AbstractBinaryDAO<MeterReading, String> {

    private static volatile MeterReadingDAO instance;

    private MeterReadingDAO() {
        super("meter_readings.dat");
    }

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

    public double getPreviousReadingValue(String meterId) throws DataPersistenceException {
        Optional<MeterReading> latest = findLatestByMeterId(meterId);
        return latest.map(MeterReading::getReadingValue).orElse(0.0);
    }

    public double getTotalConsumption(String meterId, LocalDate startDate, LocalDate endDate)
            throws DataPersistenceException {
        List<MeterReading> readings = findByMeterIdAndDateRange(meterId, startDate, endDate);
        return readings.stream()
                .mapToDouble(MeterReading::getConsumption)
                .sum();
    }
}
