package com.example.domain.repositories;

import com.example.domain.entities.FareRule;
import com.example.domain.entities.Route;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Testcontainers
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(TestJpaConfiguration.class)
@DisplayName("FareRule Repository Integration Tests")
class FareRuleRepositoryIT {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

    @Autowired
    private FareRuleRepository fareRuleRepository;

    @Autowired
    private RouteRepository routeRepository;

    private Route route;

    @BeforeEach
    void setUp() {
        route = Route.builder()
                .code("RT001")
                .name("Test Route")
                .origin("City A")
                .destination("City B")
                .durationMinutes(120)
                .distanceKm(100.0)
                .pricePerKm(0.5)
                .build();
        route = routeRepository.save(route);
    }

    @Test
    @DisplayName("Should save and retrieve fare rule")
    void shouldSaveAndRetrieveFareRule() {
        // Given
        FareRule fareRule = FareRule.builder()
                .basePrice(50.0)
                .childrenDiscount(0.5)
                .seniorDiscount(0.3)
                .studentDiscount(0.2)
                .dynamicPricing(true)
                .route(route)
                .build();

        // When
        FareRule saved = fareRuleRepository.save(fareRule);

        // Then
        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getBasePrice()).isEqualTo(50.0);
        assertThat(saved.getChildrenDiscount()).isEqualTo(0.5);
        assertThat(saved.getDynamicPricing()).isTrue();
    }

    @Test
    @DisplayName("Should find fare rule by route ID")
    void shouldFindFareRuleByRouteId() {
        // Given
        FareRule fareRule = FareRule.builder()
                .basePrice(75.0)
                .childrenDiscount(0.5)
                .seniorDiscount(0.3)
                .studentDiscount(0.2)
                .dynamicPricing(false)
                .route(route)
                .build();
        fareRuleRepository.save(fareRule);

        // When
        FareRule found = fareRuleRepository.findByRouteId(route.getId());

        // Then
        assertThat(found).isNotNull();
        assertThat(found.getBasePrice()).isEqualTo(75.0);
        assertThat(found.getRoute().getId()).isEqualTo(route.getId());
    }

    @Test
    @DisplayName("Should save fare rule with all discount types")
    void shouldSaveFareRuleWithAllDiscounts() {
        // Given
        FareRule fareRule = FareRule.builder()
                .basePrice(100.0)
                .childrenDiscount(0.5)
                .seniorDiscount(0.4)
                .studentDiscount(0.25)
                .dynamicPricing(true)
                .route(route)
                .build();

        // When
        FareRule saved = fareRuleRepository.save(fareRule);

        // Then
        assertThat(saved.getChildrenDiscount()).isEqualTo(0.5);
        assertThat(saved.getSeniorDiscount()).isEqualTo(0.4);
        assertThat(saved.getStudentDiscount()).isEqualTo(0.25);
    }
}
