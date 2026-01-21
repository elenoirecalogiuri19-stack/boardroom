package main.service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import main.domain.Prenotazioni;
import main.domain.Sale;
import main.repository.PrenotazioniRepository; // AGGIUNTO
import main.repository.SaleRepository;
import main.service.dto.SaleDTO;
import main.service.mapper.SaleMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
    private final PrenotazioniRepository prenotazioniRepository; // AGGIUNTO

    public SaleService(SaleRepository saleRepository, SaleMapper saleMapper, PrenotazioniRepository prenotazioniRepository) {
        this.saleRepository = saleRepository;
        this.saleMapper = saleMapper;
        this.prenotazioniRepository = prenotazioniRepository; // AGGIUNTO
    }

    @Transactional(readOnly = true)
    public List<SaleDTO> findSaleLibere(LocalDate data, LocalTime inizio, LocalTime fine) {
        LOG.debug("Request to get free Sales for date: {}, from {} to {}", data, inizio, fine);

        List<Sale> tutteLeSale = saleRepository.findAll();

        List<Prenotazioni> occupate = prenotazioniRepository.findOccupiedRooms(data, inizio, fine);

        List<UUID> idSaleOccupate = occupate.stream().map(p -> p.getSala().getId()).collect(Collectors.toList());

        return tutteLeSale
            .stream()
            .filter(sala -> !idSaleOccupate.contains(sala.getId()))
            .map(saleMapper::toDto)
            .collect(Collectors.toList());
    }

    public SaleDTO save(SaleDTO saleDTO) {
        LOG.debug("Request to save Sale : {}", saleDTO);
        Sale sale = saleMapper.toEntity(saleDTO);
        sale = saleRepository.save(sale);
        return saleMapper.toDto(sale);
    }

    public SaleDTO update(SaleDTO saleDTO) {
        LOG.debug("Request to update Sale : {}", saleDTO);
        Sale sale = saleMapper.toEntity(saleDTO);
        sale = saleRepository.save(sale);
        return saleMapper.toDto(sale);
    }

    public Optional<SaleDTO> partialUpdate(SaleDTO saleDTO) {
        LOG.debug("Request to partially update Sale : {}", saleDTO);
        return saleRepository
            .findById(saleDTO.getId())
            .map(existingSale -> {
                saleMapper.partialUpdate(existingSale, saleDTO);
                return existingSale;
            })
            .map(saleRepository::save)
            .map(saleMapper::toDto);
    }

    @Transactional(readOnly = true)
    public Page<SaleDTO> findAll(Pageable pageable) {
        LOG.debug("Request to get all Sales");
        return saleRepository.findAll(pageable).map(saleMapper::toDto);
    }

    @Transactional(readOnly = true)
    public Optional<SaleDTO> findOne(UUID id) {
        LOG.debug("Request to get Sale : {}", id);
        return saleRepository.findById(id).map(saleMapper::toDto);
    }

    public void delete(UUID id) {
        LOG.debug("Request to delete Sale : {}", id);
        saleRepository.deleteById(id);
    }
}
