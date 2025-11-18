package com.example.domain.entities;

import java.time.LocalDateTime;

import com.example.domain.common.TimestampedEntity;
import com.example.domain.enums.TripStatus;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * TripLog entity to track historical trip completion data for metrics.
 * Records occupation, punctuality, and revenue data when trips are completed.
 */
@Entity
@Table(name = "trip_logs")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TripLog extends TimestampedEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "trip_id", nullable = false)
    private Trip trip;

    @Column(nullable = false)
    private LocalDateTime scheduledDeparture;

    @Column
    private LocalDateTime actualDeparture;

    @Column(nullable = false)
    private LocalDateTime scheduledArrival;

    @Column
    private LocalDateTime actualArrival;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TripStatus finalStatus;

    @Column(nullable = false)
    private Integer totalSeats;

    @Column(nullable = false)
    private Integer soldSeats;

    @Column(nullable = false)
    private Integer cancelledSeats;

    @Column(nullable = false)
    private Double occupationRate; // percentage

    @Column(nullable = false)
    private Double totalRevenue;

    @Column
    private Integer delayMinutes; // null if on time, positive if delayed

    @Column(nullable = false)
    private Boolean onTime; // true if departed within 15 minutes of schedule
}
