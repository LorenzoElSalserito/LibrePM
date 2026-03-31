package com.lorenzodm.librepm.api.controller;

import com.lorenzodm.librepm.api.dto.response.DatabaseImportResponse;
import com.lorenzodm.librepm.api.dto.response.DatabaseStatusResponse;
import com.lorenzodm.librepm.service.DatabaseTransferService;
import com.lorenzodm.librepm.service.EncryptionService;
import com.lorenzodm.librepm.service.EventJournalService;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
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
    public DatabaseImportResponse importDb(@RequestPart("file") MultipartFile file) {
        DatabaseImportResponse result = databaseTransferService.importDb(file);
        eventJournalService.record("DB_IMPORT", "DATABASE", null,
                "{\"accepted\":" + result.accepted() + "}", null);
        return result;
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
            EncryptionService.EncryptedPayload payload = encryptionService.encrypt(data, passphrase);
            return ResponseEntity.ok(Map.of(
                    "ciphertext", payload.ciphertext(),
                    "salt", payload.salt(),
                    "iv", payload.iv()
            ));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of("error", "Encryption failed: " + e.getMessage()));
        }
    }

    @PostMapping("/import-encrypted")
    public ResponseEntity<DatabaseImportResponse> importEncrypted(@RequestBody Map<String, String> body) {
        String passphrase = body.get("passphrase");
        String ciphertext = body.get("ciphertext");
        String salt = body.get("salt");
        String iv = body.get("iv");

        if (passphrase == null || ciphertext == null || salt == null || iv == null) {
            return ResponseEntity.badRequest().body(
                    new DatabaseImportResponse(false, false, null, "Missing required fields"));
        }

        try {
            EncryptionService.EncryptedPayload payload = new EncryptionService.EncryptedPayload(ciphertext, salt, iv);
            byte[] decrypted = encryptionService.decrypt(payload, passphrase);
            DatabaseImportResponse result = databaseTransferService.importDecryptedDb(decrypted);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(
                    new DatabaseImportResponse(false, false, null, "Decryption failed: " + e.getMessage()));
        }
    }
}
