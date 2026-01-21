package main.web.rest;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import main.repository.EventiRepository;
import main.service.EventiService;
import main.service.dto.EventiDTO;
import main.web.rest.errors.BadRequestAlertException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import tech.jhipster.web.util.HeaderUtil;
import tech.jhipster.web.util.PaginationUtil;
import tech.jhipster.web.util.ResponseUtil;

/**
 * REST controller for managing {@link main.domain.Eventi}.
 */
@RestController
@RequestMapping("/api/eventis")
public class EventiResource {

    private static final Logger LOG = LoggerFactory.getLogger(EventiResource.class);

    private static final String ENTITY_NAME = "eventi";

    @Value("${jhipster.clientApp.name}")
    private String applicationName;

    private final EventiService eventiService;

    private final EventiRepository eventiRepository;

    public EventiResource(EventiService eventiService, EventiRepository eventiRepository) {
        this.eventiService = eventiService;
        this.eventiRepository = eventiRepository;
    }

    /**
     * {@code POST  /eventis} : Create a new eventi.
     *
     * @param eventiDTO the eventiDTO to create.
     * @return the {@link ResponseEntity} with status {@code 201 (Created)} and with body the new eventiDTO, or with status {@code 400 (Bad Request)} if the eventi has already an ID.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PostMapping("")
    public ResponseEntity<EventiDTO> createEventi(@Valid @RequestBody EventiDTO eventiDTO) throws URISyntaxException {
        LOG.debug("REST request to save Eventi : {}", eventiDTO);
        if (eventiDTO.getId() != null) {
            throw new BadRequestAlertException("A new eventi cannot already have an ID", ENTITY_NAME, "idexists");
        }
        eventiDTO = eventiService.save(eventiDTO);
        return ResponseEntity.created(new URI("/api/eventis/" + eventiDTO.getId()))
            .headers(HeaderUtil.createEntityCreationAlert(applicationName, false, ENTITY_NAME, eventiDTO.getId().toString()))
            .body(eventiDTO);
    }

    /**
     * {@code PUT  /eventis/:id} : Updates an existing eventi.
     *
     * @param id the id of the eventiDTO to save.
     * @param eventiDTO the eventiDTO to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated eventiDTO,
     * or with status {@code 400 (Bad Request)} if the eventiDTO is not valid,
     * or with status {@code 500 (Internal Server Error)} if the eventiDTO couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PutMapping("/{id}")
    public ResponseEntity<EventiDTO> updateEventi(
        @PathVariable(value = "id", required = false) final UUID id,
        @Valid @RequestBody EventiDTO eventiDTO
    ) throws URISyntaxException {
        LOG.debug("REST request to update Eventi : {}, {}", id, eventiDTO);
        if (eventiDTO.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, eventiDTO.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!eventiRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        eventiDTO = eventiService.update(eventiDTO);
        return ResponseEntity.ok()
            .headers(HeaderUtil.createEntityUpdateAlert(applicationName, false, ENTITY_NAME, eventiDTO.getId().toString()))
            .body(eventiDTO);
    }

    /**
     * {@code PATCH  /eventis/:id} : Partial updates given fields of an existing eventi, field will ignore if it is null
     *
     * @param id the id of the eventiDTO to save.
     * @param eventiDTO the eventiDTO to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated eventiDTO,
     * or with status {@code 400 (Bad Request)} if the eventiDTO is not valid,
     * or with status {@code 404 (Not Found)} if the eventiDTO is not found,
     * or with status {@code 500 (Internal Server Error)} if the eventiDTO couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PatchMapping(value = "/{id}", consumes = { "application/json", "application/merge-patch+json" })
    public ResponseEntity<EventiDTO> partialUpdateEventi(
        @PathVariable(value = "id", required = false) final UUID id,
        @NotNull @RequestBody EventiDTO eventiDTO
    ) throws URISyntaxException {
        LOG.debug("REST request to partial update Eventi partially : {}, {}", id, eventiDTO);
        if (eventiDTO.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, eventiDTO.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!eventiRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        Optional<EventiDTO> result = eventiService.partialUpdate(eventiDTO);

        return ResponseUtil.wrapOrNotFound(
            result,
            HeaderUtil.createEntityUpdateAlert(applicationName, false, ENTITY_NAME, eventiDTO.getId().toString())
        );
    }

    /**
     * {@code GET  /eventis} : get all the eventis.
     *
     * @param pageable the pagination information.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list of eventis in body.
     */
    @GetMapping("")
    public ResponseEntity<List<EventiDTO>> getAllEventis(@org.springdoc.core.annotations.ParameterObject Pageable pageable) {
        LOG.debug("REST request to get a page of Eventis");
        Page<EventiDTO> page = eventiService.findAll(pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(ServletUriComponentsBuilder.fromCurrentRequest(), page);
        return ResponseEntity.ok().headers(headers).body(page.getContent());
    }

    /**
     * {@code GET  /eventis/:id} : get the "id" eventi.
     *
     * @param id the id of the eventiDTO to retrieve.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the eventiDTO, or with status {@code 404 (Not Found)}.
     */
    @GetMapping("/{id}")
    public ResponseEntity<EventiDTO> getEventi(@PathVariable("id") UUID id) {
        LOG.debug("REST request to get Eventi : {}", id);
        Optional<EventiDTO> eventiDTO = eventiService.findOne(id);
        return ResponseUtil.wrapOrNotFound(eventiDTO);
    }

    /**
     * {@code DELETE  /eventis/:id} : delete the "id" eventi.
     *
     * @param id the id of the eventiDTO to delete.
     * @return the {@link ResponseEntity} with status {@code 204 (NO_CONTENT)}.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteEventi(@PathVariable("id") UUID id) {
        LOG.debug("REST request to delete Eventi : {}", id);
        eventiService.delete(id);
        return ResponseEntity.noContent()
            .headers(HeaderUtil.createEntityDeletionAlert(applicationName, false, ENTITY_NAME, id.toString()))
            .build();
    }
}
