package main.web.rest;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import main.repository.StatiPrenotazioneRepository;
import main.service.StatiPrenotazioneService;
import main.service.dto.StatiPrenotazioneDTO;
import main.web.rest.errors.BadRequestAlertException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tech.jhipster.web.util.HeaderUtil;
import tech.jhipster.web.util.ResponseUtil;

/**
 * REST controller for managing {@link main.domain.StatiPrenotazione}.
 */
@RestController
@RequestMapping("/api/stati-prenotaziones")
public class StatiPrenotazioneResource {

    private static final Logger LOG = LoggerFactory.getLogger(StatiPrenotazioneResource.class);

    private static final String ENTITY_NAME = "statiPrenotazione";

    @Value("${jhipster.clientApp.name}")
    private String applicationName;

    private final StatiPrenotazioneService statiPrenotazioneService;

    private final StatiPrenotazioneRepository statiPrenotazioneRepository;

    public StatiPrenotazioneResource(
        StatiPrenotazioneService statiPrenotazioneService,
        StatiPrenotazioneRepository statiPrenotazioneRepository
    ) {
        this.statiPrenotazioneService = statiPrenotazioneService;
        this.statiPrenotazioneRepository = statiPrenotazioneRepository;
    }

    /**
     * {@code POST  /stati-prenotaziones} : Create a new statiPrenotazione.
     *
     * @param statiPrenotazioneDTO the statiPrenotazioneDTO to create.
     * @return the {@link ResponseEntity} with status {@code 201 (Created)} and with body the new statiPrenotazioneDTO, or with status {@code 400 (Bad Request)} if the statiPrenotazione has already an ID.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PostMapping("")
    public ResponseEntity<StatiPrenotazioneDTO> createStatiPrenotazione(@Valid @RequestBody StatiPrenotazioneDTO statiPrenotazioneDTO)
        throws URISyntaxException {
        LOG.debug("REST request to save StatiPrenotazione : {}", statiPrenotazioneDTO);
        if (statiPrenotazioneDTO.getId() != null) {
            throw new BadRequestAlertException("A new statiPrenotazione cannot already have an ID", ENTITY_NAME, "idexists");
        }
        statiPrenotazioneDTO = statiPrenotazioneService.save(statiPrenotazioneDTO);
        return ResponseEntity.created(new URI("/api/stati-prenotaziones/" + statiPrenotazioneDTO.getId()))
            .headers(HeaderUtil.createEntityCreationAlert(applicationName, false, ENTITY_NAME, statiPrenotazioneDTO.getId().toString()))
            .body(statiPrenotazioneDTO);
    }

    /**
     * {@code PUT  /stati-prenotaziones/:id} : Updates an existing statiPrenotazione.
     *
     * @param id the id of the statiPrenotazioneDTO to save.
     * @param statiPrenotazioneDTO the statiPrenotazioneDTO to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated statiPrenotazioneDTO,
     * or with status {@code 400 (Bad Request)} if the statiPrenotazioneDTO is not valid,
     * or with status {@code 500 (Internal Server Error)} if the statiPrenotazioneDTO couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PutMapping("/{id}")
    public ResponseEntity<StatiPrenotazioneDTO> updateStatiPrenotazione(
        @PathVariable(value = "id", required = false) final UUID id,
        @Valid @RequestBody StatiPrenotazioneDTO statiPrenotazioneDTO
    ) throws URISyntaxException {
        LOG.debug("REST request to update StatiPrenotazione : {}, {}", id, statiPrenotazioneDTO);
        if (statiPrenotazioneDTO.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, statiPrenotazioneDTO.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!statiPrenotazioneRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        statiPrenotazioneDTO = statiPrenotazioneService.update(statiPrenotazioneDTO);
        return ResponseEntity.ok()
            .headers(HeaderUtil.createEntityUpdateAlert(applicationName, false, ENTITY_NAME, statiPrenotazioneDTO.getId().toString()))
            .body(statiPrenotazioneDTO);
    }

    /**
     * {@code PATCH  /stati-prenotaziones/:id} : Partial updates given fields of an existing statiPrenotazione, field will ignore if it is null
     *
     * @param id the id of the statiPrenotazioneDTO to save.
     * @param statiPrenotazioneDTO the statiPrenotazioneDTO to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated statiPrenotazioneDTO,
     * or with status {@code 400 (Bad Request)} if the statiPrenotazioneDTO is not valid,
     * or with status {@code 404 (Not Found)} if the statiPrenotazioneDTO is not found,
     * or with status {@code 500 (Internal Server Error)} if the statiPrenotazioneDTO couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PatchMapping(value = "/{id}", consumes = { "application/json", "application/merge-patch+json" })
    public ResponseEntity<StatiPrenotazioneDTO> partialUpdateStatiPrenotazione(
        @PathVariable(value = "id", required = false) final UUID id,
        @NotNull @RequestBody StatiPrenotazioneDTO statiPrenotazioneDTO
    ) throws URISyntaxException {
        LOG.debug("REST request to partial update StatiPrenotazione partially : {}, {}", id, statiPrenotazioneDTO);
        if (statiPrenotazioneDTO.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, statiPrenotazioneDTO.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!statiPrenotazioneRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        Optional<StatiPrenotazioneDTO> result = statiPrenotazioneService.partialUpdate(statiPrenotazioneDTO);

        return ResponseUtil.wrapOrNotFound(
            result,
            HeaderUtil.createEntityUpdateAlert(applicationName, false, ENTITY_NAME, statiPrenotazioneDTO.getId().toString())
        );
    }

    /**
     * {@code GET  /stati-prenotaziones} : get all the statiPrenotaziones.
     *
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list of statiPrenotaziones in body.
     */
    @GetMapping("")
    public List<StatiPrenotazioneDTO> getAllStatiPrenotaziones() {
        LOG.debug("REST request to get all StatiPrenotaziones");
        return statiPrenotazioneService.findAll();
    }

    /**
     * {@code GET  /stati-prenotaziones/:id} : get the "id" statiPrenotazione.
     *
     * @param id the id of the statiPrenotazioneDTO to retrieve.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the statiPrenotazioneDTO, or with status {@code 404 (Not Found)}.
     */
    @GetMapping("/{id}")
    public ResponseEntity<StatiPrenotazioneDTO> getStatiPrenotazione(@PathVariable("id") UUID id) {
        LOG.debug("REST request to get StatiPrenotazione : {}", id);
        Optional<StatiPrenotazioneDTO> statiPrenotazioneDTO = statiPrenotazioneService.findOne(id);
        return ResponseUtil.wrapOrNotFound(statiPrenotazioneDTO);
    }

    /**
     * {@code DELETE  /stati-prenotaziones/:id} : delete the "id" statiPrenotazione.
     *
     * @param id the id of the statiPrenotazioneDTO to delete.
     * @return the {@link ResponseEntity} with status {@code 204 (NO_CONTENT)}.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteStatiPrenotazione(@PathVariable("id") UUID id) {
        LOG.debug("REST request to delete StatiPrenotazione : {}", id);
        statiPrenotazioneService.delete(id);
        return ResponseEntity.noContent()
            .headers(HeaderUtil.createEntityDeletionAlert(applicationName, false, ENTITY_NAME, id.toString()))
            .build();
    }
}
