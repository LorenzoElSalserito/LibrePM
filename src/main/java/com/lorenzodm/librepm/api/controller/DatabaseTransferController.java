package com.lorenzodm.librepm.api.controller;

import com.lorenzodm.librepm.api.dto.response.DatabaseImportResponse;
import com.lorenzodm.librepm.api.dto.response.DatabaseStatusResponse;
import com.lorenzodm.librepm.service.DatabaseTransferService;
import com.lorenzodm.librepm.service.EncryptionService;
import com.lorenzodm.librepm.service.EventJournalService;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.InputStreamResource;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@RestController
@RequestMapping("/api/db")
public class DatabaseTransferController {

    private final DatabaseTransferService databaseTransferService;
    private final EncryptionService encryptionService;
    private final EventJournalService eventJournalService;

    /** Marker written into encrypted backups so the importer can recognise them. */
    public static final String ENCRYPTED_FORMAT = "librepm-encrypted-db";

    public DatabaseTransferController(DatabaseTransferService databaseTransferService,
                                      EncryptionService encryptionService,
                                      EventJournalService eventJournalService) {
        this.databaseTransferService = databaseTransferService;
        this.encryptionService = encryptionService;
        this.eventJournalService = eventJournalService;
    }

    @GetMapping("/status")
    public DatabaseStatusResponse status() {
        return databaseTransferService.status();
    }

    @GetMapping("/backup-info")
    public java.util.Map<String, Object> backupInfo() {
        return databaseTransferService.getBackupInfo();
    }

    @GetMapping("/export")
    public ResponseEntity<InputStreamResource> export(
            @RequestParam(name = "format", defaultValue = "db") String format,
            @RequestParam(name = "includeAssets", defaultValue = "false") boolean includeAssets
    ) {
        DatabaseTransferService.ExportedFile exported;

        if ("zip".equalsIgnoreCase(format)) {
            exported = databaseTransferService.exportZip(includeAssets);
        } else {
            exported = databaseTransferService.exportDbSnapshot();
        }

        InputStreamResource resource = databaseTransferService.asResource(exported.path());
        eventJournalService.record("DB_EXPORT", "DATABASE", null, "{\"format\":\"" + format + "\"}", null);

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(exported.contentType()))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + exported.downloadName() + "\"")
                .body(resource);
    }

    @PostMapping(value = "/import", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<DatabaseImportResponse> importDb(@RequestPart("file") MultipartFile file) {
        return importResult(databaseTransferService.importDb(file));
    }

    /**
     * Raw-body variant used by the desktop app: streams the .db file without the multipart size
     * limits (real databases easily exceed the 1 MB multipart default).
     */
    @PostMapping(value = "/import", consumes = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    public ResponseEntity<DatabaseImportResponse> importDbRaw(HttpServletRequest request) throws java.io.IOException {
        try (var in = request.getInputStream()) {
            return importResult(databaseTransferService.importDbStream(in));
        }
    }

    private ResponseEntity<DatabaseImportResponse> importResult(DatabaseImportResponse result) {
        eventJournalService.record("DB_IMPORT", "DATABASE", null,
                "{\"accepted\":" + result.accepted() + "}", null);
        return result.accepted() ? ResponseEntity.ok(result) : ResponseEntity.badRequest().body(result);
    }

    @ExceptionHandler(DatabaseTransferService.FileTransferUnsupportedException.class)
    public ResponseEntity<Map<String, Object>> unsupported(DatabaseTransferService.FileTransferUnsupportedException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of(
                "status", 409,
                "error", "FILE_TRANSFER_UNSUPPORTED",
                "message", ex.getMessage()));
    }

    @PostMapping("/export-encrypted")
    public ResponseEntity<Map<String, String>> exportEncrypted(@RequestBody Map<String, String> body) {
        String passphrase = body.get("passphrase");
        if (passphrase == null || passphrase.length() < 4) {
            return ResponseEntity.badRequest().body(Map.of("error", "Passphrase must be at least 4 characters"));
        }

        DatabaseTransferService.ExportedFile exported = databaseTransferService.exportDbSnapshot();
        try {
            byte[] data = java.nio.file.Files.readAllBytes(exported.path());
            java.nio.file.Files.deleteIfExists(exported.path());
            EncryptionService.EncryptedPayload payload = encryptionService.encrypt(data, passphrase);
            eventJournalService.record("DB_EXPORT", "DATABASE", null, "{\"format\":\"encrypted\"}", null);
            return ResponseEntity.ok(Map.of(
                    "format", ENCRYPTED_FORMAT,
                    "formatVersion", "1",
                    "algorithm", "AES-256-GCM/PBKDF2",
                    "ciphertext", payload.ciphertext(),
                    "salt", payload.salt(),
                    "iv", payload.iv()
            ));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of("error", "Encryption failed: " + e.getMessage()));
        }
    }

    /**
     * Encrypted backups embed the whole database as Base64, which can exceed Jackson's default
     * 20M-character string limit: parse the body with a mapper sized for large backups.
     */
    private static final com.fasterxml.jackson.databind.ObjectMapper LARGE_JSON =
            com.fasterxml.jackson.databind.json.JsonMapper.builder(
                    com.fasterxml.jackson.core.JsonFactory.builder()
                            .streamReadConstraints(com.fasterxml.jackson.core.StreamReadConstraints.builder()
                                    .maxStringLength(Integer.MAX_VALUE).build())
                            .build()).build();

    @PostMapping(value = "/import-encrypted", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<DatabaseImportResponse> importEncrypted(HttpServletRequest request) throws java.io.IOException {
        com.fasterxml.jackson.databind.JsonNode body;
        try (var in = request.getInputStream()) {
            body = LARGE_JSON.readTree(in);
        } catch (com.fasterxml.jackson.core.JsonProcessingException e) {
            return ResponseEntity.badRequest().body(new DatabaseImportResponse(false, false, null,
                    "The file is not a LibrePM encrypted backup"));
        }
        String passphrase = body == null ? null : body.path("passphrase").asText(null);
        String ciphertext = body == null ? null : body.path("ciphertext").asText(null);
        String salt = body == null ? null : body.path("salt").asText(null);
        String iv = body == null ? null : body.path("iv").asText(null);

        if (passphrase == null || ciphertext == null || salt == null || iv == null) {
            return ResponseEntity.badRequest().body(
                    new DatabaseImportResponse(false, false, null, "Missing required fields"));
        }

        byte[] decrypted;
        try {
            EncryptionService.EncryptedPayload payload = new EncryptionService.EncryptedPayload(ciphertext, salt, iv);
            decrypted = encryptionService.decrypt(payload, passphrase);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new DatabaseImportResponse(false, false, null,
                    "Decryption failed: wrong passphrase or damaged backup file"));
        }
        return importResult(databaseTransferService.importDecryptedDb(decrypted));
    }
}
