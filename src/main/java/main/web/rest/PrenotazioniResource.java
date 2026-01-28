package main.web.rest;

import jakarta.validation.Valid;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import main.service.PrenotazioniService;
import main.service.dto.PrenotazioniDTO;
import main.web.rest.errors.BadRequestAlertException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import tech.jhipster.web.util.HeaderUtil;
import tech.jhipster.web.util.PaginationUtil;
import tech.jhipster.web.util.ResponseUtil;

@RestController
@RequestMapping("/api/prenotazionis")
@Transactional
public class PrenotazioniResource {

    private static final Logger log = LoggerFactory.getLogger(PrenotazioniResource.class);
    private static final String ENTITY_NAME = "prenotazioni";

    @Value("${jhipster.clientApp.name}")
    private String applicationName;

    private final PrenotazioniService prenotazioniService;

    public PrenotazioniResource(PrenotazioniService prenotazioniService) {
        this.prenotazioniService = prenotazioniService;
    }

    // --- CREATE GENERICO ---
    @PostMapping
    public ResponseEntity<PrenotazioniDTO> createPrenotazioni(@Valid @RequestBody PrenotazioniDTO prenotazioniDTO) {
        log.debug("REST request to save Prenotazioni : {}", prenotazioniDTO);
        if (prenotazioniDTO.getId() != null) {
            throw new BadRequestAlertException("A new prenotazioni cannot already have an ID", ENTITY_NAME, "idexists");
        }

        PrenotazioniDTO result = prenotazioniService.save(prenotazioniDTO);
        return ResponseEntity.created(ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}").buildAndExpand(result.getId()).toUri())
            .headers(HeaderUtil.createEntityCreationAlert(applicationName, true, ENTITY_NAME, result.getId().toString()))
            .body(result);
    }

    // --- UPDATE ---
    @PutMapping("/{id}")
    public ResponseEntity<PrenotazioniDTO> updatePrenotazioni(@PathVariable UUID id, @Valid @RequestBody PrenotazioniDTO prenotazioniDTO) {
        log.debug("REST request to update Prenotazioni : {}, {}", id, prenotazioniDTO);
        if (prenotazioniDTO.getId() == null || !id.equals(prenotazioniDTO.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        PrenotazioniDTO result = prenotazioniService.save(prenotazioniDTO);
        return ResponseEntity.ok()
            .headers(HeaderUtil.createEntityUpdateAlert(applicationName, true, ENTITY_NAME, result.getId().toString()))
            .body(result);
    }

    // --- PATCH ---
    @PatchMapping(value = "/{id}", consumes = { "application/json", "application/merge-patch+json" })
    public ResponseEntity<PrenotazioniDTO> partialUpdatePrenotazioni(@PathVariable UUID id, @RequestBody PrenotazioniDTO prenotazioniDTO) {
        log.debug("REST request to partially update Prenotazioni : {}, {}", id, prenotazioniDTO);
        if (prenotazioniDTO.getId() == null || !id.equals(prenotazioniDTO.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        Optional<PrenotazioniDTO> result = prenotazioniService.partialUpdate(prenotazioniDTO);
        return ResponseUtil.wrapOrNotFound(result, HeaderUtil.createEntityUpdateAlert(applicationName, true, ENTITY_NAME, id.toString()));
    }

    // --- GET ALL ---
    @GetMapping
    @Transactional(readOnly = true)
    public ResponseEntity<List<PrenotazioniDTO>> getAllPrenotazionis(
        Pageable pageable,
        @RequestParam(name = "eagerload", defaultValue = "true") boolean eagerload,
        @RequestParam(name = "salaId", required = false) UUID salaId
    ) {
        log.debug("REST request to get all Prenotazionis, filter salaId: {}", salaId);
        Page<PrenotazioniDTO> page = prenotazioniService.getAll(pageable, eagerload, salaId);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(ServletUriComponentsBuilder.fromCurrentRequest(), page);
        return ResponseEntity.ok().headers(headers).body(page.getContent());
    }

    // --- GET BY ID ---
    @GetMapping("/{id}")
    public ResponseEntity<PrenotazioniDTO> getPrenotazioni(@PathVariable UUID id) {
        log.debug("REST request to get Prenotazioni : {}", id);
        Optional<PrenotazioniDTO> dto = prenotazioniService.findOne(id);
        return ResponseUtil.wrapOrNotFound(dto);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> annullPrenotazione(@PathVariable UUID id) {
        log.debug("REST request to delete Prenotazioni : {}", id);
        prenotazioniService.delete(id);
        return ResponseEntity.noContent()
            .headers(HeaderUtil.createEntityDeletionAlert(applicationName, true, ENTITY_NAME, id.toString()))
            .build();
    }

    // --- US4: CREAZIONE PRENOTAZIONE ---
    @PostMapping("/crea")
    public ResponseEntity<PrenotazioniDTO> creaPrenotazione(@Valid @RequestBody PrenotazioniDTO prenotazioniDTO) {
        log.debug("REST request to create custom Prenotazione : {}", prenotazioniDTO);
        if (prenotazioniDTO.getUtente() == null || prenotazioniDTO.getUtente().getId() == null) {
            throw new BadRequestAlertException("Utente obbligatorio", ENTITY_NAME, "utentenull");
        }
        if (prenotazioniDTO.getSala() == null || prenotazioniDTO.getSala().getId() == null) {
            throw new BadRequestAlertException("Sala obbligatoria", ENTITY_NAME, "salanull");
        }

        PrenotazioniDTO result = prenotazioniService.creaPrenotazione(prenotazioniDTO);
        return ResponseEntity.ok(result);
    }

    // --- US6: CONFERMA PRENOTAZIONE ---
    @PostMapping("/{id}/conferma")
    public ResponseEntity<PrenotazioniDTO> confermaPrenotazioni(@PathVariable UUID id) {
        log.debug("REST request to confirm Prenotazione : {}", id);
        Optional<PrenotazioniDTO> result = prenotazioniService.confermaPrenotazione(id);
        return ResponseUtil.wrapOrNotFound(result);
    }

    @GetMapping("/storico")
    public ResponseEntity<List<PrenotazioniDTO>> getStoricoPrenotazioni() {
        log.debug("REST request to get storico prenotazioni");
        return ResponseEntity.ok(prenotazioniService.getStoricoPrenotazioni());
    }

    @GetMapping("/odierne")
    public ResponseEntity<List<PrenotazioniDTO>> getPrenotazioniOdierne() {
        log.debug("REST request to get prenotazioni odierne e future");
        return ResponseEntity.ok(prenotazioniService.getPrenotazioniOdierne());
    }
}
