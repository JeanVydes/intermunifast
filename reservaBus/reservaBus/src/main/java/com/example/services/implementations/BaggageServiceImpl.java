package com.example.services.implementations;

import java.math.BigDecimal;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.api.dto.BaggageDTOs;
import com.example.domain.entities.Baggage;
import com.example.domain.repositories.BaggageRepository;
import com.example.domain.repositories.TicketRepository;
import com.example.exceptions.NotFoundException;
import com.example.services.definitions.BaggageService;
import com.example.services.extra.ConfigCacheService;
import com.example.services.mappers.BaggageMapper;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class BaggageServiceImpl implements BaggageService {

    private final BaggageRepository repo;
    private final BaggageMapper mapper;
    private final TicketRepository ticketRepo;
    private final ConfigCacheService configCache;

    @Override
    public BaggageDTOs.BaggageResponse createBaggage(BaggageDTOs.CreateBaggageRequest req) {
        var ticket = ticketRepo.findById(req.ticketId())
                .orElseThrow(() -> new NotFoundException("Ticket %d not found".formatted(req.ticketId())));

        double maxWeightKg = configCache.getMaxBaggageWeightKg();
        double feePercentage = configCache.getBaggageFeePercentage();

        var fee = BigDecimal.ZERO;
        if (req.weightKg() > maxWeightKg) {
            var ticketPrice = ticket.getPrice();
            double extraFee = (req.weightKg() - maxWeightKg) * (ticketPrice * feePercentage);
            fee = BigDecimal.valueOf(extraFee);
            ticket.setPrice(ticketPrice + extraFee);
            ticketRepo.save(ticket);
        }

        var baggage = Baggage.builder()
                .weight(req.weightKg())
                .fee(fee)
                .tagCode(req.tagCode())
                .ticket(ticket)
                .build();
        return mapper.toResponse(repo.save(baggage));
    }

    @Override
    @Transactional(readOnly = true)
    public BaggageDTOs.BaggageResponse getBaggageById(Long id) {
        return repo.findById(id).map(mapper::toResponse)
                .orElseThrow(() -> new NotFoundException("Baggage %d not found".formatted(id)));
    }

    @Override
    public void deleteBaggage(Long id) {
        var baggage = repo.findById(id)
                .orElseThrow(() -> new NotFoundException("Baggage %d not found".formatted(id)));
        repo.delete(baggage);
    }

    @Override
    public BaggageDTOs.BaggageResponse updateBaggage(Long id, BaggageDTOs.UpdateBaggageRequest req) {
        var baggage = repo.findById(id)
                .orElseThrow(() -> new NotFoundException("Baggage %d not found".formatted(id)));

        if (req.ticketId() != null && !ticketRepo.existsById(req.ticketId())) {
            throw new NotFoundException("Ticket %d not found".formatted(req.ticketId()));
        }

        mapper.patch(baggage, req);
        return mapper.toResponse(repo.save(baggage));
    }
}