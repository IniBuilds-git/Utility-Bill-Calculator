package com.utilitybill.dao;

import com.utilitybill.exception.DataPersistenceException;
import com.utilitybill.model.MeterType;
import com.utilitybill.model.Tariff;

import java.util.List;
import java.util.Optional;

public class TariffDAO extends AbstractBinaryDAO<Tariff, String> {

    private static volatile TariffDAO instance;

    private TariffDAO() {
        super("tariffs.dat");
    }

    public static TariffDAO getInstance() {
        if (instance == null) {
            synchronized (TariffDAO.class) {
                if (instance == null) {
                    instance = new TariffDAO();
                }
            }
        }
        return instance;
    }

    @Override
    protected String getId(Tariff entity) {
        return entity.getTariffId();
    }

    public List<Tariff> findByMeterType(MeterType meterType) throws DataPersistenceException {
        initializeCache();
        lock.readLock().lock();
        try {
            return cache.values().stream()
                    .filter(t -> t.getMeterType() == meterType)
                    .toList();
        } finally {
            lock.readLock().unlock();
        }
    }

    public List<Tariff> findAllActive() throws DataPersistenceException {
        initializeCache();
        lock.readLock().lock();
        try {
            return cache.values().stream()
                    .filter(Tariff::isCurrentlyValid)
                    .toList();
        } finally {
            lock.readLock().unlock();
        }
    }

    public List<Tariff> findActiveByMeterType(MeterType meterType) throws DataPersistenceException {
        initializeCache();
        lock.readLock().lock();
        try {
            return cache.values().stream()
                    .filter(t -> t.getMeterType() == meterType && t.isCurrentlyValid())
                    .toList();
        } finally {
            lock.readLock().unlock();
        }
    }

    public Optional<Tariff> findByName(String name) throws DataPersistenceException {
        initializeCache();
        lock.readLock().lock();
        try {
            return cache.values().stream()
                    .filter(t -> t.getName().equalsIgnoreCase(name))
                    .findFirst();
        } finally {
            lock.readLock().unlock();
        }
    }
}