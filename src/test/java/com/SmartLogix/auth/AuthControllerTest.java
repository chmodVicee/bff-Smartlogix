package com.SmartLogix.auth;

import com.SmartLogix.security.JwtService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    @Mock
    private JwtService jwtService;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private AuthController authController;

    @Test
    void testLoginSuccess() {
        AuthController.LoginRequest request = new AuthController.LoginRequest("admin", "admin123");
        when(passwordEncoder.matches("admin123", "$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy")).thenReturn(true);
        when(jwtService.generate("admin")).thenReturn("mock-token");

        ResponseEntity<?> response = authController.login(request);

        assertEquals(200, response.getStatusCode().value());
        assertTrue(response.getBody() instanceof Map);
        Map<?, ?> body = (Map<?, ?>) response.getBody();
        assertEquals("mock-token", body.get("accessToken"));
        assertEquals("admin", body.get("username"));

        // Test record getter methods for 100% coverage
        assertEquals("admin", request.username());
        assertEquals("admin123", request.password());
    }

    @Test
    void testLoginInvalidPassword() {
        AuthController.LoginRequest request = new AuthController.LoginRequest("admin", "wrong-password");
        when(passwordEncoder.matches("wrong-password", "$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy")).thenReturn(false);

        ResponseEntity<?> response = authController.login(request);

        assertEquals(401, response.getStatusCode().value());
        assertTrue(response.getBody() instanceof Map);
        Map<?, ?> body = (Map<?, ?>) response.getBody();
        assertEquals("Usuario o contraseña incorrectos", body.get("error"));
    }

    @Test
    void testLoginUserNotFound() {
        AuthController.LoginRequest request = new AuthController.LoginRequest("unknown-user", "password");

        ResponseEntity<?> response = authController.login(request);

        assertEquals(401, response.getStatusCode().value());
        assertTrue(response.getBody() instanceof Map);
        Map<?, ?> body = (Map<?, ?>) response.getBody();
        assertEquals("Usuario o contraseña incorrectos", body.get("error"));
    }
}