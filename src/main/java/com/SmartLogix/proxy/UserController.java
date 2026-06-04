package com.SmartLogix.proxy;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;

/**
 * Proxy hacia el Microservicio de Users (puerto 8083).
 *
 * Rutas expuestas por el BFF → ruta real en el MS:
 *   POST /api/users/auth/register  →  POST http://localhost:8083/api/auth/register
 *   POST /api/users/auth/login     →  POST http://localhost:8083/api/auth/login
 *   GET  /api/users/profile        →  GET  http://localhost:8083/api/users/profile
 *   GET  /api/users                →  GET  http://localhost:8083/api/users
 */
@RestController
@RequestMapping("/api/users")
@Slf4j
public class UserController {

    @Value("${app.microservices.users}")
    private String usersUrl;

    private final RestTemplate restTemplate = new RestTemplate();

    // POST /api/users/auth/register
    @PostMapping("/auth/register")
    public ResponseEntity<String> register(@RequestBody String body) {
        return forward(HttpMethod.POST, "/api/auth/register", body);
    }

    // POST /api/users/auth/login
    @PostMapping("/auth/login")
    public ResponseEntity<String> login(@RequestBody String body) {
        return forward(HttpMethod.POST, "/api/auth/login", body);
    }

    // GET /api/users/profile
    @GetMapping("/profile")
    public ResponseEntity<String> getProfile(@RequestHeader("Authorization") String token) {
        return forwardWithAuth(HttpMethod.GET, "/api/users/profile", null, token);
    }

    // GET /api/users
    @GetMapping
    public ResponseEntity<String> getAllUsers(@RequestHeader("Authorization") String token) {
        return forwardWithAuth(HttpMethod.GET, "/api/users", null, token);
    }

    // ── reenvío interno ────────────────────────────────────────────────────

    private ResponseEntity<String> forward(HttpMethod method, String path, String body) {
        String url = usersUrl + path;
        log.info("Users proxy: {} {}", method, url);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        try {
            return restTemplate.exchange(url, method, new HttpEntity<>(body, headers), String.class);
        } catch (HttpClientErrorException | HttpServerErrorException e) {
            return ResponseEntity.status(e.getStatusCode()).body(e.getResponseBodyAsString());
        } catch (Exception e) {
            log.error("Error llamando a Users MS: {}", e.getMessage());
            return ResponseEntity.status(503)
                    .body("{\"error\":\"Servicio de usuarios no disponible\"}");
        }
    }

    private ResponseEntity<String> forwardWithAuth(HttpMethod method, String path, String body, String token) {
        String url = usersUrl + path;
        log.info("Users proxy (auth): {} {}", method, url);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", token);

        try {
            return restTemplate.exchange(url, method, new HttpEntity<>(body, headers), String.class);
        } catch (HttpClientErrorException | HttpServerErrorException e) {
            return ResponseEntity.status(e.getStatusCode()).body(e.getResponseBodyAsString());
        } catch (Exception e) {
            log.error("Error llamando a Users MS: {}", e.getMessage());
            return ResponseEntity.status(503)
                    .body("{\"error\":\"Servicio de usuarios no disponible\"}");
        }
    }
}