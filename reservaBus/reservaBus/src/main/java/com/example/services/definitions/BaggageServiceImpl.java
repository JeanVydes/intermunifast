package com.example.services.definitions;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.api.dto.BaggageDTOs;
import com.example.domain.entities.Baggage;
import com.example.domain.entities.Ticket;
import com.example.domain.repositories.BaggageRepository;
import com.example.domain.repositories.TicketRepository;
import com.example.exceptions.NotFoundException;
import com.example.services.mappers.BaggageMapper;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional

public class BaggageServiceImpl implements BaggageService {

    private static final Integer MAX_WEIGHT_KG = 25;

    private final BaggageRepository repo;
    private final BaggageMapper mapper;
    private final TicketRepository ticketRepo;

    @Override
    public BaggageDTOs.BaggageResponse createBaggage(BaggageDTOs.CreateBaggageRequest req) {
        Ticket ticket = ticketRepo.findById(req.ticketId())
                .orElseThrow(() -> new NotFoundException("Ticket %d not found".formatted(req.ticketId())));
        if (req.weightKg() > MAX_WEIGHT_KG) {
            var ticketPrice = ticket.getPrice();
            ticket.setPrice(ticketPrice + ((req.weightKg() - 25) * (ticketPrice * 0.03)));
            ticketRepo.save(ticket);
        }
        Baggage baggage = Baggage.builder()
                .weight(req.weightKg())
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
        if (req.ticketId() != null) {
            ticketRepo.findById(req.ticketId())
                    .orElseThrow(() -> new NotFoundException("Ticket %d not found".formatted(req.ticketId())));
        }
        mapper.patch(baggage, req);
        return mapper.toResponse(repo.save(baggage));
    }

}