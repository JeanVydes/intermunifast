# ReservaBus Test Suite

This document provides an overview of the comprehensive test suite for the ReservaBus application.

## Test Structure

```
src/test/java/com/example/
├── domain/repositories/          # Repository Integration Tests (with Testcontainers)
│   ├── AccountRepositoryIT.java  ✅ PASSING
│   ├── AssignmentRepositoryIT.java  ⚠️ Needs fixes
│   ├── BusRepositoryIT.java        ⚠️ Needs fixes  
│   ├── RouteRepositoryIT.java      ⚠️ Needs fixes
│   ├── TripRepositoryIT.java       ⚠️ Needs fixes
│   ├── TicketRepositoryIT.java     ⚠️ Needs fixes
│   └── TestJpaConfiguration.java
└── services/                      # Service Unit Tests (with Mockito)
    └── AccountServiceTest.java    ⚠️ Needs fixes

```

## Repository Integration Tests

### Technology Stack
- **Framework**: JUnit 5
- **Container**: Testcontainers PostgreSQL 16-alpine
- **Assertions**: AssertJ
- **Test Slice**: `@DataJpaTest`

### Common Setup
All repository tests use:
- `@Testcontainers` for PostgreSQL container management
- `@ServiceConnection` for automatic datasource configuration
- `@ActiveProfiles("test")` for test profile
- `AutoConfigureTestDatabase.Replace.NONE` to use Testcontainers

### Test Files Created

#### ✅ AccountRepositoryIT.java (WORKING)
Tests for Account repository:
- Save and find by email
- Check existence by email  
- Verify ignore case search

#### ⚠️ AssignmentRepositoryIT.java (Needs Fixes)
**Issues to Fix:**
1. Fix method names: `findByTripId` → `findByTrip_Id`
2. Fix method names: `findByDriverId` → `findByDriver_Id`
3. Fix getter: `isChecklistOk()` → `getChecklistOk()`

**Tests:**
- Save and find assignment
- Find assignments by trip ID
- Find assignments by driver ID

#### ⚠️ BusRepositoryIT.java (Needs Fixes)
**Issues to Fix:**
1. Add missing method to BusRepository: `existsByPlate(String)`

**Tests:**
- Save and find bus by plate
- Find buses by status
- Check if bus exists by plate

#### ⚠️ RouteRepositoryIT.java (READY)
**Tests:**
- Save and find route
- Find routes by origin
- Find routes by destination
- Find routes by origin and destination

####⚠️ TripRepositoryIT.java (Needs Fixes)
**Issues to Fix:**
1. Add missing methods to TripRepository:
   - `findByRouteId(Long)`
   - `findByDate(LocalDate)`
   - `findByRouteIdAndDate(Long, LocalDate)` (already exists as `findByRoute_IdAndDate`)

**Tests:**
- Save and find trip
- Find trips by route ID
- Find trips by date
- Find trips by route ID and date

#### ⚠️ TicketRepositoryIT.java (Needs Fixes)
**Issues to Fix:**
1. Fix enum import: `TicketPaymentMethod` → `PaymentMethod`
2. Fix enum value: `AccountRole.CUSTOMER` → valid AccountRole value
3. Add missing method: `findByTrip_Id(Long)` (exists as `findByTrip_IdAndStatus`)

**Tests:**
- Save and find ticket
- Find tickets by trip ID
- Find tickets by account ID
- Find tickets by QR code

### Additional Repository Tests Needed

Still need to create tests for:
- `BaggageRepository`
- `ConfigRepository`
- `FareRuleRepository`
- `IncidentRepository`
- `ParcelRepository`
- `SeatRepository`
- `SeatHoldRepository`
- `StopRepository`

## Service Unit Tests

### Technology Stack
- **Framework**: JUnit 5
- **Mocking**: Mockito (with `@ExtendWith(MockitoExtension.class)`)
- **Assertions**: AssertJ

### Test Files Created

#### ⚠️ AccountServiceTest.java (Needs Fixes)
**Issues to Fix:**
1. Fix service class name: `AccountServiceImpl` → `AccountServiceimpl` (lowercase impl)
2. Fix AccountRole enum value: `CUSTOMER` → valid value (check AccountRole.java)
3. Fix DTO constructor - check actual AccountDTOs.AccountResponse structure

**Tests:**
- Get account by ID successfully
- Throw NotFoundException when account not found
- Create account successfully
- Get all accounts
- Find account by email
- Delete account by ID
- Throw exception when deleting non-existent account

### Additional Service Tests Needed

Need to create unit tests for:
- `AssignmentService`
- `BaggageService`
- `BusService`
- `ConfigService`
- `FareRuleService`
- `IncidentService`
- `ParcelService`
- `RouteService`
- `SeatService`
- `SeatHoldService`
- `StopService`
- `TicketService`
- `TripService`

## Running Tests

### Run All Tests
```bash
./mvnw test
```

### Run Specific Test Class
```bash
./mvnw test -Dtest=AccountRepositoryIT
```

### Run All Repository Tests
```bash
./mvnw test -Dtest=*RepositoryIT
```

### Run All Service Tests
```bash
./mvnw test -Dtest=*ServiceTest
```

## Test Configuration

### TestJpaConfiguration.java
Custom Spring Boot Application class for tests that:
- Enables JPA repositories
- Scans entities
- Provides minimal Spring context for `@DataJpaTest`

### Test Profile (application-test.properties)
Located at: `src/test/resources/application-test.properties`

```properties
spring.jpa.hibernate.ddl-auto=create-drop
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.format_sql=true
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.PostgreSQLDialect
logging.level.org.hibernate.SQL=DEBUG
logging.level.org.hibernate.type.descriptor.sql.BasicBinder=TRACE
```

## TODO List

### High Priority Fixes
1. ✅ Fix AccountRepositoryIT (DONE)
2. ⚠️ Fix AssignmentRepositoryIT method names
3. ⚠️ Add missing methods to BusRepository
4. ⚠️ Add missing methods to TripRepository
5. ⚠️ Fix TicketRepositoryIT enums and imports
6. ⚠️ Fix AccountServiceTest class names and DTOs

### Medium Priority
7. Create remaining repository tests (8 more repositories)
8. Create remaining service tests (13 more services)

### Low Priority
9. Add integration tests that test multiple layers together
10. Add performance tests for database queries
11. Add test coverage reporting

## Best Practices

### Repository Tests
- Use `@BeforeEach` to set up test data
- Clean database state is automatic (Testcontainers creates fresh DB)
- Use meaningful test names with `@DisplayName`
- Test both happy path and edge cases
- Verify relationships between entities

### Service Tests
- Mock all dependencies (repositories, mappers, etc.)
- Use `verify()` to ensure mocked methods are called
- Test exception scenarios
- Test business logic thoroughly
- Keep tests isolated and independent

## Common Issues & Solutions

### Issue: Tests fail with "No suitable driver found"
**Solution**: Ensure Testcontainers and PostgreSQL driver are in dependencies

### Issue: Tests fail with "ApplicationContext failed to load"
**Solution**: Check that TestJpaConfiguration is imported in test class

### Issue: Repository method not found
**Solution**: Check actual repository interface for correct method name (may use `_` for relationships)

### Issue: Enum value not found
**Solution**: Check actual enum class in `com.example.domain.enums` package

## Next Steps

1. Fix all compilation errors in existing tests
2. Run each test class individually to verify
3. Create remaining repository tests
4. Create remaining service tests
5. Achieve >80% code coverage
