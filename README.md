# API-ERP

![Java](https://img.shields.io/badge/Java-17-orange)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.5-brightgreen)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-16-blue)
![Redis](https://img.shields.io/badge/Redis-7-red)
![Kafka](https://img.shields.io/badge/Kafka-KRaft-black)
![Tests](https://img.shields.io/badge/tests-JUnit%205%20%2B%20Mockito-brightgreen)
![CI/CD](https://github.com/devguilhrm/API-Manager/actions/workflows/ci.yml/badge.svg)

REST API for CRM and sales operations built with Spring Boot. Supports multi-role workflows (managers and sellers), JWT authentication with refresh-token rotation, Redis-backed security controls, asynchronous Kafka events, and horizontal scaling behind an Nginx load balancer.

---

## Table of contents

- [Architecture](#architecture)
- [Features](#features)
- [Tech stack](#tech-stack)
- [Getting started](#getting-started)
- [Running with Docker Compose](#running-with-docker-compose)
- [Horizontal scaling](#horizontal-scaling)
- [CI/CD](#cicd)
- [API reference](#api-reference)
- [Configuration](#configuration)
- [Tests](#tests)
- [Contributing](#contributing)

---

## Architecture

```
client → nginx:8080 → api-1 / api-2 / api-3 → PostgreSQL / Redis / Kafka
```

The API layer is fully stateless. JWT access tokens carry identity; refresh tokens are persisted in PostgreSQL; token blacklist, login rate limits, and cache entries live in Redis. This makes the `api` service safe to run as multiple replicas behind Nginx without any session affinity.

```
src/main/java/com/devguilhrm/API_ERP/
├── auth/           Login, users, roles, and authentication services
├── clients/        Client lifecycle, ownership, and reassignment
├── common/         Shared entities, enums, and response contracts
├── config/         Cache configuration and dev data initializer
├── dashboard/      Managerial dashboard and revenue indicators
├── exception/      Domain exceptions and global exception handler
├── manager/        Manager-specific use cases
├── product/        Product catalog and stock information
├── refreshToken/   Refresh-token persistence, validation, and rotation
├── sale/           Sales, sale items, state transitions, and events
├── security/       JWT service, security filter, blacklist, and user details
└── seller/         Seller creation and listing
```

Each feature follows a `controller → service → repository` layering. Controllers own DTOs and HTTP concerns; services own business rules and transaction boundaries; repositories isolate persistence. MapStruct handles entity-to-DTO conversion.

### Sale lifecycle

```
PENDING ──(manager completes)──▶ COMPLETED
   │                                  │
   ▼                                  ▼
CANCELLED ◀──────────────────── CANCELLED
                         (stock returned on completed cancellation)
```

---

## Features

### Authentication and security

- JWT access tokens with configurable expiration.
- Persistent hashed refresh tokens with rotation on every use.
- Logout with access-token blacklist stored in Redis.
- Login rate limiting backed by Redis.
- Role-based access control: `MANAGER` and `SELLER`.
- Centralized exception handling with standardized error responses.

### Sales and inventory

- Sellers create sales in `PENDING` state.
- Pending sales reserve stock logically without debiting inventory.
- Managers complete sales and trigger stock debit.
- Managers cancel sales with a required reason; completed cancellations return stock.
- Overselling prevention via pending-sale checks and pessimistic product locking.
- Full sale-status history and Kafka business events on every transition.

### CRM operations

- Managers create and list sellers.
- Sellers create and manage their own clients.
- Managers view all clients and reassign them between sellers.
- Product catalog with create, update, list, filter, and deactivate operations.
- Dashboard with revenue and CRM indicators filtered by period.

---

## Tech stack

| Technology | Purpose |
|---|---|
| Java 17 | Runtime |
| Spring Boot 3.5 | Application framework |
| Spring Boot Actuator | Health checks and operational metrics |
| Spring Security | Authentication and authorization |
| Spring Data JPA / Hibernate | Persistence layer |
| PostgreSQL 16 | Production database |
| H2 | Local development database |
| Redis 7 | Cache, token blacklist, and rate limiting |
| Kafka KRaft | Asynchronous business events |
| Flyway | Production schema migrations |
| MapStruct | DTO/entity mapping |
| SpringDoc OpenAPI | Swagger UI and OpenAPI spec |
| Nginx Alpine | Reverse proxy and load balancer |
| JUnit 5 + Mockito | Unit and integration tests |
| Testcontainers | Integration infrastructure (requires Docker) |

---

## Getting started

**Prerequisites:** Java 17+, Maven 3.8+ (or use `./mvnw`), Docker and Docker Compose.

### Local dev (H2, no Docker required)

```bash
./mvnw clean spring-boot:run
```

| Interface | URL |
|---|---|
| Swagger UI | http://localhost:8080/api/swagger-ui/index.html |
| H2 console | http://localhost:8080/api/h2-console |

### Useful commands

```bash
./mvnw test                          # run all tests
./mvnw clean package                 # build jar
docker compose up -d --build         # start full stack
docker compose up -d --build --scale api=3  # start with 3 API replicas
docker compose down                  # stop and remove containers
```

---

## Running with Docker Compose

Start the full stack (PostgreSQL, Redis, Kafka, API, Nginx):

```bash
docker compose up -d --build
```

To create an initial manager in the Docker/prod stack, enable bootstrap explicitly:

```bash
BOOTSTRAP_MANAGER_ENABLED=true \
BOOTSTRAP_MANAGER_EMAIL=admin@example.com \
BOOTSTRAP_MANAGER_PASSWORD='change-me' \
docker compose up -d --build
```

The `api` service is not published directly. All external traffic enters through Nginx after the API healthcheck reports `UP`:

```
http://localhost:8080/api
```

| Service | Purpose | External port |
|---|---|---|
| `nginx` | Reverse proxy and load balancer | 8080 |
| `api` | Spring Boot application replicas | — |
| `postgres` | Database | 5432 |
| `redis` | Cache and security state | 6379 |
| `kafka` | Event broker | 9092 |

Operational endpoints exposed through the API context path:

| Endpoint | Auth | Purpose |
|---|---|---|
| `/api/actuator/health` | Public | Container and load-balancer health verification |
| `/api/actuator/info` | Public | Basic application info endpoint |
| `/api/actuator/metrics` | Authenticated | Runtime metrics |

### Local development seed users

These users are created by `DataInitializer` only when the `dev` profile is active. The Docker Compose stack uses the `prod` profile and starts with the schema created by Flyway.

| Role | Email | Password |
|---|---|---|
| `MANAGER` | admin@crm.com | admin123 |
| `SELLER` | seller1@crm.com | seller123 |
| `SELLER` | seller2@crm.com | seller123 |

---

## Horizontal scaling

Scale the API to any number of replicas:

```bash
docker compose up -d --build --scale api=3
```

The Nginx upstream is configured with:

- `least_conn` — routes each request to the replica with the fewest active connections.
- Passive health checking — skips replicas returning `error`, `timeout`, `502`, or `503`.
- Forwarded headers — `X-Real-IP`, `X-Forwarded-For`, `X-Forwarded-Proto`, `Host`.
- Gzip compression for JSON responses.
- Access log including the selected upstream backend.

**Verify load distribution:**

```bash
for i in {1..20}; do
  curl -s -o /dev/null -w "%{http_code}\n" http://localhost:8080/api/v3/api-docs
done
```

**Inspect upstream selection in Nginx logs:**

```bash
docker compose logs nginx | grep upstream=
```

**After changing replica count on a running stack**, restart Nginx so it re-resolves the Docker service:

```bash
docker compose restart nginx
```

**Monitor replica health:**

```bash
docker compose ps
docker compose logs -f nginx api
```

---

## CI/CD

The repository includes a GitHub Actions pipeline in `.github/workflows/ci.yml`.

On pull requests to `main`, the workflow:

- Sets up Java 17 with Maven dependency caching.
- Runs `./mvnw -B verify`.
- Uploads Surefire test reports.
- Builds the Docker image to validate the Dockerfile.

On pushes to `main`, the workflow also publishes the Docker image to GitHub Container Registry:

```text
ghcr.io/devguilhrm/api-erp:latest
ghcr.io/devguilhrm/api-erp:sha-<commit>
```

The Docker build uses GitHub Actions cache through Buildx to keep repeated builds faster.

---

## API reference

### Authentication

| Method | Endpoint | Auth | Description |
|---|---|---|---|
| `POST` | `/api/auth/login` | Public | Authenticate and receive tokens |
| `POST` | `/api/auth/refresh` | Public | Rotate refresh token and issue new access token |
| `POST` | `/api/auth/logout` | Public | Revoke refresh token and blacklist access token |

### Clients

| Method | Endpoint | Auth | Description |
|---|---|---|---|
| `GET` | `/api/clients` | Authenticated | List clients with seller isolation and filters |
| `GET` | `/api/clients/{id}` | Authenticated | Get client by ID |
| `POST` | `/api/clients` | `SELLER` | Create client for the authenticated seller |
| `PUT` | `/api/clients/{id}` | Authenticated | Update client |
| `PUT` | `/api/clients/{id}/reassign` | `MANAGER` | Reassign client to another seller |

### Products

| Method | Endpoint | Auth | Description |
|---|---|---|---|
| `GET` | `/api/products` | Authenticated | List products with filters |
| `GET` | `/api/products/{id}` | Authenticated | Get product by ID |
| `POST` | `/api/products` | `MANAGER` | Create product |
| `PUT` | `/api/products/{id}` | `MANAGER` | Update product |
| `DELETE` | `/api/products/{id}` | `MANAGER` | Deactivate product |

### Sales

| Method | Endpoint | Auth | Description |
|---|---|---|---|
| `GET` | `/api/sales` | Authenticated | List sales with seller isolation and filters |
| `GET` | `/api/sales/{id}` | Authenticated | Get sale by ID |
| `POST` | `/api/sales` | `SELLER` | Create pending sale |
| `PUT` | `/api/sales/{id}/complete` | `MANAGER` | Complete sale and debit stock |
| `PUT` | `/api/sales/{id}/cancel` | `MANAGER` | Cancel sale and return stock when applicable |

### Managers, sellers, and dashboard

| Method | Endpoint | Auth | Description |
|---|---|---|---|
| `GET` | `/api/managers/me` | `MANAGER` | Return the authenticated manager |
| `GET` | `/api/sellers` | `MANAGER` | List sellers |
| `POST` | `/api/sellers` | `MANAGER` | Create seller |
| `GET` | `/api/dashboard` | `MANAGER` | View global dashboard |

### Example requests

**Login**

```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email": "admin@crm.com", "password": "admin123"}'
```

**Create product**

```bash
curl -X POST http://localhost:8080/api/products \
  -H "Authorization: Bearer <access-token>" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Notebook Pro",
    "description": "Commercial notebook",
    "price": 5500.00,
    "stockQuantity": 12
  }'
```

**Create sale**

```bash
curl -X POST http://localhost:8080/api/sales \
  -H "Authorization: Bearer <access-token>" \
  -H "Content-Type: application/json" \
  -d '{
    "clientId": "<client-id>",
    "paymentMethod": "PIX",
    "discount": 0.00,
    "items": [{"productId": "<product-id>", "quantity": 2}]
  }'
```

**Complete sale**

```bash
curl -X PUT http://localhost:8080/api/sales/<sale-id>/complete \
  -H "Authorization: Bearer <access-token>"
```

**Common filters**

```bash
# Products
curl "http://localhost:8080/api/products?search=notebook&active=true&lowStockThreshold=20" \
  -H "Authorization: Bearer <access-token>"

# Sales by period and status
curl "http://localhost:8080/api/sales?status=COMPLETED&from=2026-01-01&to=2026-01-31" \
  -H "Authorization: Bearer <access-token>"

# Dashboard
curl "http://localhost:8080/api/dashboard?from=2026-01-01&to=2026-01-31" \
  -H "Authorization: Bearer <access-token>"
```

---

## Configuration

All values can be overridden via environment variables in `docker-compose.yml` or a `.env` file.

| Variable | Description | Default |
|---|---|---|
| `SPRING_PROFILES_ACTIVE` | Active Spring profile | `dev` |
| `DATABASE_URL` | PostgreSQL JDBC URL | Required in `prod` |
| `DATABASE_USERNAME` | PostgreSQL username | Required in `prod` |
| `DATABASE_PASSWORD` | PostgreSQL password | Required in `prod` |
| `REDIS_HOST` | Redis host | `localhost` |
| `REDIS_PORT` | Redis port | `6379` |
| `KAFKA_BOOTSTRAP_SERVERS` | Kafka bootstrap address | `localhost:9092` |
| `KAFKA_PRODUCER_MAX_BLOCK_MS` | Max Kafka producer metadata wait | `2000` |
| `JWT_SECRET` | Secret for signing access tokens | `dev-secret-key-...` |
| `JWT_ACCESS_MINUTES` | Access token expiration (minutes) | `15` |
| `JWT_REFRESH_DAYS` | Refresh token expiration (days) | `7` |
| `CACHE_TTL_MINUTES` | Cache TTL (minutes) | `5` |
| `LOGIN_RATE_LIMIT_MAX_ATTEMPTS` | Max failed login attempts | `5` |
| `LOGIN_RATE_LIMIT_WINDOW_MINUTES` | Rate-limit window (minutes) | `15` |
| `CORS_ALLOWED_ORIGINS` | Comma-separated allowed origins | `http://localhost:3000,...` |
| `BOOTSTRAP_MANAGER_ENABLED` | Enables initial manager creation on startup | `false` |
| `BOOTSTRAP_MANAGER_NAME` | Initial manager display name | `Administrador` |
| `BOOTSTRAP_MANAGER_EMAIL` | Initial manager email, required when bootstrap is enabled | Empty |
| `BOOTSTRAP_MANAGER_PASSWORD` | Initial manager password, required when bootstrap is enabled | Empty |

### Redis responsibilities

- Dashboard, product, and client list cache with configurable TTL.
- JWT access-token blacklist after logout.
- Login attempt rate limiting.

### Kafka topics

| Topic | Published when |
|---|---|
| `sales.created` | Seller creates a pending sale |
| `sales.completed` | Manager completes a sale |
| `sales.cancelled` | Manager cancels a sale |
| `stock.updated` | Stock is debited or returned |

---

## Tests

```bash
./mvnw test
```

Coverage includes:

- Authentication: login, invalid credentials, refresh flow.
- Refresh-token: create, validate, rotate, revoke, hashing.
- Login rate limiting and token blacklist.
- User details loading.
- Manager, seller, and dashboard services.
- Client ownership and seller isolation.
- Product creation and listing.
- Sale creation, pending-stock checks, completion, cancellation, stock return, and overselling prevention.
- Integration flow with Testcontainers (skipped automatically when Docker is unavailable).

---

## Contributing

1. Create a feature branch from `main`.
2. Keep changes focused; cover behavior changes with tests.
3. Use [Conventional Commits](https://www.conventionalcommits.org).
4. Include test evidence in pull requests.

Accepted commit types: `feat`, `fix`, `docs`, `test`, `refactor`, `chore`, `build`.

---

## License

MIT License.
