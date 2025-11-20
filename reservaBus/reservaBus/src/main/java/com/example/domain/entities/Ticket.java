package com.example.domain.entities;

import java.util.ArrayList;
import java.util.List;

import com.example.domain.common.TimestampedEntity;
import com.example.domain.enums.FareRulePassengerType;
import com.example.domain.enums.PaymentMethod;
import com.example.domain.enums.PaymentStatus;
import com.example.domain.enums.TicketStatus;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "tickets")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder

public class Ticket extends TimestampedEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column(nullable = false)
    private String seatNumber;

    @Column(nullable = false)
    private Double price;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TicketStatus status;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentMethod paymentMethod;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentStatus paymentStatus;

    @Column(nullable = true)
    private String paymentIntentId;

    @Column(nullable = true)
    private String qrCode;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private FareRulePassengerType passengerType;

    @ManyToOne(optional = false)
    @JoinColumn(name = "trip_id", nullable = false)
    private Trip trip;

    @ManyToOne(optional = false)
    @JoinColumn(name = "account_id", nullable = false)
    private Account account;

    // if there is not fromStop, mean the passenger boards at the route origin
    @ManyToOne(optional = true)
    @JoinColumn(name = "from_stop_id", nullable = true)
    private Stop fromStop;

    // if there is not toStop, mean the passenger alights at the route destination
    @ManyToOne(optional = true)
    @JoinColumn(name = "to_stop_id", nullable = true)
    private Stop toStop;

    @OneToMany(mappedBy = "ticket")
    @Builder.Default
    private List<Baggage> baggages = new ArrayList<>();

    @Transient
    private List<Incident> incidents;

}
