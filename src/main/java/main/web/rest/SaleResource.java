package main.web.rest;

import java.util.List;
import main.domain.Sale;
import main.repository.SaleRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller per la gestione delle Sale - US2.
 */
@RestController
@RequestMapping("/api/sales")
public class SaleResource {

    private static final Logger LOG = LoggerFactory.getLogger(SaleResource.class);

    private final SaleRepository saleRepository;

    // Costruttore per l'iniezione del Repository
    public SaleResource(SaleRepository saleRepository) {
        this.saleRepository = saleRepository;
    }

    /**
     * GET  /api/sales : recupera solo le sale che non hanno prenotazioni attive (US2).
     *
     * @return la lista delle sale libere con stato 200 (OK).
     */
    @GetMapping("")
    public ResponseEntity<List<Sale>> getAllSales() {
        LOG.debug("REST request to get all free Sales for US2");

        // Chiamata alla tua query personalizzata nel Repository
        List<Sale> freeSales = saleRepository.findAllFreeSales();

        return ResponseEntity.ok().body(freeSales);
    }
}
