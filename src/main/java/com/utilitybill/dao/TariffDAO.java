package com.utilitybill.dao;

import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import com.utilitybill.exception.DataPersistenceException;
import com.utilitybill.model.ElectricityTariff;
import com.utilitybill.model.GasTariff;
import com.utilitybill.model.MeterType;
import com.utilitybill.model.Tariff;

import java.io.*;
import java.lang.reflect.Type;
import java.time.LocalDate;
import java.util.*;

/**
 * Data Access Object for Tariff entities.
 * Handles persistence of tariff data to JSON files with polymorphic deserialization.
 *
 * <p>Design Pattern: Singleton - Only one instance manages tariff data.</p>
 *
 * @author Utility Bill Management System
 * @version 1.0
 * @since 2024
 */
public class TariffDAO extends AbstractJsonDAO<Tariff, String> {

    /** Singleton instance */
    private static volatile TariffDAO instance;

    /** Custom Gson with type adapter for Tariff hierarchy */
    private static final Gson TARIFF_GSON = new GsonBuilder()
            .setPrettyPrinting()
            .registerTypeAdapter(LocalDate.class, new LocalDateAdapter())
            .registerTypeAdapter(Tariff.class, new TariffTypeAdapter())
            .create();

    /**
     * Private constructor for singleton pattern.
     */
    private TariffDAO() {
        super("tariffs.json");
    }

    /**
     * Gets the singleton instance.
     *
     * @return the TariffDAO instance
     */
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

    @Override
    protected Type getEntityListType() {
        return new TypeToken<List<Tariff>>(){}.getType();
    }

    /**
     * Overrides loadFromFile to use custom Gson for polymorphic deserialization.
     */
    @Override
    protected void loadFromFile() throws DataPersistenceException {
        File file = new File(filePath);
        if (!file.exists()) {
            cache.clear();
            return;
        }

        try (Reader reader = new FileReader(file)) {
            JsonArray jsonArray = JsonParser.parseReader(reader).getAsJsonArray();
            cache.clear();
            for (JsonElement element : jsonArray) {
                Tariff tariff = TARIFF_GSON.fromJson(element, Tariff.class);
                cache.put(getId(tariff), tariff);
            }
        } catch (IOException e) {
            throw DataPersistenceException.readError(filePath, e);
        } catch (Exception e) {
            throw DataPersistenceException.deserializationError(filePath, e);
        }
    }

    /**
     * Overrides saveToFile to use custom Gson for polymorphic serialization.
     */
    @Override
    protected void saveToFile() throws DataPersistenceException {
        try (Writer writer = new FileWriter(filePath)) {
            JsonArray jsonArray = new JsonArray();
            for (Tariff tariff : cache.values()) {
                jsonArray.add(TARIFF_GSON.toJsonTree(tariff, Tariff.class));
            }
            TARIFF_GSON.toJson(jsonArray, writer);
        } catch (IOException e) {
            throw DataPersistenceException.writeError(filePath, e);
        }
    }

    /**
     * Finds all tariffs for a specific meter type.
     *
     * @param meterType the meter type
     * @return list of tariffs for that meter type
     * @throws DataPersistenceException if the operation fails
     */
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

    /**
     * Finds all active tariffs.
     *
     * @return list of active tariffs
     * @throws DataPersistenceException if the operation fails
     */
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

    /**
     * Finds active tariffs for a specific meter type.
     *
     * @param meterType the meter type
     * @return list of active tariffs for that type
     * @throws DataPersistenceException if the operation fails
     */
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

    /**
     * Finds a tariff by name.
     *
     * @param name the tariff name
     * @return an Optional containing the tariff if found
     * @throws DataPersistenceException if the operation fails
     */
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

    /**
     * Custom type adapter for polymorphic Tariff serialization/deserialization.
     */
    private static class TariffTypeAdapter implements JsonSerializer<Tariff>, JsonDeserializer<Tariff> {

        private static final String TYPE_FIELD = "_type";

        @Override
        public JsonElement serialize(Tariff src, Type typeOfSrc, JsonSerializationContext context) {
            JsonObject jsonObject = new JsonObject();

            // Add type discriminator
            if (src instanceof ElectricityTariff) {
                jsonObject.addProperty(TYPE_FIELD, "ElectricityTariff");
                JsonObject data = (JsonObject) new GsonBuilder()
                        .registerTypeAdapter(LocalDate.class, new LocalDateAdapter())
                        .create().toJsonTree(src);
                for (Map.Entry<String, JsonElement> entry : data.entrySet()) {
                    jsonObject.add(entry.getKey(), entry.getValue());
                }
            } else if (src instanceof GasTariff) {
                jsonObject.addProperty(TYPE_FIELD, "GasTariff");
                JsonObject data = (JsonObject) new GsonBuilder()
                        .registerTypeAdapter(LocalDate.class, new LocalDateAdapter())
                        .create().toJsonTree(src);
                for (Map.Entry<String, JsonElement> entry : data.entrySet()) {
                    jsonObject.add(entry.getKey(), entry.getValue());
                }
            }

            return jsonObject;
        }

        @Override
        public Tariff deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
                throws JsonParseException {
            JsonObject jsonObject = json.getAsJsonObject();

            if (!jsonObject.has(TYPE_FIELD)) {
                throw new JsonParseException("Missing type field in Tariff JSON");
            }

            String type = jsonObject.get(TYPE_FIELD).getAsString();
            Gson gson = new GsonBuilder()
                    .registerTypeAdapter(LocalDate.class, new LocalDateAdapter())
                    .create();

            return switch (type) {
                case "ElectricityTariff" -> gson.fromJson(json, ElectricityTariff.class);
                case "GasTariff" -> gson.fromJson(json, GasTariff.class);
                default -> throw new JsonParseException("Unknown tariff type: " + type);
            };
        }
    }
}

