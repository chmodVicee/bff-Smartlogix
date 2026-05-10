package com.SmartLogix.proxy;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;

/**
 * Proxy hacia el Microservicio de Inventory (puerto 8081).
 *
 * Rutas expuestas por el BFF → ruta real en el MS:
 *   GET  /api/inventory/all                              →  GET  http://localhost:8081/all
 *   GET  /api/inventory/{productoCodigo}/{almacenCodigo} →  GET  http://localhost:8081/{productoCodigo}/{almacenCodigo}
 *   GET  /api/inventory/total/{productoCodigo}           →  GET  http://localhost:8081/total/{productoCodigo}
 *   POST /api/inventory/add                              →  POST http://localhost:8081/add
 *   POST /api/inventory/bulk-add                         →  POST http://localhost:8081/bulk-add
 *   POST /api/inventory/update                           →  POST http://localhost:8081/update
 */
@RestController
@RequestMapping("/api/inventory")
@Slf4j
public class InventoryController {

    @Value("${app.microservices.inventory}")
    private String inventoryUrl;   // http://localhost:8081

    private final RestTemplate restTemplate = new RestTemplate();

    // GET /api/inventory/all
    @GetMapping("/all")
    public ResponseEntity<String> getAllStock() {
        return forward(HttpMethod.GET, "/all", null);
    }

    // GET /api/inventory/{productoCodigo}/{almacenCodigo}
    @GetMapping("/{productoCodigo}/{almacenCodigo}")
    public ResponseEntity<String> getStock(@PathVariable String productoCodigo,
                                           @PathVariable String almacenCodigo) {
        return forward(HttpMethod.GET, "/" + productoCodigo + "/" + almacenCodigo, null);
    }

    // GET /api/inventory/total/{productoCodigo}
    @GetMapping("/total/{productoCodigo}")
    public ResponseEntity<String> getTotalStock(@PathVariable String productoCodigo) {
        return forward(HttpMethod.GET, "/total/" + productoCodigo, null);
    }

    // POST /api/inventory/add
    @PostMapping("/add")
    public ResponseEntity<String> addProduct(@RequestBody String body) {
        return forward(HttpMethod.POST, "/add", body);
    }

    // POST /api/inventory/bulk-add
    @PostMapping("/bulk-add")
    public ResponseEntity<String> addProducts(@RequestBody String body) {
        return forward(HttpMethod.POST, "/bulk-add", body);
    }

    // POST /api/inventory/update
    @PostMapping("/update")
    public ResponseEntity<String> updateStock(@RequestBody String body) {
        return forward(HttpMethod.POST, "/update", body);
    }

    // ── reenvío interno ────────────────────────────────────────────────────

    private ResponseEntity<String> forward(HttpMethod method, String path, String body) {
        String url = inventoryUrl + path;
        log.info("Inventory proxy: {} {}", method, url);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        try {
            return restTemplate.exchange(url, method, new HttpEntity<>(body, headers), String.class);
        } catch (HttpClientErrorException | HttpServerErrorException e) {
            return ResponseEntity.status(e.getStatusCode()).body(e.getResponseBodyAsString());
        } catch (Exception e) {
            log.error("Error llamando a Inventory MS: {}", e.getMessage());
            return ResponseEntity.status(503)
                    .body("{\"error\":\"Servicio de inventario no disponible\"}");
        }
    }
}
