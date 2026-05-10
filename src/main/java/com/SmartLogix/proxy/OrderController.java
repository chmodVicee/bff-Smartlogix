package com.SmartLogix.proxy;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;

/**
 * Proxy hacia el Microservicio de Orders (puerto 8082).
 *
 * Rutas expuestas por el BFF → ruta real en el MS:
 *   POST /api/orders/place-order  →  POST http://localhost:8082/place-order
 *   GET  /api/orders/all          →  GET  http://localhost:8082/all
 */
@RestController
@RequestMapping("/api/orders")
@Slf4j
public class OrderController {

    @Value("${app.microservices.orders}")
    private String ordersUrl;   // http://localhost:8082

    private final RestTemplate restTemplate = new RestTemplate();

    // POST /api/orders/place-order
    @PostMapping("/place-order")
    public ResponseEntity<String> createOrder(@RequestBody String body) {
        return forward(HttpMethod.POST, "/place-order", body);
    }

    // GET /api/orders/all
    @GetMapping("/all")
    public ResponseEntity<String> getAllOrders() {
        return forward(HttpMethod.GET, "/all", null);
    }

    // ── reenvío interno ────────────────────────────────────────────────────

    private ResponseEntity<String> forward(HttpMethod method, String path, String body) {
        String url = ordersUrl + path;
        log.info("Orders proxy: {} {}", method, url);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        try {
            return restTemplate.exchange(url, method, new HttpEntity<>(body, headers), String.class);
        } catch (HttpClientErrorException | HttpServerErrorException e) {
            return ResponseEntity.status(e.getStatusCode()).body(e.getResponseBodyAsString());
        } catch (Exception e) {
            log.error("Error llamando a Orders MS: {}", e.getMessage());
            return ResponseEntity.status(503)
                    .body("{\"error\":\"Servicio de pedidos no disponible\"}");
        }
    }
}