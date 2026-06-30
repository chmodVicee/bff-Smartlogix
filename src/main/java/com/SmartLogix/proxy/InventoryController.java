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
 */
@RestController
@RequestMapping("/api/inventory")
@Slf4j
public class InventoryController {

    @Value("${app.microservices.inventory}")
    private String inventoryUrl;   // http://localhost:8081

    private final RestTemplate restTemplate = new RestTemplate();

    @GetMapping("/all")
    public ResponseEntity<String> getAllStock(@RequestHeader(value = "Authorization", required = false) String token) {
        return forwardWithAuth(HttpMethod.GET, "/all", null, token);
    }

    @GetMapping("/{productoCodigo}/{almacenCodigo}")
    public ResponseEntity<String> getStock(@PathVariable String productoCodigo,
                                           @PathVariable String almacenCodigo,
                                           @RequestHeader(value = "Authorization", required = false) String token) {
        return forwardWithAuth(HttpMethod.GET, "/" + productoCodigo + "/" + almacenCodigo, null, token);
    }

    @GetMapping("/total/{productoCodigo}")
    public ResponseEntity<String> getTotalStock(@PathVariable String productoCodigo,
                                                @RequestHeader(value = "Authorization", required = false) String token) {
        return forwardWithAuth(HttpMethod.GET, "/total/" + productoCodigo, null, token);
    }

    @PostMapping("/add")
    public ResponseEntity<String> addProduct(@RequestBody String body,
                                             @RequestHeader(value = "Authorization", required = false) String token) {
        return forwardWithAuth(HttpMethod.POST, "/add", body, token);
    }

    @PostMapping("/bulk-add")
    public ResponseEntity<String> addProducts(@RequestBody String body,
                                              @RequestHeader(value = "Authorization", required = false) String token) {
        return forwardWithAuth(HttpMethod.POST, "/bulk-add", body, token);
    }

    @PostMapping("/update")
    public ResponseEntity<String> updateStock(@RequestBody String body,
                                              @RequestHeader(value = "Authorization", required = false) String token) {
        return forwardWithAuth(HttpMethod.POST, "/update", body, token);
    }

    @PutMapping("/{id}")
    public ResponseEntity<String> updateById(@PathVariable Long id,
                                             @RequestBody String body,
                                             @RequestHeader(value = "Authorization", required = false) String token) {
        return forwardWithAuth(HttpMethod.PUT, "/" + id, body, token);
    }


    private ResponseEntity<String> forwardWithAuth(HttpMethod method, String path, String body, String token) {
        String url = inventoryUrl + "/api/inventory" + path;
        log.info("Inventory proxy (auth): {} {}", method, url);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        if (token != null) {
            headers.set("Authorization", token);
        }

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