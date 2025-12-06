package com.utilitybill.dao;

import com.google.gson.*;

import java.lang.reflect.Type;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Gson adapter for LocalDateTime serialization and deserialization.
 * Converts LocalDateTime to/from ISO-8601 format.
 *
 * @author Utility Bill Management System
 * @version 1.0
 * @since 2024
 */
public class LocalDateTimeAdapter implements JsonSerializer<LocalDateTime>, JsonDeserializer<LocalDateTime> {

    /** DateTime formatter using ISO-8601 format */
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    /**
     * Serializes a LocalDateTime to JSON.
     *
     * @param src       the LocalDateTime to serialize
     * @param typeOfSrc the type of the source object
     * @param context   the serialization context
     * @return the serialized JSON element
     */
    @Override
    public JsonElement serialize(LocalDateTime src, Type typeOfSrc, JsonSerializationContext context) {
        if (src == null) {
            return JsonNull.INSTANCE;
        }
        return new JsonPrimitive(src.format(FORMATTER));
    }

    /**
     * Deserializes a JSON element to a LocalDateTime.
     *
     * @param json    the JSON element to deserialize
     * @param typeOfT the type of the target object
     * @param context the deserialization context
     * @return the deserialized LocalDateTime
     * @throws JsonParseException if parsing fails
     */
    @Override
    public LocalDateTime deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
            throws JsonParseException {
        if (json == null || json.isJsonNull()) {
            return null;
        }
        try {
            return LocalDateTime.parse(json.getAsString(), FORMATTER);
        } catch (Exception e) {
            throw new JsonParseException("Failed to parse LocalDateTime: " + json.getAsString(), e);
        }
    }
}

