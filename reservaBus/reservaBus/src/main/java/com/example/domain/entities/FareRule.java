package com.example.domain.entities;

import com.example.domain.common.TimestampedEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "fare_rules")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder

public class FareRule extends TimestampedEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column(nullable = false)
    private Double basePrice;

    @Column(nullable = false)
    private Double childrenDiscount;

    @Column(nullable = false)
    private Double seniorDiscount;

    @Column(nullable = false)
    private Double studentDiscount;

    @Column(nullable = false)
    private Boolean dynamicPricing;

    @OneToOne
    private Route route;
}
