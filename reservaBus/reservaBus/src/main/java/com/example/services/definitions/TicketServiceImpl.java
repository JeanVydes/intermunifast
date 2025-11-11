package com.example.services.definitions;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.api.dto.BaggageDTOs;
import com.example.api.dto.IncidentDTOs;
import com.example.api.dto.TicketDTOs;
import com.example.domain.repositories.BaggageRepository;
import com.example.domain.repositories.IncidentRepository;
import com.example.domain.repositories.TicketRepository;
import com.example.exceptions.NotFoundException;
import com.example.services.mappers.BaggageMapper;
import com.example.services.mappers.IncidentMapper;
import com.example.services.mappers.TicketMapper;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional

public class TicketServiceImpl implements TicketService {

    private final TicketRepository repo;
    private final TicketMapper mapper;
    private final BaggageMapper baggageMapper;
    private final BaggageRepository baggageRepo;
    private final IncidentMapper incidentMapper;
    private final IncidentRepository incidentRepo;

    @Override 
    public TicketDTOs.TicketResponse createTicket(TicketDTOs.CreateTicketRequest req) {
        var ticket = mapper.toEntity(req);
        return mapper.toResponse(repo.save(ticket));
    }

    @Override
    @Transactional(readOnly = true)
    public TicketDTOs.TicketResponse getTicketById(Long id) {
        return repo.findById(id).map(mapper::toResponse)
                .orElseThrow(() -> new NotFoundException("Ticket %d not found".formatted(id)));
    }

    @Override
    public void deleteTicket(Long id) {
        var ticket = repo.findById(id)
                .orElseThrow(() -> new NotFoundException("Ticket %d not found".formatted(id)));
        repo.delete(ticket);
    }

    @Override
    public TicketDTOs.TicketResponse updateTicket(Long id, TicketDTOs.UpdateTicketRequest req) {
        var ticket = repo.findById(id)
                .orElseThrow(() -> new NotFoundException("Ticket %d not found".formatted(id)));
        mapper.patch( ticket, req);
        return mapper.toResponse(repo.save(ticket));
    }

    @Override
    @Transactional(readOnly = true)
    public List<TicketDTOs.TicketResponse> searchTickets(Long accountId, String seatNumber) {
        if(accountId == null && seatNumber != null) {
            var tickets = repo.findBySeatNumber(seatNumber);
            return tickets.stream().map(mapper::toResponse).toList();
        }
        if(accountId != null && seatNumber == null) {
            var tickets = repo.findByAccountId(accountId);
            return tickets.stream().map(mapper::toResponse).toList();
        }
        if(accountId != null && seatNumber != null) {
            var tickets = repo.findByAccountIdAndSeatNumber(accountId, seatNumber);
            return tickets.stream().map(mapper::toResponse).toList();
        }
        var tickets = repo.findAll();
        return tickets.stream().map(mapper::toResponse).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<BaggageDTOs.BaggageResponse> getBaggagesByTicketId(Long id) {
        var ticket = repo.findById(id)
                .orElseThrow(() -> new NotFoundException("Ticket %d not found".formatted(id)));
        return baggageRepo.findByTicketId(ticket.getId()).stream()
                .map(baggageMapper::toResponse)
                        .toList();
    }

    @Override
    public List<IncidentDTOs.IncidentResponse> getIncidentsByTicketId(Long id) {
        var ticket = repo.findById(id)
                .orElseThrow(() -> new NotFoundException("Ticket %d not found".formatted(id)));
        return incidentRepo.findByTicketId(ticket.getId()).stream()
                .map(incidentMapper::toResponse)
                        .toList();
    }

}
