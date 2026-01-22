package main.web.rest;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;
import main.service.SaleService;
import main.service.dto.SaleDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/sales")
public class SaleResource {

    private static final Logger LOG = LoggerFactory.getLogger(SaleResource.class);
    private final SaleService saleService;

    public SaleResource(SaleService saleService) {
        this.saleService = saleService;
    }

    @PostMapping("")
    public ResponseEntity<SaleDTO> createSale(@RequestBody SaleDTO saleDTO) {
        SaleDTO result = saleService.save(saleDTO);
        return ResponseEntity.ok().body(result);
    }

    @PutMapping("/{id}")
    public ResponseEntity<SaleDTO> updateSale(@PathVariable(value = "id") final UUID id, @RequestBody SaleDTO saleDTO) {
        saleDTO.setId(id);
        SaleDTO result = saleService.update(saleDTO);
        return ResponseEntity.ok().body(result);
    }

    @GetMapping("")
    public ResponseEntity<List<SaleDTO>> getAllSales() {
        return ResponseEntity.ok().body(saleService.findAll(Pageable.unpaged()).getContent());
    }

    @GetMapping("/disponibili")
    public ResponseEntity<List<SaleDTO>> getAllFreeSales(
        @RequestParam LocalDate data,
        @RequestParam LocalTime inizio,
        @RequestParam LocalTime fine
    ) {
        List<SaleDTO> result = saleService.findAllFreeSales(data, inizio, fine);
        return ResponseEntity.ok().body(result);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteSale(@PathVariable("id") UUID id) {
        saleService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
