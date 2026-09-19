# Order Management System — Microservices Architecture

A Spring Boot microservices-based order management system demonstrating service discovery, inter-service communication, and API gateway routing — built to reflect real-world backend architecture patterns.

## Architecture

```
                        ┌──────────────────┐
                        │   Eureka Server   │
                        │   (Port 8761)     │
                        │ Service Registry  │
                        └────────┬──────────┘
                                 │ registers with
        ┌────────────────┬──────┴──────┬────────────────┐
        │                │             │                │
┌───────▼──────┐ ┌───────▼───────┐ ┌───▼──────────┐ ┌────▼─────────┐
│ User Service │ │Product Service│ │Order Service │ │ API Gateway  │
│ (Port 8081)  │ │ (Port 8082)   │ │(Port 8083)   │ │ (Port 8080)  │
│              │ │               │ │              │ │              │
│   userdb     │ │  productdb    │ │   orderdb    │ │ Routes to    │
│  (MySQL)     │ │   (MySQL)     │ │   (MySQL)    │ │ all 3 above  │
└──────────────┘ └───────▲───────┘ └──────┬───────┘ └──────────────┘
                         │                │
                         └────────────────┘
                    Feign Client calls (validates
                    user, checks/reduces stock)
```

**Client requests** enter through the API Gateway (port 8080) — the single public entry point. The gateway routes requests to the appropriate service based on path, using Eureka for service discovery instead of hardcoded URLs.

**Order Service** orchestrates the core business flow: when an order is placed, it calls User Service (via Feign) to validate the user exists, calls Product Service to check and reduce stock, then persists the order — all via HTTP, with zero hardcoded host/port values anywhere in the codebase.

## Tech Stack

- **Java 17**, **Spring Boot**
- **Spring Cloud Netflix Eureka** — service discovery
- **Spring Cloud Gateway** (WebFlux/Netty) — API gateway and routing
- **Spring Cloud OpenFeign** — declarative REST client for inter-service calls
- **Spring Data JPA + Hibernate** — ORM and database access
- **MySQL** — one database per service (database-per-service pattern)
- **Lombok** — boilerplate reduction
- **Maven** — build and dependency management

## Services

| Service | Port | Responsibility |
|---|---|---|
| `eureka-server` | 8761 | Service registry — all services register here and discover each other |
| `user-service` | 8081 | User CRUD |
| `product-service` | 8082 | Product catalog CRUD + stock management |
| `order-service` | 8083 | Order placement, orchestrates User + Product services via Feign |
| `api-gateway` | 8080 | Single entry point; routes `/api/users/**`, `/api/products/**`, `/api/orders/**` to the correct service |

## Key Design Decisions

- **Database-per-service**: each service owns its own MySQL database exclusively. No service reads another's database directly or performs cross-database joins — all cross-service data access goes through REST APIs (via Feign), keeping services genuinely independent and deployable separately.
- **DTOs for inter-service contracts**: Order Service uses lightweight DTOs (`UserDTO`, `ProductDTO`) rather than depending on the other services' full entity classes, decoupling it from their internal structure.
- **Soft-cancel over hard-delete**: orders are cancelled (status change) rather than deleted, preserving order history for auditing — reflecting real e-commerce system design rather than naive CRUD.
- **Service discovery over hardcoded URLs**: both Feign clients and the API Gateway resolve target services by name (`lb://user-service`) through Eureka, not by IP/port — enabling services to scale or relocate without config changes elsewhere.

## API Endpoints (via Gateway, port 8080)

### Users
```
POST   /api/users
GET    /api/users
GET    /api/users/{id}
PUT    /api/users/{id}
DELETE /api/users/{id}
```

### Products
```
POST   /api/products
GET    /api/products
GET    /api/products/{id}
PUT    /api/products/{id}
DELETE /api/products/{id}
PUT    /api/products/{id}/reduce-stock?quantity={n}
```

### Orders
```
POST   /api/orders                  { "userId": 1, "productId": 2, "quantity": 3 }
GET    /api/orders
GET    /api/orders/{id}
PUT    /api/orders/{id}/cancel
```

## Running Locally

**Prerequisites:** JDK 17+, Maven, MySQL running locally.

1. Clone the repo:
   ```bash
   git clone https://github.com/SSv3611u/order-management-microservices.git
   cd order-management-microservices
   ```

2. Create the databases:
   ```sql
   CREATE DATABASE userdb;
   CREATE DATABASE productdb;
   CREATE DATABASE orderdb;
   ```

3. Update each service's `src/main/resources/application.properties` with your MySQL credentials (`spring.datasource.username` / `password`).

4. Start services **in this order** (each in its own terminal/IDE run configuration):
   ```
   1. eureka-server
   2. user-service
   3. product-service
   4. order-service
   5. api-gateway
   ```

5. Confirm all services registered: open `http://localhost:8761` — you should see USER-SERVICE, PRODUCT-SERVICE, ORDER-SERVICE, and API-GATEWAY listed as UP.

6. Test through the gateway, e.g.:
   ```
   GET  http://localhost:8080/api/products
   POST http://localhost:8080/api/orders
   ```

## Known Limitations / Future Improvements

- No global exception handling yet — Feign call failures (e.g. user not found) currently surface as generic 500 errors rather than mapped HTTP status codes. A `@ControllerAdvice` in Order Service would fix this.
- No compensating transaction (Saga pattern) if an order fails to save *after* stock has already been reduced — a known trade-off for this project's scope.
- No containerization yet (Docker/docker-compose planned).
- No frontend yet (React app planned to consume the Gateway API).

## Author

Sai Saketh — built as a hands-on backend project applying Spring Boot, Spring Cloud, and microservices architecture patterns.

**Repo:** [github.com/SSv3611u/order-management-microservices](https://github.com/SSv3611u/order-management-microservices)
