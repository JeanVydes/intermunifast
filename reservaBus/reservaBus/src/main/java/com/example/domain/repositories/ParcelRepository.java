package com.example.domain.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.domain.entities.Parcel;
import com.example.domain.enums.ParcelStatus;

public interface ParcelRepository extends JpaRepository<Parcel, Long> {

    List<Parcel> findByFromStop_IdAndToStop_Id(Long fromStopId, Long toStopId);

    Parcel findByCode(String code);

    List<Parcel> findByStatus(ParcelStatus status);
}
