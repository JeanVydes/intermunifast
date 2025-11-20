# InterMuniFast - AI Coding Agent Instructions

## Project Overview
InterMuniFast is a **full-stack bus reservation system** for intermunicipal transport in Colombia. The system consists of:
- **Backend**: Spring Boot 3.5.7 (Java 17) REST API with PostgreSQL (`reservaBus/reservaBus/`)
- **Frontend**: Preact + TypeScript SPA with Vite, Tailwind CSS 4.x (`intermunifast-web/`)

The app handles trip search, seat booking, ticket management, baggage handling, and payment processing with dynamic pricing and role-based access control.

## Architecture Patterns

### Backend Architecture (Spring Boot)
- **Domain-Driven Design**: Entities in `domain/entities/`, repositories in `domain/repositories/`
- **Service Layer**: All business logic in `services/implementations/` with interface contracts in `services/definitions/`
- **DTOs + MapStruct**: API uses DTOs (`api/dto/`), MapStruct mappers (`services/mappers/`) for entity-DTO conversion
- **JWT Authentication**: Stateless JWT-based auth with Bearer tokens. Config in `security/config/SecurityConfig.java`
  - Public endpoints: `POST /api/accounts`, `/api/auth/**`, all `GET /**`
  - Authenticated: All other endpoints require `Authorization: Bearer <token>` header
- **Transactional Service Pattern**: All service implementations are `@Transactional` at class level, read-only methods use `@Transactional(readOnly = true)`

### Frontend Architecture (Preact)
- **State Management**: Zustand stores (`stores/AuthStore.ts`, `AccountStore.ts`, `RouteStore.ts`)
- **API Layer**: Centralized Axios instance with interceptors (`api/API.ts`)
  - Auto-injects JWT token from Zustand store
  - Auto-redirects to login on 401 responses
  - Generic `createEndpoint()` factory for type-safe API calls
- **Routing**: `preact-iso` router with route definitions in `pages/`
- **Component Pattern**: Functional components with hooks, organized by feature in `components/` and `pages/`

## Critical Domain Logic

### Trip Lifecycle & Automated Status Updates
**TripWatcher Service** (`services/watchers/TripWatcher.java`) runs scheduled tasks:
- **Every 1 minute**: Updates trip status based on departure time
  - `BOARDING`: 30 minutes before departure
  - `DEPARTED`: At departure time (also sets parcels to `IN_TRANSIT`)
  - `NO_SHOW`: Tickets not confirmed 5 minutes before departure
- **Every 5 minutes**: Logs completed trips to `TripLog` for metrics (occupation rate, revenue, punctuality)

### Seat Hold System
- **Temporary Reservations**: `SeatHold` entity reserves seats for configurable minutes (default: 10)
- **Expiration**: Holds expire if not converted to tickets within timeout
- **Configuration**: `ConfigCacheService` loads system configs into memory at startup (e.g., `MAX_SEAT_HOLD_MINUTES`)

### Pricing & Fare Rules
- **Base Calculation**: `Route.pricePerKm * Route.distanceKm`
- **Discounts**: Configured per route in `FareRule` entity (children, senior, student discounts)
- **Baggage Fees**: Extra charge if baggage exceeds `MAX_BAGGAGE_WEIGHT_KG` (default: 25kg, fee: 3% of ticket price)

### Ticket Payment Flow
1. **Create Ticket**: `POST /api/tickets` → status `PENDING_APPROVAL`
2. **Add to Cart**: Frontend stores ticket IDs in localStorage (`cartTicketIds`)
3. **Payment**: `POST /api/tickets/payments/multiple` with `paymentIntentId` → status `CONFIRMED`
4. **Cart Restoration**: On page load, frontend fetches tickets by ID and validates status

## Development Workflows

### Running the Application
```bash
# Backend (from reservaBus/reservaBus/)
./mvnw spring-boot:run
# Runs on localhost:8080, connects to PostgreSQL

# Frontend (from intermunifast-web/)
npm run dev
# Runs on localhost:5173, proxies API to localhost:8080
```

### Database Seeding
Use `seed-data-api.sh` to populate the database via REST API:
- Creates admin account (`j@lojur.com` / `123`) + 5 test users
- Creates 5 buses with seats (capacities: 40, 35, 30, 45, 25)
- Creates 4 routes (Santa Marta-Barranquilla, Santa Marta-Valledupar, etc.)
- Generates ~100 trips for November-December 2025 with random times
- Creates sample tickets for showcase

### Testing
- **Integration Tests**: Use Testcontainers for PostgreSQL (`@ServiceConnection` in `AbstractControllerIT`)
- **Run tests**: `./mvnw test` or `docker-compose -f docker-compose.test.yml up`

## Key Conventions

### Backend
- **Naming**: Service interfaces in `services/definitions/`, implementations in `services/implementations/` with `Impl` suffix
- **Error Handling**: Custom exceptions in `exceptions/`, handled by global exception handler
- **Enums**: All domain enums in `domain/enums/` (e.g., `TripStatus`, `TicketStatus`, `PaymentMethod`)
- **Timestamps**: All entities extend `TimestampedEntity` (auto `createdAt`, `updatedAt`)

### Frontend
- **API Calls**: Use typed API modules (`api/Trip.ts`, `api/Ticket.ts`) with `createEndpoint()` factory
- **Auth Context**: `AuthContextProvider` fetches user account on mount and syncs with localStorage
- **TypeScript Types**: Mirror backend DTOs in `api/types/` (e.g., `Transport.ts`, `Account.ts`)
- **Component Naming**: PascalCase for components, `index.tsx` for page entry points

### Environment Variables
**Backend** (`application.properties`):
- `DB_HOST`, `DB_PORT`, `DB_NAME`, `DB_USERNAME`, `DB_PASSWORD`: PostgreSQL connection
- `JWT_SECRET_KEY`, `JWT_EXPIRATION_MS`: JWT configuration

**Frontend** (`Config.ts`):
- `PRODUCTION` flag: Switches between local (`localhost:8080`) and prod API URL
- `API_REQUESTS_TIMEOUT`: Default 5000ms

## Common Gotchas
- **CORS**: Backend allows all origins via `@CrossOrigin` on controllers
- **JWT Expiration**: Frontend clears token on 401 but doesn't auto-refresh (implement refresh tokens if needed)
- **MapStruct Compilation**: Lombok + MapStruct require specific annotation processor ordering in `pom.xml`
- **Route Stops**: Tickets require `fromStopId` and `toStopId` for partial route bookings (can be `null` for full route)
- **Cart Persistence**: Frontend cart survives page refresh via localStorage but only restores tickets in `CONFIRMED` or `PENDING_APPROVAL` status
- **Passenger Type Enum**: Use `CHILD` not `CHILDREN` (`FareRulePassengerType` accepts: `ADULT`, `CHILD`, `SENIOR`, `STUDENT`)

## File References
- **Auth flow**: `security/jwt/JwtAuthenticationFilter.java`, `api/Auth.ts`, `providers/AuthContextProvider.tsx`
- **Trip search logic**: `services/implementations/TripServiceImpl.java`, `pages/Home/index.tsx`
- **Booking modal**: `components/BookingModal.tsx` (seat selection, passenger type, baggage)
- **Config system**: `services/ConfigCacheService.java` (in-memory cache with fallback defaults)
