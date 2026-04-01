package main.web.rest;

import jakarta.persistence.EntityNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.UUID;
import main.security.AuthoritiesConstants;
import main.service.SaleService;
import main.service.dto.SaleDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

/**
 * REST controller per la gestione delle immagini delle sale.
 *
 * Strategia MVP: le immagini vengono salvate nel filesystem locale
 * nella directory {app.upload.dir}/sale/ (configurabile in application.yml).
 * Il file viene servito come risorsa statica da Spring tramite
 * WebMvcConfigurer (vedere WebConfigurer o aggiungere configurazione).
 *
 * Endpoint:
 *   POST   /api/sales/{id}/immagine   → upload immagine (solo admin)
 *   DELETE /api/sales/{id}/immagine   → rimuovi immagine (solo admin)
 */
@RestController
@RequestMapping("/api/sales")
public class SaleImmagineResource {

    private static final Logger LOG = LoggerFactory.getLogger(SaleImmagineResource.class);

    /** Dimensione massima: 5 MB */
    private static final long MAX_SIZE_BYTES = 5 * 1024 * 1024;

    /** Formati accettati */
    private static final List<String> TIPI_ACCETTATI = List.of("image/jpeg", "image/jpg", "image/png", "image/webp");

    /** Directory di upload — creare la cartella o configurare in application.yml */
    private static final String UPLOAD_DIR = "uploads/sale";

    private final SaleService saleService;

    public SaleImmagineResource(SaleService saleService) {
        this.saleService = saleService;
    }

    /**
     * POST /api/sales/{id}/immagine
     * Carica e associa un'immagine a una sala.
     *
     * Validazioni:
     * - Solo JPEG, PNG, WEBP
     * - Max 5 MB
     * - Il file viene rinominato con UUID per evitare collisioni e path traversal
     */
    @PostMapping(value = "/{id}/immagine", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAuthority(\"" + AuthoritiesConstants.ADMIN + "\")")
    public ResponseEntity<SaleDTO> uploadImmagine(@PathVariable UUID id, @RequestParam("file") MultipartFile file) {
        LOG.debug("REST request to upload immagine for sale: {}", id);

        // ── Validazione ──────────────────────────────────────
        if (file.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "File vuoto");
        }
        if (file.getSize() > MAX_SIZE_BYTES) {
            throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "File troppo grande (max 5 MB, ricevuto " + (file.getSize() / 1024 / 1024) + " MB)"
            );
        }
        String contentType = file.getContentType();
        if (contentType == null || !TIPI_ACCETTATI.contains(contentType.toLowerCase())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Formato non supportato. Accettati: JPEG, PNG, WEBP");
        }

        // ── Estrai estensione sicura ──────────────────────────
        String originalFilename = StringUtils.cleanPath(file.getOriginalFilename() != null ? file.getOriginalFilename() : "immagine");
        String extension = getEstensione(contentType);
        String nomeFile = id + extension; // es: "3f2504e0-...jpg"

        // ── Salva sul filesystem ──────────────────────────────
        try {
            Path uploadPath = Paths.get(UPLOAD_DIR);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            // Elimina la vecchia immagine se presente
            SaleDTO existing = saleService.findOne(id).orElseThrow(() -> new EntityNotFoundException("Sala non trovata: " + id));
            if (existing.getImageUrl() != null) {
                Path vecchio = Paths.get("." + existing.getImageUrl());
                try {
                    Files.deleteIfExists(vecchio);
                } catch (IOException ignored) {}
            }

            Path destinazione = uploadPath.resolve(nomeFile);
            Files.copy(file.getInputStream(), destinazione, StandardCopyOption.REPLACE_EXISTING);

            String imageUrl = "/" + UPLOAD_DIR + "/" + nomeFile;

            // ── Aggiorna la sala nel DB ───────────────────────
            SaleDTO dto = saleService.findOne(id).orElseThrow(() -> new EntityNotFoundException("Sala non trovata: " + id));
            dto.setImageUrl(imageUrl);
            SaleDTO risultato = saleService.update(dto);

            LOG.info("Immagine sala {} salvata: {}", id, imageUrl);
            return ResponseEntity.ok(risultato);
        } catch (IOException e) {
            LOG.error("Errore salvataggio immagine per sala {}", id, e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Errore durante il salvataggio del file");
        }
    }

    /**
     * DELETE /api/sales/{id}/immagine
     * Rimuove l'immagine di una sala.
     */
    @DeleteMapping("/{id}/immagine")
    @PreAuthorize("hasAuthority(\"" + AuthoritiesConstants.ADMIN + "\")")
    public ResponseEntity<Void> eliminaImmagine(@PathVariable UUID id) {
        LOG.debug("REST request to delete immagine for sale: {}", id);

        SaleDTO dto = saleService.findOne(id).orElseThrow(() -> new EntityNotFoundException("Sala non trovata: " + id));

        if (dto.getImageUrl() != null) {
            Path filePath = Paths.get("." + dto.getImageUrl());
            try {
                Files.deleteIfExists(filePath);
            } catch (IOException ignored) {}
            dto.setImageUrl(null);
            saleService.update(dto);
        }

        return ResponseEntity.noContent().build();
    }

    // ── Helpers ───────────────────────────────────────────────

    private String getEstensione(String contentType) {
        return switch (contentType.toLowerCase()) {
            case "image/jpeg", "image/jpg" -> ".jpg";
            case "image/png" -> ".png";
            case "image/webp" -> ".webp";
            default -> ".jpg";
        };
    }
}
