package com.SmartLogix.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;

class JwtServiceTest {

    private JwtService jwtService;

    @BeforeEach
    void setUp() {
        jwtService = new JwtService();
        // Clave secreta fuerte (mínimo 256 bits para HS256)
        ReflectionTestUtils.setField(jwtService, "secret", "this-is-a-very-strong-secret-key-for-jwt-signing");
        ReflectionTestUtils.setField(jwtService, "expirationMs", 3600000L); // 1 hora
    }

    @Test
    void testGenerateToken() {
        String token = jwtService.generate("admin");
        assertNotNull(token);
        assertFalse(token.isEmpty());
    }

    @Test
    void testGetUsername() {
        String token = jwtService.generate("admin");
        String username = jwtService.getUsername(token);
        assertEquals("admin", username);
    }

    @Test
    void testIsValidWithValidToken() {
        String token = jwtService.generate("admin");
        assertTrue(jwtService.isValid(token));
    }

    @Test
    void testIsValidWithInvalidToken() {
        assertFalse(jwtService.isValid("invalid-token"));
    }
    
    @Test
    void testIsValidWithExpiredToken() {
        ReflectionTestUtils.setField(jwtService, "expirationMs", -1000L); // -1 segundo (ya expirado)
        String expiredToken = jwtService.generate("admin");
        assertFalse(jwtService.isValid(expiredToken));
    }
    
    @Test
    void testIsValidWithMalformedToken() {
        assertFalse(jwtService.isValid("this.is.not.a.valid.token"));
    }
}