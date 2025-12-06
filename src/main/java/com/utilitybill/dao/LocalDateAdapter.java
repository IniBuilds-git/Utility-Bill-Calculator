package com.utilitybill.dao;

import com.google.gson.*;

import java.lang.reflect.Type;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * Gson adapter for LocalDate serialization and deserialization.
 * Converts LocalDate to/from ISO-8601 format (yyyy-MM-dd).
 *
 * @author Utility Bill Management System
 * @version 1.0
 * @since 2024
 */
public class LocalDateAdapter implements JsonSerializer<LocalDate>, JsonDeserializer<LocalDate> {

    /** Date formatter using ISO-8601 format */
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE;

    /**
     * Serializes a LocalDate to JSON.
     *
     * @param src       the LocalDate to serialize
     * @param typeOfSrc the type of the source object
     * @param context   the serialization context
     * @return the serialized JSON element
     */
    @Override
    public JsonElement serialize(LocalDate src, Type typeOfSrc, JsonSerializationContext context) {
        if (src == null) {
            return JsonNull.INSTANCE;
        }
        return new JsonPrimitive(src.format(FORMATTER));
    }

    /**
     * Deserializes a JSON element to a LocalDate.
     *
     * @param json    the JSON element to deserialize
     * @param typeOfT the type of the target object
     * @param context the deserialization context
     * @return the deserialized LocalDate
     * @throws JsonParseException if parsing fails
     */
    @Override
    public LocalDate deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
            throws JsonParseException {
        if (json == null || json.isJsonNull()) {
            return null;
        }
        try {
            return LocalDate.parse(json.getAsString(), FORMATTER);
        } catch (Exception e) {
            throw new JsonParseException("Failed to parse LocalDate: " + json.getAsString(), e);
        }
    }
}

