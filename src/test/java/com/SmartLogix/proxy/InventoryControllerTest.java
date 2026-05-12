package com.SmartLogix.proxy;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class InventoryControllerTest {

    @Mock
    private RestTemplate restTemplate;

    @InjectMocks
    private InventoryController inventoryController;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(inventoryController, "inventoryUrl", "http://localhost:8081");
        ReflectionTestUtils.setField(inventoryController, "restTemplate", restTemplate);
    }

    @Test
    void testGetAllStockSuccess() {
        ResponseEntity<String> expectedResponse = new ResponseEntity<>("[{\"id\":1}]", HttpStatus.OK);
        when(restTemplate.exchange(eq("http://localhost:8081/all"), eq(HttpMethod.GET), any(HttpEntity.class), eq(String.class)))
                .thenReturn(expectedResponse);

        ResponseEntity<String> response = inventoryController.getAllStock();
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("[{\"id\":1}]", response.getBody());
    }

    @Test
    void testGetStockSuccess() {
        ResponseEntity<String> expectedResponse = new ResponseEntity<>("{\"stock\":10}", HttpStatus.OK);
        when(restTemplate.exchange(eq("http://localhost:8081/prod1/alm1"), eq(HttpMethod.GET), any(HttpEntity.class), eq(String.class)))
                .thenReturn(expectedResponse);

        ResponseEntity<String> response = inventoryController.getStock("prod1", "alm1");
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("{\"stock\":10}", response.getBody());
    }

    @Test
    void testGetTotalStockSuccess() {
        ResponseEntity<String> expectedResponse = new ResponseEntity<>("{\"total\":50}", HttpStatus.OK);
        when(restTemplate.exchange(eq("http://localhost:8081/total/prod1"), eq(HttpMethod.GET), any(HttpEntity.class), eq(String.class)))
                .thenReturn(expectedResponse);

        ResponseEntity<String> response = inventoryController.getTotalStock("prod1");
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("{\"total\":50}", response.getBody());
    }

    @Test
    void testAddProductSuccess() {
        ResponseEntity<String> expectedResponse = new ResponseEntity<>("Product added", HttpStatus.CREATED);
        when(restTemplate.exchange(eq("http://localhost:8081/add"), eq(HttpMethod.POST), any(HttpEntity.class), eq(String.class)))
                .thenReturn(expectedResponse);

        ResponseEntity<String> response = inventoryController.addProduct("{\"name\":\"Product 1\"}");
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals("Product added", response.getBody());
    }

    @Test
    void testAddProductsSuccess() {
        ResponseEntity<String> expectedResponse = new ResponseEntity<>("Products added", HttpStatus.CREATED);
        when(restTemplate.exchange(eq("http://localhost:8081/bulk-add"), eq(HttpMethod.POST), any(HttpEntity.class), eq(String.class)))
                .thenReturn(expectedResponse);

        ResponseEntity<String> response = inventoryController.addProducts("[{\"name\":\"Product 1\"}]");
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals("Products added", response.getBody());
    }

    @Test
    void testUpdateStockSuccess() {
        ResponseEntity<String> expectedResponse = new ResponseEntity<>("Stock updated", HttpStatus.OK);
        when(restTemplate.exchange(eq("http://localhost:8081/update"), eq(HttpMethod.POST), any(HttpEntity.class), eq(String.class)))
                .thenReturn(expectedResponse);

        ResponseEntity<String> response = inventoryController.updateStock("{\"id\":1, \"stock\":20}");
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Stock updated", response.getBody());
    }

    @Test
    void testHttpClientErrorException() {
        when(restTemplate.exchange(eq("http://localhost:8081/all"), eq(HttpMethod.GET), any(HttpEntity.class), eq(String.class)))
                .thenThrow(new HttpClientErrorException(HttpStatus.NOT_FOUND, "Not Found", "Not Found Body".getBytes(), null));

        ResponseEntity<String> response = inventoryController.getAllStock();
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals("Not Found Body", response.getBody());
    }

    @Test
    void testHttpServerErrorException() {
        when(restTemplate.exchange(eq("http://localhost:8081/all"), eq(HttpMethod.GET), any(HttpEntity.class), eq(String.class)))
                .thenThrow(new HttpServerErrorException(HttpStatus.INTERNAL_SERVER_ERROR, "Server Error", "Server Error Body".getBytes(), null));

        ResponseEntity<String> response = inventoryController.getAllStock();
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertEquals("Server Error Body", response.getBody());
    }

    @Test
    void testGenericException() {
        when(restTemplate.exchange(eq("http://localhost:8081/all"), eq(HttpMethod.GET), any(HttpEntity.class), eq(String.class)))
                .thenThrow(new RuntimeException("Connection refused"));

        ResponseEntity<String> response = inventoryController.getAllStock();
        assertEquals(HttpStatus.SERVICE_UNAVAILABLE, response.getStatusCode());
        assertEquals("{\"error\":\"Servicio de inventario no disponible\"}", response.getBody());
    }
}