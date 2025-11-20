package com.example.domain.repositories;

import com.example.domain.entities.Bus;
import com.example.domain.enums.BusStatus;
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

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Testcontainers
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(TestJpaConfiguration.class)
@DisplayName("Bus Repository Integration Tests")
class BusRepositoryIT {

        @Container
        @ServiceConnection
        static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

        @Autowired
        private BusRepository busRepository;

        @Test
        @DisplayName("Should save and find bus by plate")
        void shouldSaveAndFindBusByPlate() {
                // Given
                Bus bus = Bus.builder()
                                .plate("XYZ789")
                                .capacity(50)
                                .status(BusStatus.ACTIVE)
                                .build();

                // When
                busRepository.save(bus);
                Optional<Bus> found = busRepository.findByPlate("XYZ789");

                // Then
                assertThat(found).isPresent();
                assertThat(found.get().getPlate()).isEqualTo("XYZ789");
                assertThat(found.get().getCapacity()).isEqualTo(50);
                assertThat(found.get().getStatus()).isEqualTo(BusStatus.ACTIVE);
        }

        @Test
        @DisplayName("Should find buses by status")
        void shouldFindBusesByStatus() {
                // Given
                Bus activeBus = Bus.builder()
                                .plate("ACTIVE1")
                                .capacity(40)
                                .status(BusStatus.ACTIVE)
                                .build();

                Bus maintenanceBus = Bus.builder()
                                .plate("MAINT1")
                                .capacity(45)
                                .status(BusStatus.MAINTENANCE)
                                .build();

                busRepository.save(activeBus);
                busRepository.save(maintenanceBus);

                // When
                List<Bus> activeBuses = busRepository.findByStatus(BusStatus.ACTIVE);

                // Then
                assertThat(activeBuses).hasSize(1);
                assertThat(activeBuses.get(0).getStatus()).isEqualTo(BusStatus.ACTIVE);
        }

        @Test
        @DisplayName("Should find buses by capacity")
        void shouldFindBusesByCapacity() {
                // Given
                Bus smallBus = Bus.builder()
                                .plate("SMALL1")
                                .capacity(30)
                                .status(BusStatus.ACTIVE)
                                .build();

                Bus largeBus = Bus.builder()
                                .plate("LARGE1")
                                .capacity(50)
                                .status(BusStatus.ACTIVE)
                                .build();

                busRepository.save(smallBus);
                busRepository.save(largeBus);
        }
}
