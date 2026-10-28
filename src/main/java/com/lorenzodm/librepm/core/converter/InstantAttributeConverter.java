package com.lorenzodm.librepm.core.converter;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoField;

/**
 * JPA Attribute Converter per Instant <-> String (SQLite TEXT)
 *
 * SQLite non ha un tipo TIMESTAMP/DATETIME nativo, salva tutto come TEXT.
 * Hibernate con @UpdateTimestamp/@CreationTimestamp usa Instant, che di default
 * viene salvato come epoch milliseconds causando errori di parsing.
 *
 * Questo converter gestisce la conversione bidirezionale.
 *
 * Formati supportati in lettura:
 * - yyyy-MM-dd'T'HH:mm:ss'Z' (ISO 8601 UTC)
 * - yyyy-MM-dd'T'HH:mm:ss.SSS'Z' (ISO 8601 UTC con millisecondi)
 * - yyyy-MM-dd HH:mm:ss (SQLite standard)
 * - yyyy-MM-dd HH:mm:ss.SSS (SQLite con millisecondi)
 * - Epoch milliseconds (es. "1769607872740")
 *
 * Formato scrittura: yyyy-MM-dd HH:mm:ss.SSSSSS (compatibile SQLite e DATETIME(6) MariaDB).
 * I microsecondi mantengono l'ordine cronologico di note e messaggi scritti nello stesso secondo.
 *
 * @author Lorenzo DM
 * @since 0.3.1
 */
@Converter(autoApply = true)
public class InstantAttributeConverter implements AttributeConverter<Instant, String> {

    private static final DateTimeFormatter SQLITE_FORMAT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final DateTimeFormatter SQLITE_FORMAT_US =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSSSSS");
    private static final DateTimeFormatter SQLITE_FORMAT_FRACTION = new DateTimeFormatterBuilder()
            .appendPattern("yyyy-MM-dd HH:mm:ss")
            .optionalStart().appendFraction(ChronoField.NANO_OF_SECOND, 0, 9, true).optionalEnd()
            .toFormatter();
    private static final DateTimeFormatter ISO_UTC_FORMAT =
            DateTimeFormatter.ISO_INSTANT;

    @Override
    public String convertToDatabaseColumn(Instant attribute) {
        if (attribute == null) {
            return null;
        }
        LocalDateTime ldt = LocalDateTime.ofInstant(attribute, ZoneOffset.UTC);
        return ldt.format(SQLITE_FORMAT_US);
    }

    @Override
    public Instant convertToEntityAttribute(String dbData) {
        if (dbData == null || dbData.isBlank()) {
            return null;
        }

        String trimmed = dbData.trim();

        // 1. Prova epoch milliseconds (solo numeri)
        if (trimmed.matches("^\\d+$")) {
            try {
                long epoch = Long.parseLong(trimmed);
                return Instant.ofEpochMilli(epoch);
            } catch (NumberFormatException e) {
                // Continua con altri formati
            }
        }

        // 2. Prova formato ISO 8601 UTC (yyyy-MM-dd'T'HH:mm:ss'Z' o con millisecondi)
        try {
            return Instant.parse(trimmed);
        } catch (DateTimeParseException e) {
            // Continua con altri formati
        }

        // 3. Prova formato SQLite, con o senza frazione di secondo (fino ai nanosecondi)
        try {
            LocalDateTime ldt = LocalDateTime.parse(trimmed, SQLITE_FORMAT_FRACTION);
            return ldt.toInstant(ZoneOffset.UTC);
        } catch (DateTimeParseException e) {
            // Continua
        }

        // 4. Prova solo data con ora implicita (yyyy-MM-dd)
        try {
            if (trimmed.length() == 10) {
                LocalDateTime ldt = LocalDateTime.parse(trimmed + " 00:00:00", SQLITE_FORMAT);
                return ldt.toInstant(ZoneOffset.UTC);
            }
        } catch (DateTimeParseException e) {
            // Ignora
        }

        // Se tutto fallisce, logga e ritorna null
        System.err.println("[InstantConverter] Impossibile parsare timestamp: '" + dbData + "'");
        return null;
    }
}