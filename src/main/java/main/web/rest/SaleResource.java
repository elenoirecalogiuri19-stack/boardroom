package main.web.rest;

import java.util.List;
import java.util.UUID;
import main.domain.Sale;
import main.repository.SaleRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/sales")
public class SaleResource {

    private static final Logger LOG = LoggerFactory.getLogger(SaleResource.class);
    private final SaleRepository saleRepository;

    public SaleResource(SaleRepository saleRepository) {
        this.saleRepository = saleRepository;
    }

    @PostMapping("")
    public ResponseEntity<Sale> createSale(@RequestBody Sale sale) {
        LOG.debug("REST request to save Sale : {}", sale);
        Sale result = saleRepository.save(sale);
        return ResponseEntity.ok().body(result);
    }

    // MODIFICA (Serve per la gestione completa)
    @PutMapping("/{id}")
    public ResponseEntity<Sale> updateSale(@PathVariable(value = "id") final UUID id, @RequestBody Sale sale) {
        LOG.debug("REST request to update Sale : {}, {}", id, sale);
        sale.setId(id);
        Sale result = saleRepository.save(sale);
        return ResponseEntity.ok().body(result);
    }

    @GetMapping("")
    public ResponseEntity<List<Sale>> getAllSales() {
        LOG.debug("REST request to get all Sales");
        return ResponseEntity.ok().body(saleRepository.findAll());
    }

    @GetMapping("/disponibili")
    public ResponseEntity<List<Sale>> getAllFreeSales() {
        LOG.debug("REST request to get all free Sales for US2");
        return ResponseEntity.ok().body(saleRepository.findAllFreeSales());
    }

    // ELIMINAZIONE (Risolve il tuo errore "Not Found")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteSale(@PathVariable("id") UUID id) {
        LOG.debug("REST request to delete Sale : {}", id);
        saleRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
