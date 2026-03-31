package com.lorenzodm.librepm.core.converter;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

/**
 * JPA Attribute Converter per LocalDateTime <-> String (SQLite TEXT)
 *
 * SQLite non ha un tipo DATETIME nativo, salva tutto come TEXT.
 * Questo converter gestisce la conversione bidirezionale.
 *
 * Formati supportati in lettura:
 * - yyyy-MM-dd'T'HH:mm:ss (ISO standard)
 * - yyyy-MM-dd HH:mm:ss (spazio invece di T)
 * - yyyy-MM-dd HH:mm:ss.SSS (con millisecondi)
 * - yyyy-MM-dd (solo data, aggiunge 00:00:00)
 *
 * Formato scrittura: yyyy-MM-dd HH:mm:ss (compatibile SQLite)
 *
 * @author Lorenzo DM
 * @since 0.2.1
 */
@Converter(autoApply = true)
public class LocalDateTimeAttributeConverter implements AttributeConverter<LocalDateTime, String> {

    private static final DateTimeFormatter SQLITE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final DateTimeFormatter SQLITE_FORMAT_MS = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");
    private static final DateTimeFormatter ISO_FORMAT = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
    private static final DateTimeFormatter DATE_ONLY = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    @Override
    public String convertToDatabaseColumn(LocalDateTime attribute) {
        if (attribute == null) {
            return null;
        }
        // Salva in formato SQLite standard: yyyy-MM-dd HH:mm:ss
        return attribute.format(SQLITE_FORMAT);
    }

    @Override
    public LocalDateTime convertToEntityAttribute(String dbData) {
        if (dbData == null || dbData.isBlank()) {
            return null;
        }

        String trimmed = dbData.trim();

        // Prova formato SQLite standard (yyyy-MM-dd HH:mm:ss)
        try {
            return LocalDateTime.parse(trimmed, SQLITE_FORMAT);
        } catch (DateTimeParseException e) {
            // Continua con altri formati
        }

        // Prova formato SQLite con millisecondi (yyyy-MM-dd HH:mm:ss.SSS)
        try {
            return LocalDateTime.parse(trimmed, SQLITE_FORMAT_MS);
        } catch (DateTimeParseException e) {
            // Continua
        }

        // Prova formato ISO (yyyy-MM-dd'T'HH:mm:ss)
        try {
            return LocalDateTime.parse(trimmed, ISO_FORMAT);
        } catch (DateTimeParseException e) {
            // Continua
        }

        // Prova solo data (yyyy-MM-dd) - aggiunge mezzanotte
        try {
            if (trimmed.length() == 10) {
                return java.time.LocalDate.parse(trimmed, DATE_ONLY).atStartOfDay();
            }
        } catch (DateTimeParseException e) {
            // Continua
        }

        // Prova con millisecondi variabili
        try {
            // Rimuovi millisecondi extra se presenti
            if (trimmed.contains(".")) {
                String withoutMs = trimmed.substring(0, trimmed.indexOf('.'));
                return LocalDateTime.parse(withoutMs, SQLITE_FORMAT);
            }
        } catch (Exception e) {
            // Ignora
        }

        // Se tutto fallisce, logga e ritorna null
        System.err.println("[LocalDateTimeConverter] Impossibile parsare datetime: '" + dbData + "'");
        return null;
    }
}