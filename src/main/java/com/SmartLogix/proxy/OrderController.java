package com.SmartLogix.proxy;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;

/**
 * Proxy hacia el Microservicio de Orders (puerto 8081).
 */
@RestController
@RequestMapping("/api/orders")
@Slf4j
public class OrderController {

    @Value("${app.microservices.orders}")
    private String ordersUrl;   // Leerá http://localhost:8081/api/orders desde el yml

    private final RestTemplate restTemplate = new RestTemplate();

    @PostMapping("/place-order")
    public ResponseEntity<String> createOrder(@RequestBody String body,
                                              @RequestHeader(value = "Authorization", required = false) String token) {
        return forwardWithAuth(HttpMethod.POST, "/place-order", body, token);
    }

    @GetMapping("/all")
    public ResponseEntity<String> getAllOrders(@RequestHeader(value = "Authorization", required = false) String token) {
        return forwardWithAuth(HttpMethod.GET, "/all", null, token);
    }


    private ResponseEntity<String> forwardWithAuth(HttpMethod method, String path, String body, String token) {
        String url = ordersUrl + path;
        log.info("Orders proxy (auth): {} {}", method, url);

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
            log.error("Error llamando a Orders MS: {}", e.getMessage());
            return ResponseEntity.status(503)
                    .body("{\"error\":\"Servicio de pedidos no disponible\"}");
        }
    }
}