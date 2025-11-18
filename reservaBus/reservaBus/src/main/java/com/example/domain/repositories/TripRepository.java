package com.example.domain.repositories;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.domain.entities.Trip;
import com.example.domain.enums.TripStatus;

public interface TripRepository extends JpaRepository<Trip, Long> {

    List<Trip> findByRoute_Id(Long routeId);

    List<Trip> findByBus_Id(Long busId);

    List<Trip> findByStatus(TripStatus status);

    @Query("SELECT t FROM Trip t WHERE t.route.id = :routeId AND DATE(t.departureAt) = DATE(:departureDate)")
    List<Trip> findByRouteIdAndDepartureDate(@Param("routeId") Long routeId,
            @Param("departureDate") LocalDateTime departureDate);

    @Query("SELECT t FROM Trip t WHERE t.departureAt BETWEEN :now AND :futureTime")
    List<Trip> findStartingTripsInNextMinutes(@Param("now") LocalDateTime now,
            @Param("futureTime") LocalDateTime futureTime);

    @Query("SELECT t FROM Trip t JOIN t.parcels p WHERE p.fromStop.id = :fromStopId AND p.toStop.id = :toStopId")
    Trip findByParcelsFromStop_IdAndParcelsToStop_Id(@Param("fromStopId") Long fromStopId,
            @Param("toStopId") Long toStopId);

    /**
     * NOTE: QUERY MADE WITH HELP OF AI
     * 
     * Searches for available trips between origin and destination stops.
     * 
     * Search logic:
     * 1. Matches routes where route origin/destination match the search terms
     * 2. OR matches routes with stops in correct sequence order
     * 
     * Optimizations applied:
     * 1. Single JOIN FETCH for route to avoid N+1 queries
     * 2. Subquery for stop validation to prevent cartesian product
     * 3. Index hints via proper WHERE clause ordering
     * 4. Status filter for only bookable trips
     * 5. Future departure filter with optional date match
     * 
     * @param origin             Stop name to search from (case-insensitive partial
     *                           match)
     * @param destination        Stop name to search to (case-insensitive partial
     *                           match)
     * @param departureDateStart Optional start of day filter (can be null)
     * @param departureDateEnd   Optional end of day filter (can be null)
     * @return List of available trips matching criteria
     */
    @Query("""
                SELECT DISTINCT t FROM Trip t
                JOIN FETCH t.route r
                WHERE t.status IN (com.example.domain.enums.TripStatus.SCHEDULED, com.example.domain.enums.TripStatus.BOARDING)
                AND t.departureAt >= CURRENT_TIMESTAMP
                AND (CAST(:departureDateStart AS timestamp) IS NULL OR t.departureAt >= :departureDateStart)
                AND (CAST(:departureDateEnd AS timestamp) IS NULL OR t.departureAt < :departureDateEnd)
                AND (
                    (LOWER(r.origin) LIKE LOWER(CONCAT('%', :origin, '%'))
                     AND LOWER(r.destination) LIKE LOWER(CONCAT('%', :destination, '%')))
                    OR
                    (EXISTS (
                        SELECT 1 FROM Stop sFrom
                        WHERE sFrom.route.id = r.id
                        AND LOWER(sFrom.name) LIKE LOWER(CONCAT('%', :origin, '%'))
                    )
                    AND EXISTS (
                        SELECT 1 FROM Stop sTo
                        WHERE sTo.route.id = r.id
                        AND LOWER(sTo.name) LIKE LOWER(CONCAT('%', :destination, '%'))
                        AND sTo.sequence > (
                            SELECT COALESCE(MAX(sf.sequence), -1) FROM Stop sf
                            WHERE sf.route.id = r.id
                            AND LOWER(sf.name) LIKE LOWER(CONCAT('%', :origin, '%'))
                        )
                    ))
                )
                ORDER BY t.departureAt ASC
            """)
    List<Trip> searchAvailableTrips(
            @Param("origin") String origin,
            @Param("destination") String destination,
            @Param("departureDateStart") LocalDateTime departureDateStart,
            @Param("departureDateEnd") LocalDateTime departureDateEnd);

    // Real-time metrics queries - show ALL trips
    @Query("SELECT COUNT(t) FROM Trip t")
    Long countTripsByPeriod(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

    @Query("SELECT t FROM Trip t")
    List<Trip> findTripsByPeriod(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

}
