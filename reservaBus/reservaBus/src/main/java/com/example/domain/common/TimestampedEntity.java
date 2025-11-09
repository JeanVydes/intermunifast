package com.example.domain.common;

import java.time.Instant;

import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@MappedSuperclass
@Getter
@Setter
@NoArgsConstructor
public abstract class TimestampedEntity {
    @Column(nullable = false, updatable = false)
    private Long createdAt = Instant.now().toEpochMilli();

    @Column(nullable = false)
    private Long updatedAt = Instant.now().toEpochMilli();
}