# CRM API REST

![Java](https://img.shields.io/badge/Java-17-orange)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.x-brightgreen)
![Build](https://img.shields.io/badge/build-passing-brightgreen)
![License](https://img.shields.io/badge/license-MIT-blue)

Production-grade CRM REST API for sales teams, managers, clients, products, inventory, and secure JWT sessions.

## About

CRM API REST is a Java Spring Boot backend designed to manage customers, sellers, products, sales, and managerial dashboards through a secure REST interface.

Key technical differentiators include role-based access control, a sale state machine, pessimistic locking during stock debit, optimistic locking on entities, standardized API responses, and centralized error handling.

## Features

**MANAGER**

- Create and list sellers.
- Access the global dashboard.
- Create, update, list, view, and deactivate products.
- Complete pending sales.
- Cancel sales with a required reason.
- Reassign clients between sellers.
- View all clients and sales.

**SELLER**

- Create clients automatically linked to the authenticated seller.
- View only owned clients and sales.
- Create sales with `PENDING` status.
- Validate stock during sale creation without debiting inventory.

**Sale Flow**

```text
PENDING --(manager)--> COMPLETED
   |                       |
(manager)              (manager)
   v                       v
CANCELLED <----------- CANCELLED
          (returns stock)
```

## Tech Stack

| Technology | Version | Purpose |
|---|---:|---|
| Java | 17 | Main programming language |
| Spring Boot | 3.x | Application framework |
| Spring Security | 6 | Authentication, authorization, RBAC |
| JWT (JJWT) | 0.12.x | Access token generation and validation |
| H2 | Runtime | Development database |
| PostgreSQL | Runtime | Production database |
| JPA / Hibernate | 6.x | ORM and persistence |
| MapStruct | 1.6.x | Entity and DTO mapping |
| SpringDoc OpenAPI | 2.x | Swagger UI and OpenAPI docs |
| JUnit 5 | 5.x | Automated testing |
| Mockito | 5.x | Unit test mocking |
| Flyway | 10.x+ | Production schema migrations |

## Architecture

```text
src/main/java/com/devguilhrm/API_ERP/
├── auth/           Authentication, users, login, and token renewal contracts
├── clients/        Client lifecycle, seller ownership, and reassignment
├── common/         BaseEntity, shared enums, and response envelopes
├── config/         Development seed data
├── dashboard/      Managerial revenue and CRM indicators
├── exception/      Custom exceptions and GlobalExceptionHandler
├── manager/        Manager-specific operations
├── product/        Product catalog and stock management
├── refreshToken/   Persistent refresh token lifecycle
├── sale/           Sales, sale items, state machine, and stock rules
├── security/       JWT filter, security chain, CORS, and user details
└── seller/         Seller creation and listing workflows
```

The API follows a layered structure: `controller -> service -> repository`. Controllers expose REST endpoints and return DTOs, services enforce business rules and transactions, repositories isolate persistence, and MapStruct mappers convert entities without exposing JPA models. Shared concerns live in `BaseEntity`, response wrappers, and `GlobalExceptionHandler`.

## Prerequisites and Installation

**Requirements**

- Java 17+
- Maven 3.8+ or the included Maven Wrapper
- Git

**Run locally**

1. Clone the repository:

```bash
git clone <repository-url>
```

2. Enter the project directory:

```bash
cd API-ERP
```

3. Start the application:

```bash
./mvnw clean spring-boot:run
```

4. Open Swagger UI:

```bash
http://localhost:8080/api/swagger-ui/index.html
```

## Access Credentials

| Role | Email | Password |
|---|---|---|
| MANAGER | admin@crm.com | admin123 |
| SELLER | seller1@crm.com | seller123 |
| SELLER | seller2@crm.com | seller123 |

## Usage Examples

**Login**

```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "admin@crm.com",
    "password": "admin123"
  }'
```

**Create product (MANAGER)**

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

**Create sale (SELLER)**

```bash
curl -X POST http://localhost:8080/api/sales \
  -H "Authorization: Bearer <access-token>" \
  -H "Content-Type: application/json" \
  -d '{
    "clientId": "<client-id>",
    "paymentMethod": "PIX",
    "discount": 0.00,
    "items": [
      {
        "productId": "<product-id>",
        "quantity": 2
      }
    ]
  }'
```

**Complete sale (MANAGER)**

```bash
curl -X PUT http://localhost:8080/api/sales/<sale-id>/complete \
  -H "Authorization: Bearer <access-token>"
```

**Cancel sale (MANAGER)**

```bash
curl -X PUT http://localhost:8080/api/sales/<sale-id>/cancel \
  -H "Authorization: Bearer <access-token>" \
  -H "Content-Type: application/json" \
  -d '{
    "reason": "Customer requested cancellation"
  }'
```

## Environment Variables

| Variable | Description | Default |
|---|---|---|
| `JWT_SECRET` | Secret key used to sign access tokens | `dev-secret-key-change-me-dev-secret-key-change-me` |
| `JWT_ACCESS_MINUTES` | Access token expiration in minutes | `15` |
| `JWT_REFRESH_DAYS` | Refresh token expiration in days | `7` |
| `CORS_ALLOWED_ORIGINS` | Comma-separated list of allowed origins | `http://localhost:3000,http://localhost:5173` |
| `SPRING_PROFILES_ACTIVE` | Active Spring profile | `dev` |
| `server.port` | HTTP server port | `8080` |
| `DATABASE_URL` | PostgreSQL JDBC URL for production | Required in `prod` |
| `DATABASE_USERNAME` | PostgreSQL username | Required in `prod` |
| `DATABASE_PASSWORD` | PostgreSQL password | Required in `prod` |

## Tests

Run the automated test suite:

```bash
./mvnw test
```

Covered areas:

- Authentication service: login, invalid credentials, refresh token flow.
- Client service: seller binding and data isolation.
- Product service: product creation.
- Sale service: creation, completion, cancellation, and overselling prevention.
- Integration flow: pending sale creation, manager completion, cancellation, and stock return.

## Endpoints

| Method | Endpoint | Description | Permission |
|---|---|---|---|
| `POST` | `/api/auth/login` | Authenticate user | Public |
| `POST` | `/api/auth/refresh` | Renew access token | Public |
| `GET` | `/api/clients` | List clients with seller isolation | Authenticated |
| `GET` | `/api/clients/{id}` | Get client by ID | Authenticated |
| `POST` | `/api/clients` | Create client for authenticated seller | SELLER |
| `PUT` | `/api/clients/{id}` | Update client | Authenticated |
| `PUT` | `/api/clients/{id}/reassign` | Reassign client to another seller | MANAGER |
| `GET` | `/api/products` | List products | Authenticated |
| `GET` | `/api/products/{id}` | Get product by ID | Authenticated |
| `POST` | `/api/products` | Create product | MANAGER |
| `PUT` | `/api/products/{id}` | Update product | MANAGER |
| `DELETE` | `/api/products/{id}` | Deactivate product | MANAGER |
| `GET` | `/api/sales` | List sales with seller isolation | Authenticated |
| `GET` | `/api/sales/{id}` | Get sale by ID | Authenticated |
| `POST` | `/api/sales` | Create pending sale | SELLER |
| `PUT` | `/api/sales/{id}/complete` | Complete sale and debit stock | MANAGER |
| `PUT` | `/api/sales/{id}/cancel` | Cancel sale and return stock when needed | MANAGER |
| `GET` | `/api/sellers` | List sellers | MANAGER |
| `POST` | `/api/sellers` | Create seller | MANAGER |
| `GET` | `/api/dashboard` | View global CRM dashboard | MANAGER |

## Contribution

1. Fork the repository.
2. Create a feature branch:

```bash
git checkout -b feat/your-feature
```

3. Commit using Conventional Commits:

```bash
git commit -m "feat: add your feature"
```

4. Open a pull request with a concise description and test evidence.

Recommended commit types: `feat`, `fix`, `docs`, `test`, `refactor`, `chore`, `build`.

## License

This project is licensed under the MIT License.
