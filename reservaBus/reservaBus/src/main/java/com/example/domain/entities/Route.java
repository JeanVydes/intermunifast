package com.example.domain.entities;

import java.util.ArrayList;
import java.util.List;

import com.example.domain.common.TimestampedEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "routes")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder

public class Route extends TimestampedEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column(nullable = false)
    private String code;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String origin;

    @Column(nullable = false)
    private String destination;

    @Column(nullable = false)
    private Integer durationMinutes;

    @Column(nullable = false)
    private Double distanceKm;

    @Column(nullable = false)
    private Double pricePerKm;

    @OneToMany(mappedBy = "route")
    @Builder.Default
    private List<Stop> stops = new ArrayList<>();

    @OneToMany(mappedBy = "route")
    @Builder.Default
    private List<Trip> trips = new ArrayList<>();

    @OneToOne(mappedBy = "route")
    private FareRule fareRule;
}
