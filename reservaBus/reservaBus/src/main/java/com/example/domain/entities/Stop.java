package com.example.domain.entities;

import java.util.ArrayList;
import java.util.List;

import com.example.domain.common.TimestampedEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "stops")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder

public class Stop extends TimestampedEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private Integer sequence;

    @Column(nullable = false)
    private Double latitude;

    @Column(nullable = false)
    private Double longitude;

    @ManyToOne(optional = false)
    @JoinColumn(name = "route_id", nullable = false)
    private Route route;

    @OneToMany(mappedBy = "fromStop")
    @Builder.Default
    private List<Ticket> ticketsFrom = new ArrayList<>();

    @OneToMany(mappedBy = "toStop")
    @Builder.Default
    private List<Ticket> ticketsTo = new ArrayList<>();

    @OneToMany(mappedBy = "fromStop")
    @Builder.Default
    private List<Parcel> parcelsFrom = new ArrayList<>();

    @OneToMany(mappedBy = "toStop")
    @Builder.Default
    private List<Parcel> parcelsTo = new ArrayList<>();

}
