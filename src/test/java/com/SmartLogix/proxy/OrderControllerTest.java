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
class OrderControllerTest {

    @Mock
    private RestTemplate restTemplate;

    @InjectMocks
    private OrderController orderController;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(orderController, "ordersUrl", "http://localhost:8082");
        ReflectionTestUtils.setField(orderController, "restTemplate", restTemplate);
    }

    @Test
    void testGetAllOrdersSuccess() {
        ResponseEntity<String> expectedResponse = new ResponseEntity<>("[{\"id\":1}]", HttpStatus.OK);
        when(restTemplate.exchange(eq("http://localhost:8082/all"), eq(HttpMethod.GET), any(HttpEntity.class), eq(String.class)))
                .thenReturn(expectedResponse);

        ResponseEntity<String> response = orderController.getAllOrders();
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("[{\"id\":1}]", response.getBody());
    }

    @Test
    void testCreateOrderSuccess() {
        ResponseEntity<String> expectedResponse = new ResponseEntity<>("Order created", HttpStatus.CREATED);
        when(restTemplate.exchange(eq("http://localhost:8082/place-order"), eq(HttpMethod.POST), any(HttpEntity.class), eq(String.class)))
                .thenReturn(expectedResponse);

        ResponseEntity<String> response = orderController.createOrder("{\"item\":\"item1\"}");
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals("Order created", response.getBody());
    }

    @Test
    void testHttpClientErrorException() {
        when(restTemplate.exchange(eq("http://localhost:8082/all"), eq(HttpMethod.GET), any(HttpEntity.class), eq(String.class)))
                .thenThrow(new HttpClientErrorException(HttpStatus.BAD_REQUEST, "Bad Request", "Bad Request Body".getBytes(), null));

        ResponseEntity<String> response = orderController.getAllOrders();
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Bad Request Body", response.getBody());
    }
    
    @Test
    void testHttpServerErrorException() {
        when(restTemplate.exchange(eq("http://localhost:8082/all"), eq(HttpMethod.GET), any(HttpEntity.class), eq(String.class)))
                .thenThrow(new HttpServerErrorException(HttpStatus.INTERNAL_SERVER_ERROR, "Server Error", "Server Error Body".getBytes(), null));

        ResponseEntity<String> response = orderController.getAllOrders();
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertEquals("Server Error Body", response.getBody());
    }

    @Test
    void testGenericException() {
        when(restTemplate.exchange(eq("http://localhost:8082/all"), eq(HttpMethod.GET), any(HttpEntity.class), eq(String.class)))
                .thenThrow(new RuntimeException("Connection refused"));

        ResponseEntity<String> response = orderController.getAllOrders();
        assertEquals(HttpStatus.SERVICE_UNAVAILABLE, response.getStatusCode());
        assertEquals("{\"error\":\"Servicio de pedidos no disponible\"}", response.getBody());
    }
}