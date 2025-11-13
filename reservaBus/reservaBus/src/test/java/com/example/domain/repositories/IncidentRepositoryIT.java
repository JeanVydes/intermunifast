package com.example.domain.repositories;

import com.example.domain.entities.Incident;
import com.example.domain.enums.EntityType;
import com.example.domain.enums.IncidentType;
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

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Testcontainers
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(TestJpaConfiguration.class)
@DisplayName("Incident Repository Integration Tests")
class IncidentRepositoryIT {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

    @Autowired
    private IncidentRepository incidentRepository;

    @Test
    @DisplayName("Should save and retrieve incident")
    void shouldSaveAndRetrieveIncident() {
        // Given
        Incident incident = Incident.builder()
                .type(IncidentType.SECURITY)
                .note("Security issue detected")
                .entityType(EntityType.TRIP)
                .entityId(1L)
                .build();

        // When
        Incident saved = incidentRepository.save(incident);

        // Then
        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getType()).isEqualTo(IncidentType.SECURITY);
        assertThat(saved.getNote()).isEqualTo("Security issue detected");
        assertThat(saved.getEntityType()).isEqualTo(EntityType.TRIP);
    }

    @Test
    @DisplayName("Should find incidents by entity type and ID")
    void shouldFindIncidentsByEntityTypeAndId() {
        // Given
        Incident incident1 = Incident.builder()
                .type(IncidentType.VEHICLE)
                .note("Vehicle breakdown")
                .entityType(EntityType.TRIP)
                .entityId(100L)
                .build();

        Incident incident2 = Incident.builder()
                .type(IncidentType.SECURITY)
                .note("Security check")
                .entityType(EntityType.TRIP)
                .entityId(100L)
                .build();

        incidentRepository.save(incident1);
        incidentRepository.save(incident2);

        // When
        List<Incident> incidents = incidentRepository.findByEntityTypeAndEntityId(EntityType.TRIP, 100L);

        // Then
        assertThat(incidents).hasSize(2);
        assertThat(incidents).allMatch(i -> i.getEntityId().equals(100L));
    }

    @Test
    @DisplayName("Should find incidents by type")
    void shouldFindIncidentsByType() {
        // Given
        Incident incident1 = Incident.builder()
                .type(IncidentType.DELIVERY_FAIL)
                .note("Delivery failed")
                .entityType(EntityType.PARCEL)
                .entityId(1L)
                .build();

        Incident incident2 = Incident.builder()
                .type(IncidentType.DELIVERY_FAIL)
                .note("Another delivery failure")
                .entityType(EntityType.PARCEL)
                .entityId(2L)
                .build();

        incidentRepository.save(incident1);
        incidentRepository.save(incident2);

        // When
        List<Incident> incidents = incidentRepository.findByType(IncidentType.DELIVERY_FAIL);

        // Then
        assertThat(incidents).hasSizeGreaterThanOrEqualTo(2);
        assertThat(incidents).allMatch(i -> i.getType() == IncidentType.DELIVERY_FAIL);
    }

    @Test
    @DisplayName("Should find incidents for different entity types")
    void shouldFindIncidentsForDifferentEntityTypes() {
        // Given
        Incident tripIncident = Incident.builder()
                .type(IncidentType.VEHICLE)
                .note("Trip issue")
                .entityType(EntityType.TRIP)
                .entityId(1L)
                .build();

        Incident ticketIncident = Incident.builder()
                .type(IncidentType.OVERBOOK)
                .note("Ticket issue")
                .entityType(EntityType.TICKET)
                .entityId(1L)
                .build();

        Incident parcelIncident = Incident.builder()
                .type(IncidentType.DELIVERY_FAIL)
                .note("Parcel issue")
                .entityType(EntityType.PARCEL)
                .entityId(1L)
                .build();

        incidentRepository.save(tripIncident);
        incidentRepository.save(ticketIncident);
        incidentRepository.save(parcelIncident);

        // When
        List<Incident> tripIncidents = incidentRepository.findByEntityTypeAndEntityId(EntityType.TRIP, 1L);
        List<Incident> ticketIncidents = incidentRepository.findByEntityTypeAndEntityId(EntityType.TICKET, 1L);
        List<Incident> parcelIncidents = incidentRepository.findByEntityTypeAndEntityId(EntityType.PARCEL, 1L);

        // Then
        assertThat(tripIncidents).hasSize(1);
        assertThat(ticketIncidents).hasSize(1);
        assertThat(parcelIncidents).hasSize(1);
    }
}
