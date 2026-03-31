package com.lorenzodm.librepm.core.converter;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

/**
 * JPA Attribute Converter per LocalDate <-> String (SQLite TEXT)
 *
 * SQLite non ha un tipo DATE nativo, salva tutto come TEXT.
 * Questo converter gestisce la conversione bidirezionale.
 *
 * Formati supportati in lettura:
 * - yyyy-MM-dd (ISO standard)
 * - yyyy-MM-dd HH:mm:ss (datetime completo, prende solo la parte data)
 * - yyyy-MM-dd HH:mm:ss.SSS (datetime con millisecondi)
 *
 * Formato scrittura: yyyy-MM-dd (ISO standard)
 *
 * @author Lorenzo DM
 * @since 0.2.1
 */
@Converter(autoApply = true)
public class LocalDateAttributeConverter implements AttributeConverter<LocalDate, String> {

    private static final DateTimeFormatter ISO_DATE = DateTimeFormatter.ISO_LOCAL_DATE;
    private static final DateTimeFormatter ISO_DATE_TIME = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final DateTimeFormatter ISO_DATE_TIME_MS = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");

    @Override
    public String convertToDatabaseColumn(LocalDate attribute) {
        if (attribute == null) {
            return null;
        }
        // Salva in formato ISO standard: yyyy-MM-dd
        return attribute.format(ISO_DATE);
    }

    @Override
    public LocalDate convertToEntityAttribute(String dbData) {
        if (dbData == null || dbData.isBlank()) {
            return null;
        }

        String trimmed = dbData.trim();

        // Prova prima il formato ISO date (yyyy-MM-dd) - caso più comune
        try {
            return LocalDate.parse(trimmed, ISO_DATE);
        } catch (DateTimeParseException e) {
            // Continua con altri formati
        }

        // Prova formato datetime con millisecondi (yyyy-MM-dd HH:mm:ss.SSS)
        try {
            return LocalDate.parse(trimmed.substring(0, Math.min(10, trimmed.length())), ISO_DATE);
        } catch (DateTimeParseException | StringIndexOutOfBoundsException e) {
            // Continua
        }

        // Prova formato datetime senza millisecondi (yyyy-MM-dd HH:mm:ss)
        try {
            if (trimmed.length() >= 10) {
                return LocalDate.parse(trimmed.substring(0, 10), ISO_DATE);
            }
        } catch (DateTimeParseException e) {
            // Continua
        }

        // Ultimo tentativo: prova a parsare come ISO_DATE_TIME completo
        try {
            return java.time.LocalDateTime.parse(trimmed, ISO_DATE_TIME).toLocalDate();
        } catch (DateTimeParseException e) {
            // Ignora
        }

        // Se tutto fallisce, logga e ritorna null
        System.err.println("[LocalDateConverter] Impossibile parsare data: '" + dbData + "'");
        return null;
    }
}