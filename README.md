# BFF (Backend For Frontend) — SmartLogix

Capa de API Gateway que actúa como punto de entrada único para el frontend. Centraliza la autenticación JWT, enruta las peticiones hacia los microservicios internos y protege la topología de la red interna.

## Datos técnicos

| Campo | Valor |
|---|---|
| Puerto | `8080` |
| Autenticación | JWT propio (sin base de datos, usuarios hardcodeados) |
| Microservicio Inventory | `http://localhost:8082` |
| Microservicio Orders | `http://localhost:8081` |

## Usuarios disponibles

| Usuario | Contraseña |
|---|---|
| `admin` | `admin123` |
| `gestor` | `gestor123` |

## Endpoints

### Autenticación (público)

| Método | Ruta | Descripción |
|---|---|---|
| POST | `/api/auth/login` | Obtener token JWT |

### Proxy Inventory (requieren JWT)

| Método | Ruta BFF | Redirige a Inventory MS |
|---|---|---|
| GET | `/api/inventory/all` | `GET /api/inventory/all` |
| GET | `/api/inventory/{productoCodigo}/{almacenCodigo}` | `GET /api/inventory/{productoCodigo}/{almacenCodigo}` |
| POST | `/api/inventory/add` | `POST /api/inventory/add` |
| POST | `/api/inventory/bulk-add` | `POST /api/inventory/bulk-add` |
| POST | `/api/inventory/update` | `POST /api/inventory/update` |

### Proxy Orders (requieren JWT)

| Método | Ruta BFF | Redirige a Orders MS |
|---|---|---|
| POST | `/api/orders/place-order` | `POST /api/orders/place-order` |
| GET | `/api/orders/all` | `GET /api/orders/all` |

---

## Pruebas en Postman

### 1. Login (obtener token)

```
POST http://localhost:8080/api/auth/login
Content-Type: application/json

{
  "username": "admin",
  "password": "admin123"
}
```

**Respuesta esperada (200):**
```json
{
  "accessToken": "eyJhbGciOiJIUzI1NiJ9...",
  "username": "admin"
}
```

> Copia el `accessToken`. Se usa como `Bearer Token` en todas las peticiones siguientes.

---

### 2. Listar inventario (via BFF)

```
GET http://localhost:8080/api/inventory/all
Authorization: Bearer <token>
```

---

### 3. Agregar producto al inventario (via BFF)

```
POST http://localhost:8080/api/inventory/add
Authorization: Bearer <token>
Content-Type: application/json

{
  "productoCodigo": "PROD-001",
  "almacenCodigo": "ALM-A",
  "stock": 100
}
```

---

### 4. Crear pedido (via BFF)

```
POST http://localhost:8080/api/orders/place-order
Authorization: Bearer <token>
Content-Type: application/json

{
  "numeroPedido": "PED-ABC123",
  "productoCodigo": "PROD-001",
  "almacenCodigo": "ALM-A",
  "cantidad": 3
}
```

---

### 5. Listar pedidos (via BFF)

```
GET http://localhost:8080/api/orders/all
Authorization: Bearer <token>
```

---

### Comportamiento ante fallos

Si un microservicio no está disponible, el BFF retorna:

```json
{ "error": "Servicio de inventario no disponible" }
```

con código `503 Service Unavailable`, sin exponer detalles internos al frontend.

---

## Cómo levantar

```bash
./mvnw spring-boot:run
```

No requiere base de datos. Los microservicios de Inventory (`8082`) y Orders (`8081`) deben estar corriendo para que el proxy funcione.
