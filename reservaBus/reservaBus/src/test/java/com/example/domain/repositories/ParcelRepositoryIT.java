package com.example.domain.repositories;

import com.example.domain.entities.Parcel;
import com.example.domain.entities.Route;
import com.example.domain.entities.Stop;
import com.example.domain.enums.ParcelStatus;
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

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Testcontainers
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(TestJpaConfiguration.class)
@DisplayName("Parcel Repository Integration Tests")
class ParcelRepositoryIT {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

    @Autowired
    private ParcelRepository parcelRepository;

    @Autowired
    private RouteRepository routeRepository;

    @Autowired
    private StopRepository stopRepository;

    private Stop fromStop;
    private Stop toStop;

    @BeforeEach
    void setUp() {
        // Create route
        Route route = Route.builder()
                .code("RT001")
                .name("Test Route")
                .origin("City A")
                .destination("City B")
                .durationMinutes(120)
                .distanceKm(100.0)
                .pricePerKm(0.5)
                .build();
        route = routeRepository.save(route);

        // Create stops
        fromStop = Stop.builder()
                .name("Stop A")
                .latitude(-12.046373)
                .longitude(-77.042754)
                .route(route)
                .sequence(1)
                .build();
        fromStop = stopRepository.save(fromStop);

        toStop = Stop.builder()
                .name("Stop B")
                .latitude(-12.056373)
                .longitude(-77.052754)
                .route(route)
                .sequence(2)
                .build();
        toStop = stopRepository.save(toStop);
    }

    @Test
    @DisplayName("Should save and retrieve parcel")
    void shouldSaveAndRetrieveParcel() {
        // Given
        Parcel parcel = Parcel.builder()
                .code("PCL001")
                .senderName("John Sender")
                .senderPhone("123456789")
                .receiverName("Jane Receiver")
                .receiverPhone("987654321")
                .price(15.50)
                .status(ParcelStatus.CREATED)
                .proofPhotoUrl("http://example.com/photo.jpg")
                .deliveryOtp("1234")
                .fromStop(fromStop)
                .toStop(toStop)
                .build();

        // When
        Parcel saved = parcelRepository.save(parcel);

        // Then
        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getCode()).isEqualTo("PCL001");
        assertThat(saved.getSenderName()).isEqualTo("John Sender");
        assertThat(saved.getReceiverName()).isEqualTo("Jane Receiver");
        assertThat(saved.getStatus()).isEqualTo(ParcelStatus.CREATED);
    }

    @Test
    @DisplayName("Should find parcel by code")
    void shouldFindParcelByCode() {
        // Given
        Parcel parcel = Parcel.builder()
                .code("PCL002")
                .senderName("Alice")
                .senderPhone("111222333")
                .receiverName("Bob")
                .receiverPhone("444555666")
                .price(20.00)
                .status(ParcelStatus.IN_TRANSIT)
                .proofPhotoUrl("http://example.com/photo2.jpg")
                .deliveryOtp("5678")
                .fromStop(fromStop)
                .toStop(toStop)
                .build();
        parcelRepository.save(parcel);

        // When
        Parcel found = parcelRepository.findByCode("PCL002");

        // Then
        assertThat(found).isNotNull();
        assertThat(found.getCode()).isEqualTo("PCL002");
        assertThat(found.getSenderName()).isEqualTo("Alice");
    }

    @Test
    @DisplayName("Should find parcels by status")
    void shouldFindParcelsByStatus() {
        // Given
        Parcel parcel1 = Parcel.builder()
                .code("PCL003")
                .senderName("Sender1")
                .senderPhone("111111111")
                .receiverName("Receiver1")
                .receiverPhone("222222222")
                .price(10.00)
                .status(ParcelStatus.DELIVERED)
                .proofPhotoUrl("http://example.com/photo3.jpg")
                .deliveryOtp("1111")
                .fromStop(fromStop)
                .toStop(toStop)
                .build();

        Parcel parcel2 = Parcel.builder()
                .code("PCL004")
                .senderName("Sender2")
                .senderPhone("333333333")
                .receiverName("Receiver2")
                .receiverPhone("444444444")
                .price(12.00)
                .status(ParcelStatus.DELIVERED)
                .proofPhotoUrl("http://example.com/photo4.jpg")
                .deliveryOtp("2222")
                .fromStop(fromStop)
                .toStop(toStop)
                .build();

        parcelRepository.save(parcel1);
        parcelRepository.save(parcel2);

        // When
        List<Parcel> parcels = parcelRepository.findByStatus(ParcelStatus.DELIVERED);

        // Then
        assertThat(parcels).hasSizeGreaterThanOrEqualTo(2);
        assertThat(parcels).allMatch(p -> p.getStatus() == ParcelStatus.DELIVERED);
    }

    @Test
    @DisplayName("Should find parcels by from stop and to stop")
    void shouldFindParcelsByFromStopAndToStop() {
        // Given
        Parcel parcel1 = Parcel.builder()
                .code("PCL005")
                .senderName("Sender3")
                .senderPhone("555555555")
                .receiverName("Receiver3")
                .receiverPhone("666666666")
                .price(15.00)
                .status(ParcelStatus.CREATED)
                .proofPhotoUrl("http://example.com/photo5.jpg")
                .deliveryOtp("3333")
                .fromStop(fromStop)
                .toStop(toStop)
                .build();

        Parcel parcel2 = Parcel.builder()
                .code("PCL006")
                .senderName("Sender4")
                .senderPhone("777777777")
                .receiverName("Receiver4")
                .receiverPhone("888888888")
                .price(18.00)
                .status(ParcelStatus.IN_TRANSIT)
                .proofPhotoUrl("http://example.com/photo6.jpg")
                .deliveryOtp("4444")
                .fromStop(fromStop)
                .toStop(toStop)
                .build();

        parcelRepository.save(parcel1);
        parcelRepository.save(parcel2);

        // When
        List<Parcel> parcels = parcelRepository.findByFromStop_IdAndToStop_Id(fromStop.getId(), toStop.getId());

        // Then
        assertThat(parcels).hasSize(2);
        assertThat(parcels).allMatch(p -> p.getFromStop().getId().equals(fromStop.getId()) &&
                p.getToStop().getId().equals(toStop.getId()));
    }

    @Test
    @DisplayName("Should return empty list when no parcels match route")
    void shouldReturnEmptyListWhenNoMatchingParcels() {
        // When
        List<Parcel> parcels = parcelRepository.findByFromStop_IdAndToStop_Id(999L, 888L);

        // Then
        assertThat(parcels).isEmpty();
    }
}
