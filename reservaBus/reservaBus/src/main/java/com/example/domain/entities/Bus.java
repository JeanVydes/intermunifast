package com.example.domain.entities;

import java.util.ArrayList;
import java.util.List;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import com.example.domain.enums.BusStatus;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "buses")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder

public class Bus {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column(nullable = false)
    private String plate;

    @Column(nullable = false)
    private Integer capacity;

    @JdbcTypeCode(SqlTypes.JSON)
    @Builder.Default
    private List<Amenity> amenities = new ArrayList<>();

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private BusStatus status;

    @OneToMany(mappedBy = "bus")
    @Builder.Default
    private List<Seat> seats = new ArrayList<>();

    @OneToMany(mappedBy = "bus")
    @Builder.Default
    private List<Trip> trips = new ArrayList<>();

}
