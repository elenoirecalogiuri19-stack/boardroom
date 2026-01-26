package main.web.rest;

import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.AccessDeniedException;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import main.repository.PrenotazioniRepository;
import main.service.PrenotazioniService;
import main.service.dto.PrenotazioniDTO;
import main.service.mapper.PrenotazioniMapper;
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

/**
 * REST controller for managing {@link main.domain.Prenotazioni}.
 */
@RestController
@RequestMapping("/api/prenotazionis")
public class PrenotazioniResource {

    private static final Logger LOG = LoggerFactory.getLogger(PrenotazioniResource.class);

    private static final String ENTITY_NAME = "prenotazioni";

    @Value("${jhipster.clientApp.name}")
    private String applicationName;

    private final PrenotazioniService prenotazioniService;

    private final PrenotazioniRepository prenotazioniRepository;

    private final PrenotazioniMapper prenotazioniMapper;

    public PrenotazioniResource(
        PrenotazioniService prenotazioniService,
        PrenotazioniRepository prenotazioniRepository,
        PrenotazioniMapper prenotazioniMapper
    ) {
        this.prenotazioniService = prenotazioniService;
        this.prenotazioniRepository = prenotazioniRepository;
        this.prenotazioniMapper = prenotazioniMapper;
    }

    /**
     * {@code POST  /prenotazionis} : Create a new prenotazioni.
     *
     * @param prenotazioniDTO the prenotazioniDTO to create.
     * @return the {@link ResponseEntity} with status {@code 201 (Created)} and with body the new prenotazioniDTO, or with status {@code 400 (Bad Request)} if the prenotazioni has already an ID.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PostMapping("")
    public ResponseEntity<PrenotazioniDTO> createPrenotazioni(@Valid @RequestBody PrenotazioniDTO prenotazioniDTO)
        throws URISyntaxException {
        LOG.debug("REST request to save Prenotazioni : {}", prenotazioniDTO);
        if (prenotazioniDTO.getId() != null) {
            throw new BadRequestAlertException("A new prenotazioni cannot already have an ID", ENTITY_NAME, "idexists");
        }
        prenotazioniDTO = prenotazioniService.save(prenotazioniDTO);
        return ResponseEntity.created(new URI("/api/prenotazionis/" + prenotazioniDTO.getId()))
            .headers(HeaderUtil.createEntityCreationAlert(applicationName, false, ENTITY_NAME, prenotazioniDTO.getId().toString()))
            .body(prenotazioniDTO);
    }

    /**
     * {@code PUT  /prenotazionis/:id} : Updates an existing prenotazioni.
     *
     * @param id the id of the prenotazioniDTO to save.
     * @param prenotazioniDTO the prenotazioniDTO to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated prenotazioniDTO,
     * or with status {@code 400 (Bad Request)} if the prenotazioniDTO is not valid,
     * or with status {@code 500 (Internal Server Error)} if the prenotazioniDTO couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PutMapping("/{id}")
    public ResponseEntity<PrenotazioniDTO> updatePrenotazioni(
        @PathVariable(value = "id", required = false) final UUID id,
        @Valid @RequestBody PrenotazioniDTO prenotazioniDTO
    ) throws URISyntaxException {
        LOG.debug("REST request to update Prenotazioni : {}, {}", id, prenotazioniDTO);
        if (prenotazioniDTO.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, prenotazioniDTO.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!prenotazioniRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        prenotazioniDTO = prenotazioniService.update(prenotazioniDTO);
        return ResponseEntity.ok()
            .headers(HeaderUtil.createEntityUpdateAlert(applicationName, false, ENTITY_NAME, prenotazioniDTO.getId().toString()))
            .body(prenotazioniDTO);
    }

    /**
     * {@code PATCH  /prenotazionis/:id} : Partial updates given fields of an existing prenotazioni, field will ignore if it is null
     *
     * @param id the id of the prenotazioniDTO to save.
     * @param prenotazioniDTO the prenotazioniDTO to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated prenotazioniDTO,
     * or with status {@code 400 (Bad Request)} if the prenotazioniDTO is not valid,
     * or with status {@code 404 (Not Found)} if the prenotazioniDTO is not found,
     * or with status {@code 500 (Internal Server Error)} if the prenotazioniDTO couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PatchMapping(value = "/{id}", consumes = { "application/json", "application/merge-patch+json" })
    public ResponseEntity<PrenotazioniDTO> partialUpdatePrenotazioni(
        @PathVariable(value = "id", required = false) final UUID id,
        @NotNull @RequestBody PrenotazioniDTO prenotazioniDTO
    ) throws URISyntaxException {
        LOG.debug("REST request to partial update Prenotazioni partially : {}, {}", id, prenotazioniDTO);
        if (prenotazioniDTO.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, prenotazioniDTO.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!prenotazioniRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        Optional<PrenotazioniDTO> result = prenotazioniService.partialUpdate(prenotazioniDTO);

        return ResponseUtil.wrapOrNotFound(
            result,
            HeaderUtil.createEntityUpdateAlert(applicationName, false, ENTITY_NAME, prenotazioniDTO.getId().toString())
        );
    }

    /**
     * {@code GET  /prenotazionis} : get all the prenotazionis.
     *
     * @param pageable the pagination information.
     * @param eagerload flag to eager load entities from relationships (This is applicable for many-to-many).
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list of prenotazionis in body.
     * US2: Semplificato per evitare errori di compilazione con il Service.
     */
    @GetMapping("")
    @Transactional(readOnly = true)
    public ResponseEntity<List<PrenotazioniDTO>> getAllPrenotazionis(
        @org.springdoc.core.annotations.ParameterObject Pageable pageable,
        @RequestParam(name = "eagerload", required = false, defaultValue = "true") boolean eagerload,
        @RequestParam(name = "salaId", required = false) UUID salaId
    ) {
        LOG.debug("REST request to get a page of Prenotazionis. Filter salaId: {}", salaId);
        Page<PrenotazioniDTO> page;

        if (salaId != null) {
            page = prenotazioniRepository.findBySalaId(salaId, pageable).map(prenotazioniMapper::toDto);
        } else if (eagerload) {
            page = prenotazioniService.findAllWithEagerRelationships(pageable);
        } else {
            page = prenotazioniService.findAll(pageable);
        }

        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(ServletUriComponentsBuilder.fromCurrentRequest(), page);
        return ResponseEntity.ok().headers(headers).body(page.getContent());
    }

    /**
     * {@code GET  /prenotazionis/:id} : get the "id" prenotazioni.
     */
    @GetMapping("/{id}")
    public ResponseEntity<PrenotazioniDTO> getPrenotazioni(@PathVariable("id") UUID id) {
        LOG.debug("REST request to get Prenotazioni : {}", id);
        Optional<PrenotazioniDTO> prenotazioniDTO = prenotazioniService.findOne(id);
        return ResponseUtil.wrapOrNotFound(prenotazioniDTO);
    }

    /**
     * {@code DELETE  /prenotazionis/:id} : delete the "id" prenotazioni.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePrenotazioni(@PathVariable("id") UUID id) {
        LOG.debug("REST request to delete Prenotazioni : {}", id);

        try {
            prenotazioniService.delete(id);
            return ResponseEntity.noContent()
                .headers(HeaderUtil.createEntityDeletionAlert(applicationName, true, ENTITY_NAME, id.toString()))
                .build();
        } catch (AccessDeniedException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        } catch (EntityNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     *
     * Endpoint per la prenotazione con validazione
     *
     */
    @PostMapping("/crea")
    public ResponseEntity<PrenotazioniDTO> creaPrenotazione(@Valid @RequestBody PrenotazioniDTO prenotazioniDTO) {
        PrenotazioniDTO dto = prenotazioniService.creaPrenotazione(prenotazioniDTO);
        return ResponseEntity.ok(dto);
    }

    /**
     *
     * Endpoint per  la conferma della prenotazione
     *
     */
    @PostMapping("/{id}/conferma")
    public ResponseEntity<PrenotazioniDTO> confermaPrenotazioni(@PathVariable UUID id) {
        PrenotazioniDTO dto = prenotazioniService.confermaPrenotazione(id);
        return ResponseEntity.ok(dto);
    }
}
