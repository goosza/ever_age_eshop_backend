# Everage E-Shop Backend

REST API for Everage e-commerce platform with integrated payment processing (Stripe) and shipping management (Zásilkovna).

## Features

- 🛍️ **Product Management** - Items and collections with image storage
- 💳 **Payment Processing** - Stripe Checkout integration
- 📦 **Shipping Integration** - Zásilkovna API with multiple delivery methods
- 🔄 **Order Management** - Complete order lifecycle tracking
- 📊 **Admin Dashboard** - Order and shipping management
- 🔐 **Webhook Support** - Automated order processing via Stripe and Zásilkovna webhooks

## Tech Stack

- **Framework**: Spring Boot 3.x
- **Database**: PostgreSQL with Flyway migrations
- **Payment**: Stripe API
- **Shipping**: Zásilkovna API
- **Storage**: Cloudflare R2 (optional)
- **Documentation**: OpenAPI/Swagger

## Quick Start

### Prerequisites

- Java 17+
- PostgreSQL 14+
- Docker (optional)

### Local Development

1. **Clone repository**
   ```bash
   git clone <repository-url>
   cd everage-eshop
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

5. **Access API**
   - API: http://localhost:8080
   - Swagger: http://localhost:8080/swagger-ui.html

## API Endpoints

### Products
- `GET /api/items` - List all items
- `GET /api/items/{uuid}` - Get item details
- `GET /api/collections` - List collections

### Checkout & Payment
- `POST /api/orders/checkout` - Create Stripe checkout session
- `POST /api/stripe/webhook` - Stripe webhook handler

### Shipping
- `GET /api/shipping/options` - Get available shipping methods with pricing
- `GET /api/shipping/track/{trackingNumber}` - Track shipment
- `GET /api/shipping/order/{orderUuid}/label` - Download shipping label

### Order Tracking
- `GET /api/orders/track/{orderNumber}` - Track order by number

## Shipping Pricing

Shipping costs are configured in `application.properties` and served via API:

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
      "cost": 12.00,
      "provider": "ZASILKOVNA",
      "available": true
    }
  ]
}
```

See [SHIPPING_PRICING_API.md](SHIPPING_PRICING_API.md) for details.

## Configuration

### Application Properties

```properties
# Database
spring.datasource.url=jdbc:postgresql://localhost:5432/everage
spring.datasource.username=postgres
spring.datasource.password=postgres

# Stripe
stripe.api.key=sk_test_...
stripe.webhook.secret=whsec_...

# Zásilkovna
zasilkovna.api.key=your-api-key
zasilkovna.api.password=your-password
zasilkovna.sender.id=your-sender-id
zasilkovna.enabled=false

# Shipping Pricing (EUR)
shipping.pricing.pickup=12.00
shipping.pricing.zbox=10.00
shipping.pricing.home=25.00
shipping.pricing.carrier-pickup=15.00
```

## Documentation

- [Payment Flow](PAYMENT_FLOW.md) - Stripe integration details
- [Shipping Pricing API](SHIPPING_PRICING_API.md) - Shipping costs endpoint
- [Frontend Integration](FRONTEND_SHIPPING_INTEGRATION.md) - How to use shipping API
- [Zásilkovna Integration](ZASILKOVNA_INTEGRATION_PLAN.md) - Shipping provider setup
- [Testing Guide](STRIPE_TESTING_GUIDE.md) - How to test payments

## Build & Deploy

### Build
```bash
./gradlew build
```

### Run Tests
```bash
./gradlew test
```

### Docker
```bash
docker-compose up
```

## License

Proprietary
