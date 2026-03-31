package com.lorenzodm.librepm.core.entity;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

/**
 * JPA converter for LocalTime ↔ String ("HH:mm:ss").
 * Required because SQLite has no native TIME type and its JDBC driver
 * cannot parse bare time strings like "09:00:00" via ResultSet.getTime().
 *
 * @author Lorenzo DM
 * @since 0.5.3
 */
@Converter(autoApply = false)
public class LocalTimeStringConverter implements AttributeConverter<LocalTime, String> {

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("HH:mm:ss");

    @Override
    public String convertToDatabaseColumn(LocalTime time) {
        return time != null ? time.format(FMT) : null;
    }

    @Override
    public LocalTime convertToEntityAttribute(String dbValue) {
        if (dbValue == null || dbValue.isBlank()) return null;
        return LocalTime.parse(dbValue, FMT);
    }
}
