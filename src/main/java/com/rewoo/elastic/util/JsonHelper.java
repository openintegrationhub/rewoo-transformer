package com.rewoo.elastic.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.json.*;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Date;

public final class JsonHelper {
    public static final DateTimeFormatter STANDARD_FORMATTER = DateTimeFormatter.ISO_DATE_TIME;
    private static final Logger logger = LoggerFactory.getLogger(JsonHelper.class);

    public static JsonObject createModificationObject(Long authorId, Date timestamp, DateTimeFormatter dateTimeFormat) {
        String formattedTimestamp = dateTimeFormat.withZone(ZoneId.of("UTC")).format(timestamp.toInstant());
        return Json.createObjectBuilder().add("userId", authorId.toString())
                                         .add("type", "modification")
                                         .add("timestamp", formattedTimestamp).build();
    }

    public static Date getModificationTimestamp(JsonValue modificationValue, DateTimeFormatter dateTimeFormat) {
        JsonValue.ValueType type = modificationValue.getValueType();
        if (type == JsonValue.ValueType.NULL) {
            return null;
        }
        if (type ==  JsonValue.ValueType.NUMBER) {
            long timestamp = ((JsonNumber) modificationValue).longValue();
            logger.info("Detected simple number timestamp. Return {} as date", timestamp);
            return new Date(timestamp);
        }
        if (type == JsonValue.ValueType.OBJECT) {
            String dateTimeString = ((JsonObject) modificationValue).getString("timestamp");
            // This is a modification object -> should be the standard way for the oih. Try to parse the timestamp supplied as a string
            try {
                ZonedDateTime timestamp = java.time.ZonedDateTime.parse(dateTimeString, dateTimeFormat);
                return new Date(timestamp.toInstant().toEpochMilli());
            } catch (IllegalArgumentException | DateTimeParseException e) {
                logger.error("Unable to parse timestamp {} with supplied date/time pattern {}", dateTimeString, dateTimeFormat, e);
                return null;
            }
        }
        logger.warn("Unable to detect timestamp within modification object. Returning null");
        return null;
    }

    public static Long getAuthorId(JsonValue modificationValue) {
        JsonValue.ValueType type = modificationValue.getValueType();
        if (type == JsonValue.ValueType.NULL) {
            return null;
        }
        String authorIdAsString = ((JsonObject) modificationValue).getString("authorId");
        return Long.parseLong(authorIdAsString);
    }
}
