package main.service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import main.domain.Sale;
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

    public SaleService(SaleRepository saleRepository, SaleMapper saleMapper) {
        this.saleRepository = saleRepository;
        this.saleMapper = saleMapper;
    }

    /**
     * US2 – Visualizza sale disponibili in una data e fascia oraria.
     */
    @Transactional(readOnly = true)
    public List<SaleDTO> findAllFreeSales(LocalDate data, LocalTime inizio, LocalTime fine) {
        LOG.debug("Request to get free sales for {} from {} to {}", data, inizio, fine);
        return saleRepository.findFreeSales(data, inizio, fine).stream().map(saleMapper::toDto).collect(Collectors.toList());
    }

    public SaleDTO save(SaleDTO saleDTO) {
        Sale sale = saleMapper.toEntity(saleDTO);
        sale = saleRepository.save(sale);
        return saleMapper.toDto(sale);
    }

    public SaleDTO update(SaleDTO saleDTO) {
        Sale sale = saleMapper.toEntity(saleDTO);
        sale = saleRepository.save(sale);
        return saleMapper.toDto(sale);
    }

    @Transactional(readOnly = true)
    public Page<SaleDTO> findAll(Pageable pageable) {
        return saleRepository.findAll(pageable).map(saleMapper::toDto);
    }

    public void delete(UUID id) {
        saleRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public Optional<SaleDTO> findOne(UUID id) {
        return saleRepository.findById(id).map(saleMapper::toDto);
    }
}
