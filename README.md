# Everage E-Shop Backend

REST API for the Everage e-commerce platform, with integrated payment processing (Stripe) and shipping management (Zásilkovna).

For infrastructure layout, request routing, and the security filter chain, see [docs/ARCHITECTURE.md](docs/ARCHITECTURE.md).

## Features

- 🛍️ **Product Management** — items and collections, JSON image URL storage
- 💳 **Payment Processing** — Stripe Checkout integration with webhook-driven order creation
- 📦 **Shipping Integration** — Zásilkovna API with multiple delivery methods (pickup, Z-Box, home, carrier pickup)
- 🔄 **Order Management** — full order lifecycle, public tracking by order number or Stripe session ID
- 📊 **Admin API** — HMAC-authenticated endpoints for managing items, collections, orders, and shipping
- 📧 **Email Notifications** — order confirmation and shipping notification emails (Thymeleaf templates), no-op stub when SMTP isn't configured
- 🔐 **Security** — rate limiting (Bucket4j), HMAC request signing for admin, optimistic locking on stock

## Tech Stack

- **Framework**: Spring Boot 4
- **Database**: PostgreSQL with Flyway migrations
- **Payment**: Stripe API
- **Shipping**: Zásilkovna API
- **Storage**: Cloudflare R2 (optional, S3-compatible)
- **Documentation**: OpenAPI/Swagger (dev/local only)

## Quick Start

### Prerequisites

- Java 21+
- PostgreSQL 16+
- Docker (optional, for local Postgres or full stack)

### Local Development

1. **Clone repository**
   ```bash
   git clone <repository-url>
   cd ever_age_eshop_backend
   ```

2. **Setup database**
   ```bash
   createdb everage
   ```

3. **Configure environment**
   ```bash
   cp .env.example .env
   # Edit .env with your credentials
   ```

4. **Run application**
   ```bash
   ./gradlew bootRun --args='--spring.profiles.active=local'
   ```
   `admin.dev-mode=true` in the `local` profile bypasses HMAC auth on
   `/api/admin/**` so you can hit admin endpoints directly during development.

5. **Access API**
   - API: http://localhost:8080
   - Swagger: http://localhost:8080/swagger-ui.html
   - Health: http://localhost:8080/actuator/health

## API Endpoints

### Public — Products & Collections
- `GET /api/items` — paginated item list
- `GET /api/items/all` — full item list (no pagination)
- `GET /api/items/{uuid}` — item details
- `GET /api/collections` — list collections

### Public — Checkout & Order Tracking
- `POST /api/orders/checkout` — create a Stripe Checkout session (rate-limited: 5/min per IP)
- `GET /api/orders/track/{orderNumber}` — track an order by its public order number (rate-limited: 10/min per IP)
- `GET /api/orders/by-session/{sessionId}` — look up an order by Stripe session ID (rate-limited: 10/min per IP)

### Public — Shipping
- `GET /api/shipping/options` — available shipping methods with zone-based pricing
- `GET /api/shipping/countries` — supported countries
- `GET /api/shipping/track/{trackingNumber}` — track a shipment

### Webhooks (signature/token-verified, not rate-limited)
- `POST /api/webhooks/stripe` — Stripe checkout events → creates order, sends confirmation email
- `POST /api/zasilkovna/webhook?token=...` — Zásilkovna shipment status updates → sends shipping notification email

### Admin (requires `ROLE_ADMIN` via HMAC signature — see [docs/ARCHITECTURE.md](docs/ARCHITECTURE.md))
- `/api/admin/items/**` — item CRUD
- `/api/admin/collections/**` — collection CRUD
- `/api/admin/orders/**` — order management, paginated, filterable by status
- `/api/admin/shipping/**` — shipping management

## Shipping Pricing

Shipping costs are zone-based (configured in `application-*.properties`) and served via API:

```bash
GET /api/shipping/options?country=CZ
```

**Response:**
```json
{
  "methods": [
    {
      "method": "PICKUP",
      "name": "Pick-up Point",
      "cost": 69.00,
      "provider": "ZASILKOVNA",
      "available": true
    }
  ]
}
```

## Configuration

Backend configuration is entirely environment-variable driven — see
[`.env.example`](.env.example) for the full list. Key groups:

| Area | Variables |
|---|---|
| Database | `DATABASE_URL`, `DATABASE_USERNAME`, `DATABASE_PASSWORD` |
| Admin auth | `ADMIN_SECRET` (generate with `openssl rand -hex 32`), `ADMIN_DEV_MODE` |
| Stripe | `STRIPE_API_KEY`, `STRIPE_WEBHOOK_SECRET` |
| Zásilkovna | `ZASILKOVNA_API_KEY`, `ZASILKOVNA_API_PASSWORD`, `ZASILKOVNA_SENDER_ID`, `ZASILKOVNA_ENABLED` |
| Mail (optional) | `MAIL_HOST`, `MAIL_USERNAME`, `MAIL_PASSWORD`, `SUPPORT_EMAIL` — if unset, `EmailService` logs a `STUB:` warning and skips sending instead of failing |
| Cloudflare R2 (optional) | `R2_ACCESS_KEY`, `R2_SECRET_KEY`, `R2_ENDPOINT`, `R2_BUCKET`, `R2_PUBLIC_URL` |
| Frontend origin | `FRONTEND_URL` — used for CORS and for building Stripe redirect / email tracking links |

Active profile is controlled by `SPRING_PROFILES_ACTIVE` (`local`, `dev`, or `prod`).

## Build & Deploy

### Build
```bash
./gradlew build
```

### Run Tests
```bash
./gradlew test
```

### Docker (full stack: Caddy, frontend, admin, backend, Postgres, Grafana/Loki)
```bash
cd docker
docker compose up -d
```
See [docs/ARCHITECTURE.md](docs/ARCHITECTURE.md) for how this maps to production/dev infrastructure, and [`docker/docker-compose.yml`](docker/docker-compose.yml) for the full service definitions.

## License

Proprietary
