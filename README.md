# SmartLogix - Backend For Frontend (BFF) & API Gateway

**SmartLogix** es una Plataforma Inteligente para la Gestión Logística de eCommerce diseñada para empoderar a las PYMEs, permitiéndoles gestionar sus operaciones logísticas con mayor eficiencia y reducir costos operativos. 

La solución está construida bajo una arquitectura de **microservicios altamente escalable** y desacoplada, dividida en un frontend moderno y flexible, y un backend seguro. Este repositorio corresponde a la capa **BFF (Backend For Frontend)**, que actúa de manera unificada como el **API Gateway** y el orquestador de seguridad de la plataforma.

---

## Arquitectura y Rol del BFF

Dentro del ecosistema de SmartLogix, que contempla 3 módulos principales (Gestión de Inventario, Procesamiento de Pedidos y Coordinación de Envíos), el **BFF** desempeña un rol crítico:

1. **Punto de Entrada Único (API Gateway)**: Gestiona y enruta todas las peticiones desde la capa de Frontend hacia los distintos microservicios subyacentes (Inventory MS en el puerto `8081` y Orders MS en el puerto `8082`).
2. **Separación de Responsabilidades**: Evita que el cliente (Frontend) tenga que conocer la topología de la red interna o interactuar directamente con múltiples microservicios.
3. **Capa de Seguridad Centralizada**: Maneja la autenticación y la validación de tokens JWT (`JwtFilter`), garantizando que ninguna petición no autorizada llegue a la capa de dominio de los microservicios.
4. **Resiliencia y Enrutamiento**: Facilita la implementación de patrones como *Circuit Breaker* (manejando fallos en la comunicación con los microservicios mediante `RestTemplate` y capturando excepciones HTTP).

## Patrones de Diseño y Tecnologías

El proyecto se alinea con prácticas modernas de desarrollo de software y adopta patrones arquitectónicos clave:

- **API Gateway / BFF Pattern**: Orquestación y proxying de peticiones.
- **Repository Pattern & Factory Method**: Aplicados en los microservicios internos para la persistencia y creación de instancias.
- **Resiliencia (Circuit Breaker)**: Manejo de errores controlados cuando un microservicio de backend no está disponible (`503 Service Unavailable`).
- **Tecnologías Core**: 
  - Java 21
  - Spring Boot (Web, Security)
  - JSON Web Tokens (JWT) para autenticación sin estado (Stateless).

---

## Comprobar el funcionamiento del backend + base de datos

Para levantar el BFF localmente y probar la interconexión con los microservicios (asegúrate de que los microservicios de Inventory y Orders también estén en ejecución si deseas el flujo completo):

### 1. Ejecutar la aplicación
Desde tu IDE de preferencia o mediante la terminal en la raíz del proyecto:

```bash
./mvnw spring-boot:run
```
El BFF se levantará por defecto en el puerto `8080`.

### 2. Pruebas de integración vía Postman

Dado que el BFF está asegurado con JWT, deberás obtener un token primero (o deshabilitar la seguridad para pruebas locales en `SecurityConfig`). Suponiendo un flujo estándar de inventario:

**A. Autenticación (Obtener Token)**
- **POST:** `http://localhost:8080/api/auth/login`
- **Body (JSON):**
  ```json
  {
      "username": "admin",
      "password": "admin123"
  }
  ```
- *Copia el `accessToken` devuelto para usarlo como Bearer Token en las siguientes peticiones.*

**B. Agregar producto al inventario**
- **POST:** `http://localhost:8080/api/inventory/add`
- **Headers:** `Authorization: Bearer <tu_token_aqui>`
- **Body (JSON):**
  ```json
  {
      "productoCodigo":"",
      "almacenCodigo":"",
      "stock":{number}
  }
  ```
Una vez que el BFF rutee la petición al Microservicio de Inventario y este responda con un `201 Created`, puedes proceder a verificarlo.

**C. Consultar el inventario total**
- **GET:** `http://localhost:8080/api/inventory/all`
- **Headers:** `Authorization: Bearer <tu_token_aqui>`

Este método `GET` devolverá un arreglo con todos los registros de inventario (y sus respectivos niveles de stock multisucursal) consolidados en la base de datos a través del microservicio de Inventario.