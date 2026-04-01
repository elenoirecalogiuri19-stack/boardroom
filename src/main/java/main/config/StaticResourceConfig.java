package main.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Configurazione per servire le immagini caricate come risorse statiche.
 *
 * Mappa:
 *   URL:        /uploads/sale/**
 *   Filesystem: ./uploads/sale/  (relativo alla directory di avvio dell'app)
 *
 * Esempio:
 *   GET /uploads/sale/uuid.jpg
 *   → legge il file da ./uploads/sale/uuid.jpg
 *
 * In produzione si può aggiungere un CDN davanti o spostare su S3
 * senza modificare il codice frontend (basta cambiare il prefisso URL).
 */
@Configuration
public class StaticResourceConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // Serve le immagini caricate dall'admin
        registry.addResourceHandler("/uploads/**").addResourceLocations("file:uploads/").setCachePeriod(3600); // cache 1 ora nel browser
    }
}
