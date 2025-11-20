package com.example.services.implementations;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.api.dto.ParcelDTOs;
import com.example.domain.entities.Parcel;
import com.example.domain.enums.ParcelStatus;
import com.example.domain.repositories.ParcelRepository;
import com.example.domain.repositories.StopRepository;
import com.example.exceptions.NotFoundException;
import com.example.services.definitions.ParcelService;
import com.example.services.mappers.ParcelMapper;
import com.example.utils.OtpUtil;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class ParcelServiceImpl implements ParcelService {

    private final ParcelRepository repo;
    private final ParcelMapper mapper;
    private final StopRepository stopRepo;

    @Override
    public ParcelDTOs.ParcelResponse createParcel(ParcelDTOs.CreateParcelRequest req) {
        var fromStop = stopRepo.findById(req.fromStopId())
                .orElseThrow(() -> new NotFoundException("Stop %d not found".formatted(req.fromStopId())));
        var toStop = stopRepo.findById(req.toStopId())
                .orElseThrow(() -> new NotFoundException("Stop %d not found".formatted(req.toStopId())));

        var parcel = Parcel.builder()
                .code(req.code())
                .senderName(req.senderName())
                .senderPhone(req.senderPhone())
                .receiverName(req.receiverName())
                .receiverPhone(req.receiverPhone())
                .price(0.0)
                .status(ParcelStatus.CREATED)
                .proofPhotoUrl("")
                .fromStop(fromStop)
                .toStop(toStop)
                .build();
        parcel.setDeliveryOtp(OtpUtil.generateBase32Otp(6));
        return mapper.toResponse(repo.save(parcel));
    }

    @Override
    @Transactional(readOnly = true)
    public ParcelDTOs.ParcelResponse getParcelById(Long id) {
        return repo.findById(id).map(mapper::toResponse)
                .orElseThrow(() -> new NotFoundException("Parcel %d not found".formatted(id)));
    }

    @Override
    public void deleteParcel(Long id) {
        var parcel = repo.findById(id)
                .orElseThrow(() -> new NotFoundException("Parcel %d not found".formatted(id)));
        repo.delete(parcel);
    }

    @Override
    public ParcelDTOs.ParcelResponse updateParcel(Long id, ParcelDTOs.UpdateParcelRequest req) {
        if (req.fromStopId() != null && !stopRepo.existsById(req.fromStopId())) {
            throw new NotFoundException("Stop %d not found".formatted(req.fromStopId()));
        }
        if (req.toStopId() != null && !stopRepo.existsById(req.toStopId())) {
            throw new NotFoundException("Stop %d not found".formatted(req.toStopId()));
        }
        if (req.proofPhotoUrl() == null || req.proofPhotoUrl().isBlank()) {
            throw new IllegalArgumentException("Proof photo URL cannot be blank");
        }

        var parcel = repo.findById(id)
                .orElseThrow(() -> new NotFoundException("Parcel %d not found".formatted(id)));
        mapper.patch(parcel, req);
        return mapper.toResponse(repo.save(parcel));
    }

    public boolean deliverParcel(Long id, ParcelDTOs.ParcelDeliveryRequest req) {
        var parcel = repo.findById(id)
                .orElseThrow(() -> new NotFoundException("Parcel %d not found".formatted(id)));

        if (parcel.getDeliveryOtp().equals(req.deliveryOtp()) && parcel.getStatus() != ParcelStatus.DELIVERED) {
            parcel.setStatus(ParcelStatus.DELIVERED);
            repo.save(parcel);
            return true;
        }
        return false;
    }
}
