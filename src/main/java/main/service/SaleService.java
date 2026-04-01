package main.service;

import jakarta.persistence.EntityNotFoundException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import main.domain.Sale;
import main.repository.SaleRepository;
import main.service.dto.SaleDTO;
import main.service.mapper.SaleMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class SaleService {

    private static final Logger LOG = LoggerFactory.getLogger(SaleService.class);

    private final SaleRepository saleRepository;
    private final SaleMapper saleMapper;

    public SaleService(SaleRepository saleRepository, SaleMapper saleMapper) {
        this.saleRepository = saleRepository;
        this.saleMapper = saleMapper;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Letture
    // ─────────────────────────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public List<SaleDTO> findAllFreeSales(LocalDate data, LocalTime inizio, LocalTime fine, Integer capienza) {
        LOG.debug("Ricerca sale libere {} {}-{} capienza={}", data, inizio, fine, capienza);
        return saleRepository.findFreeSales(data, inizio, fine, capienza).stream().map(saleMapper::toDto).toList();
    }

    @Transactional(readOnly = true)
    public List<SaleDTO> findAll() {
        return saleRepository.findAllCached().stream().map(saleMapper::toDto).toList();
    }

    @Transactional(readOnly = true)
    public Page<SaleDTO> findAll(Pageable pageable) {
        // Le paginate non sono cacheable (parametri dinamici) → DB diretto
        return saleRepository.findAll(pageable).map(saleMapper::toDto);
    }

    @Transactional(readOnly = true)
    public Optional<SaleDTO> findOne(UUID id) {
        // La cache L2 su Sale.class (in CacheConfiguration) copre findById
        return saleRepository.findById(id).map(saleMapper::toDto);
    }

    @CacheEvict(value = "sale-list", allEntries = true)
    public SaleDTO save(SaleDTO saleDTO) {
        LOG.debug("Salvataggio nuova sala: {}", saleDTO);
        Sale sale = saleMapper.toEntity(saleDTO);
        sale = saleRepository.save(sale);
        return saleMapper.toDto(sale);
    }

    @CacheEvict(value = "sale-list", allEntries = true)
    public SaleDTO update(SaleDTO saleDTO) {
        LOG.debug("Aggiornamento sala: {}", saleDTO);
        saleRepository.findById(saleDTO.getId()).orElseThrow(() -> new EntityNotFoundException("Sala non trovata: " + saleDTO.getId()));
        Sale sale = saleMapper.toEntity(saleDTO);
        sale = saleRepository.save(sale);
        return saleMapper.toDto(sale);
    }

    @CacheEvict(value = "sale-list", allEntries = true)
    public void delete(UUID id) {
        LOG.debug("Eliminazione sala: {}", id);
        saleRepository.deleteById(id);
    }
}
