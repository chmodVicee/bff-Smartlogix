package com.SmartLogix.auth;

import com.SmartLogix.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;

    /*
     * Usuarios de SmartLogix hardcodeados.
     * El caso no pide microservicio de usuarios, así que se gestionan aquí.
     * Si en el futuro necesitas más usuarios, puedes agregar más entradas al Map
     * o conectar a una base de datos H2 en el mismo BFF.
     *
     * Contraseñas en BCrypt — para generar una nueva:
     *   new BCryptPasswordEncoder().encode("tu-contraseña")
     *
     * admin  → password: admin123
     * gestor → password: gestor123
     */
    private static final Map<String, String> USUARIOS = Map.of(
            "admin",  "$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy",
            "gestor", "$2a$10$8K1p/a0dR1xqM2LtHxPkT.7VNvFMkFv9BFu0YQxEZvAoJ1V9yZ.Aq"
    );

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest body) {
        String hash = USUARIOS.get(body.username());

        if (hash == null || !passwordEncoder.matches(body.password(), hash)) {
            return ResponseEntity.status(401)
                    .body(Map.of("error", "Usuario o contraseña incorrectos"));
        }

        String token = jwtService.generate(body.username());
        return ResponseEntity.ok(Map.of(
                "accessToken", token,
                "username", body.username()
        ));
    }

    public record LoginRequest(String username, String password) {}
}
