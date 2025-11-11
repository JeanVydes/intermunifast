package com.example.domain.entities;

import java.util.ArrayList;
import java.util.List;

import com.example.domain.common.TimestampedEntity;
import com.example.domain.enums.AccountRole;
import com.example.domain.enums.AccountStatus;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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
@Table(name = "accounts")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder

public class Account extends TimestampedEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String email;

    @Column(nullable = false)
    private String phone;

    @Column(nullable = false)
    private AccountRole role;

    @Column(nullable = false)
    private AccountStatus status;

    @Column(nullable = false)
    private String passwordHash;

    @OneToMany(mappedBy = "account")
    @Builder.Default
    private List<SeatHold> seatHolds = new ArrayList<>();

    @OneToMany(mappedBy = "passenger")
    @Builder.Default
    private List<Ticket> tickets = new ArrayList<>();

    @OneToMany(mappedBy = "driver")
    @Builder.Default
    private List<Assignment> driverAssignments = new ArrayList<>();

    @OneToMany(mappedBy = "dispatcher")
    @Builder.Default
    private List<Assignment> dispatcherAssignments = new ArrayList<>();

}
