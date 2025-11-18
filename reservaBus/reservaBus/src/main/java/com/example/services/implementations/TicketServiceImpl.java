package com.example.services.implementations;

import java.time.LocalDateTime;
import java.time.temporal.TemporalAmount;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.api.dto.BaggageDTOs;
import com.example.api.dto.IncidentDTOs;
import com.example.api.dto.TicketDTOs;
import com.example.domain.entities.Account;
import com.example.domain.entities.Bus;
import com.example.domain.entities.FareRule;
import com.example.domain.entities.Route;
import com.example.domain.entities.Stop;
import com.example.domain.entities.Ticket;
import com.example.domain.entities.Trip;
import com.example.domain.enums.PaymentStatus;
import com.example.domain.enums.TicketStatus;
import com.example.domain.repositories.AccountRepository;
import com.example.domain.repositories.BaggageRepository;
import com.example.domain.repositories.FareRuleRepository;
import com.example.domain.repositories.IncidentRepository;
import com.example.domain.repositories.StopRepository;
import com.example.domain.repositories.TicketRepository;
import com.example.domain.repositories.TripRepository;
import com.example.exceptions.NotFoundException;
import com.example.security.services.AuthenticationService;
import com.example.services.definitions.TicketService;
import com.example.services.extra.SeatAvailabilityService;
import com.example.services.mappers.BaggageMapper;
import com.example.services.mappers.IncidentMapper;
import com.example.services.mappers.TicketMapper;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional

public class TicketServiceImpl implements TicketService {

    private static final String TICKET_NOT_FOUND = "Ticket %d not found";

    private final TicketRepository repo;
    private final AccountRepository accountRepository;
    private final TicketMapper mapper;
    private final BaggageMapper baggageMapper;
    private final BaggageRepository baggageRepo;
    private final IncidentMapper incidentMapper;
    private final IncidentRepository incidentRepo;
    private final TripRepository tripRepo;
    private final StopRepository stopRepo;
    private final AuthenticationService authenticationService;
    private final FareRuleRepository fareRuleRepo;
    private final SeatAvailabilityService seatAvailabilityService;

    @Override
    public TicketDTOs.TicketResponse createTicket(TicketDTOs.CreateTicketRequest req) {
        Account account = authenticationService.getCurrentAccount();

        Trip trip = tripRepo.findById(req.tripId())
                .orElseThrow(() -> new NotFoundException("Trip %d not found".formatted(req.tripId())));

        Bus bus = trip.getBus();

        Stop fromStop = null;
        if (req.fromStopId().isPresent()) {
            fromStop = stopRepo.findById(req.fromStopId().get())
                    .orElseThrow(() -> new NotFoundException("Stop %d not found".formatted(req.fromStopId().get())));
        }

        Stop toStop = null;
        if (req.toStopId().isPresent()) {
            toStop = stopRepo.findById(req.toStopId().get())
                    .orElseThrow(() -> new NotFoundException("Stop %d not found".formatted(req.toStopId().get())));
        }

        List<Ticket> existingTickets = repo.findTicketsSameTripAndStops(
                req.tripId(),
                fromStop != null ? fromStop.getId() : null,
                toStop != null ? toStop.getId() : null);

        if (bus.getCapacity() <= existingTickets.size()) {
            throw new IllegalStateException("No seats available for trip %d".formatted(req.tripId()));
        }

        TicketStatus status = TicketStatus.CONFIRMED;
        double percentageFull = (double) existingTickets.size() / bus.getCapacity();
        if (percentageFull > 0.95) {
            status = TicketStatus.PENDING_APPROVAL;
        }

        // Validar disponibilidad del asiento para el tramo especificado
        if (existingTickets.stream().anyMatch(t -> t.getSeatNumber().equals(req.seatNumber()))) {
            String reason = seatAvailabilityService.getAvailabilityConflictReason(
                    req.tripId(), req.seatNumber(), fromStop, toStop);
            String fromStopName = fromStop != null ? fromStop.getName() : "origin";
            String toStopName = toStop != null ? toStop.getName() : "destination";
            throw new IllegalStateException(
                    "Seat %s is not available for trip %d (segment %s -> %s): %s"
                            .formatted(req.seatNumber(), req.tripId(), fromStopName, toStopName, reason));
        }

        Route route = trip.getRoute();
        FareRule fareRule = fareRuleRepo.findByRouteId(route.getId());

        // Si no existe FareRule para esta ruta, crear una por defecto
        if (fareRule == null) {
            // Precio base: distancia * precio por km (sin descuentos)
            Double basePrice = route.getDistanceKm() * route.getPricePerKm();

            fareRule = FareRule.builder()
                    .route(route)
                    .basePrice(basePrice)
                    .dynamicPricing(false)
                    .childrenDiscount(0.25) // 25% descuento para niños
                    .seniorDiscount(0.15) // 15% descuento para seniors
                    .studentDiscount(0.10) // 10% descuento para estudiantes
                    .build();
            fareRule = fareRuleRepo.save(fareRule);

            System.out.println("⚠️  FareRule creada automáticamente para ruta " + route.getId() + " (" + route.getName()
                    + ") con precio base: $" + basePrice);
        }

        Double discount = 0.0;

        switch (req.passengerType()) {
            case ADULT -> {
                // no discount
            }
            case CHILD -> {
                discount = fareRule.getChildrenDiscount();
            }
            case SENIOR -> {
                discount = fareRule.getSeniorDiscount();
            }
            case STUDENT -> {
                discount = fareRule.getStudentDiscount();
            }
        }

        Double price = route.getDistanceKm() * route.getPricePerKm() * (1 - discount);

        Ticket ticket = Ticket.builder()
                .seatNumber(req.seatNumber())
                .trip(trip)
                .fromStop(fromStop)
                .toStop(toStop)
                .paymentMethod(req.paymentMethod())
                .paymentIntentId(req.paymentIntentId())
                .account(accountRepository.getReferenceById(account.getId()))
                .price(price)
                .status(status)
                .paymentStatus(PaymentStatus.PENDING)
                .qrCode(null)
                .build();

        return mapper.toResponse(repo.save(ticket));
    }

    @Override
    @Transactional(readOnly = true)
    public TicketDTOs.TicketResponse getTicketById(Long id) {
        return repo.findById(id).map(mapper::toResponse)
                .orElseThrow(() -> new NotFoundException(TICKET_NOT_FOUND.formatted(id)));
    }

    @Override
    public void deleteTicket(Long id) {
        var ticket = repo.findById(id)
                .orElseThrow(() -> new NotFoundException(TICKET_NOT_FOUND.formatted(id)));
        repo.delete(ticket);
    }

    @Override
    public TicketDTOs.TicketResponse updateTicket(Long id, TicketDTOs.UpdateTicketRequest req) {
        if (req.tripId() != null) {
            tripRepo.findById(req.tripId())
                    .orElseThrow(() -> new NotFoundException("Trip %d not found".formatted(req.tripId())));
        }
        if (req.fromStopId() != null) {
            stopRepo.findById(req.fromStopId())
                    .orElseThrow(() -> new NotFoundException("Stop %d not found".formatted(req.fromStopId())));
        }
        if (req.toStopId() != null) {
            stopRepo.findById(req.toStopId())
                    .orElseThrow(() -> new NotFoundException("Stop %d not found".formatted(req.toStopId())));
        }

        var ticket = repo.findById(id)
                .orElseThrow(() -> new NotFoundException(TICKET_NOT_FOUND.formatted(id)));

        Account account = authenticationService.getCurrentAccount();
        if (!ticket.getAccount().getId().equals(account.getId())) {
            throw new NotFoundException(TICKET_NOT_FOUND.formatted(id));
        }

        if (req.passengerType() != null) {
            Route route = ticket.getTrip().getRoute();
            FareRule fareRule = fareRuleRepo.findByRouteId(route.getId());

            // Si no existe FareRule para esta ruta, crear una por defecto
            if (fareRule == null) {
                // Precio base: distancia * precio por km (sin descuentos)
                Double basePrice = route.getDistanceKm() * route.getPricePerKm();

                fareRule = FareRule.builder()
                        .route(route)
                        .basePrice(basePrice)
                        .dynamicPricing(false)
                        .childrenDiscount(0.25) // 25% descuento para niños
                        .seniorDiscount(0.15) // 15% descuento para seniors
                        .studentDiscount(0.10) // 10% descuento para estudiantes
                        .build();
                fareRule = fareRuleRepo.save(fareRule);

                System.out.println("⚠️  FareRule creada automáticamente para ruta " + route.getId() + " ("
                        + route.getName() + ") con precio base: $" + basePrice);
            }

            Double discount = 0.0;

            switch (req.passengerType()) {
                case ADULT -> {
                    // no discount
                }
                case CHILD -> {
                    discount = fareRule.getChildrenDiscount();
                }
                case SENIOR -> {
                    discount = fareRule.getSeniorDiscount();
                }
                case STUDENT -> {
                    discount = fareRule.getStudentDiscount();
                }
            }

            Double price = route.getDistanceKm() * route.getPricePerKm() * (1 - discount);
            ticket.setPrice(price);
        }

        mapper.patch(ticket, req);
        return mapper.toResponse(repo.save(ticket));
    }

    @Override
    public TicketDTOs.TicketResponse cancelTicket(Long id) {
        var ticket = repo.findById(id)
                .orElseThrow(() -> new NotFoundException(TICKET_NOT_FOUND.formatted(id)));

        Account account = authenticationService.getCurrentAccount();
        if (!ticket.getAccount().getId().equals(account.getId())) {
            throw new NotFoundException(TICKET_NOT_FOUND.formatted(id));
        }

        Trip trip = ticket.getTrip();
        if (trip.getDepartureAt().isBefore(LocalDateTime.now())) {
            throw new IllegalStateException("Cannot cancel ticket for past trips");
        }

        // cannot cancel ticket 5 minutes before trip
        if (trip.getDepartureAt().isBefore(LocalDateTime.now().plusMinutes(5))) {
            throw new IllegalStateException("Cannot cancel ticket within 5 minutes of departure");
        }

        ticket.setStatus(TicketStatus.CANCELLED);
        return mapper.toResponse(repo.save(ticket));
    }

    @Override
    @Transactional(readOnly = true)
    public List<TicketDTOs.TicketResponse> searchTickets(Long accountId, String seatNumber) {
        if (accountId == null && seatNumber != null) {
            var tickets = repo.findBySeatNumber(seatNumber);
            return tickets.stream().map(mapper::toResponse).toList();
        }
        if (accountId != null && seatNumber == null) {
            var tickets = repo.findByAccount_Id(accountId);
            return tickets.stream().map(mapper::toResponse).toList();
        }
        if (accountId != null && seatNumber != null) {
            var tickets = repo.findByAccount_IdAndSeatNumber(accountId, seatNumber);
            return tickets.stream().map(mapper::toResponse).toList();
        }
        // Return all tickets when no parameters are provided
        var allTickets = repo.findAll();
        return allTickets.stream().map(mapper::toResponse).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<BaggageDTOs.BaggageResponse> getBaggagesByTicketId(Long id) {
        var ticket = repo.findById(id)
                .orElseThrow(() -> new NotFoundException(TICKET_NOT_FOUND.formatted(id)));
        return baggageRepo.findByTicket_Id(ticket.getId()).stream()
                .map(baggageMapper::toResponse)
                .toList();
    }

    @Override
    public List<IncidentDTOs.IncidentResponse> getIncidentsByTicketId(Long id) {
        var ticket = repo.findById(id)
                .orElseThrow(() -> new NotFoundException(TICKET_NOT_FOUND.formatted(id)));
        return incidentRepo.findByTicketId(ticket.getId()).stream()
                .map(incidentMapper::toResponse)
                .toList();
    }

    @Override
    public TicketDTOs.TicketResponse markTicketAsPaid(Long id, String paymentIntentId) {
        var ticket = repo.findById(id)
                .orElseThrow(() -> new NotFoundException(TICKET_NOT_FOUND.formatted(id)));

        Account account = authenticationService.getCurrentAccount();
        if (!ticket.getAccount().getId().equals(account.getId())) {
            throw new NotFoundException(TICKET_NOT_FOUND.formatted(id));
        }

        if (ticket.getPaymentStatus() == PaymentStatus.COMPLETED) {
            throw new IllegalStateException("Ticket is already paid");
        }

        if (ticket.getStatus() == TicketStatus.CANCELLED) {
            throw new IllegalStateException("Cannot pay for a cancelled ticket");
        }

        // Update ticket status to SOLD
        ticket.setPaymentStatus(PaymentStatus.COMPLETED);

        // Set payment intent ID if provided
        if (paymentIntentId != null && !paymentIntentId.isBlank()) {
            ticket.setPaymentIntentId(paymentIntentId);
        }

        // Generate QR code if not exists
        if (ticket.getQrCode() == null || ticket.getQrCode().isBlank()) {
            ticket.setQrCode(generateQrCode(ticket));
        }

        return mapper.toResponse(repo.save(ticket));
    }

    @Override
    public List<TicketDTOs.TicketResponse> markMultipleTicketsAsPaid(List<Long> ticketIds, String paymentIntentId) {
        if (ticketIds == null || ticketIds.isEmpty()) {
            throw new IllegalArgumentException("Ticket IDs list cannot be empty");
        }

        Account account = authenticationService.getCurrentAccount();
        List<Ticket> tickets = repo.findAllById(ticketIds);

        if (tickets.size() != ticketIds.size()) {
            throw new NotFoundException("Some tickets were not found");
        }

        // Verify all tickets belong to current user and are unpaid
        validateTicketsForPayment(tickets, account);

        // Update all tickets
        for (Ticket ticket : tickets) {
            ticket.setPaymentStatus(PaymentStatus.COMPLETED);

            if (paymentIntentId != null && !paymentIntentId.isBlank()) {
                ticket.setPaymentIntentId(paymentIntentId);
            }

            if (ticket.getQrCode() == null || ticket.getQrCode().isBlank()) {
                ticket.setQrCode(generateQrCode(ticket));
            }
        }

        List<Ticket> savedTickets = repo.saveAll(tickets);
        return savedTickets.stream()
                .map(mapper::toResponse)
                .toList();
    }

    private void validateTicketsForPayment(List<Ticket> tickets, Account account) {
        for (Ticket ticket : tickets) {
            if (!ticket.getAccount().getId().equals(account.getId())) {
                throw new NotFoundException(TICKET_NOT_FOUND.formatted(ticket.getId()));
            }

            if (ticket.getStatus() == TicketStatus.CANCELLED) {
                throw new IllegalStateException("Cannot pay for cancelled ticket %d".formatted(ticket.getId()));
            }

            if (ticket.getPaymentStatus() == PaymentStatus.COMPLETED) {
                throw new IllegalStateException("Ticket %d is already paid".formatted(ticket.getId()));
            }
        }
    }

    @Override
    public List<TicketDTOs.TicketResponse> getTicketsForCurrentUser(String status) {
        Account account = authenticationService.getCurrentAccount();

        if (status != null && !status.isBlank()) {
            try {
                TicketStatus ticketStatus = TicketStatus.valueOf(status.toUpperCase());
                return repo.findByAccount_IdAndStatus(account.getId(), ticketStatus).stream()
                        .map(mapper::toResponse)
                        .toList();
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException("Invalid ticket status: " + status);
            }
        }

        return repo.findByAccount_Id(account.getId()).stream()
                .map(mapper::toResponse)
                .toList();
    }

    /**
     * Generates a unique QR code for a ticket
     * Format: TICKET-{ticketId}-{timestamp}
     */
    private String generateQrCode(Ticket ticket) {
        return "TICKET-%d-%d".formatted(ticket.getId(), System.currentTimeMillis());
    }

    @Override
    public TicketDTOs.TicketResponse approveTicket(Long id) {
        var ticket = repo.findById(id)
                .orElseThrow(() -> new NotFoundException(TICKET_NOT_FOUND.formatted(id)));

        if (ticket.getStatus() != TicketStatus.PENDING_APPROVAL) {
            throw new IllegalStateException("Only tickets with PENDING_APPROVAL status can be approved");
        }

        ticket.setStatus(TicketStatus.CONFIRMED);
        return mapper.toResponse(repo.save(ticket));
    }

    @Override
    public TicketDTOs.TicketResponse cancelPendingTicket(Long id) {
        var ticket = repo.findById(id)
                .orElseThrow(() -> new NotFoundException(TICKET_NOT_FOUND.formatted(id)));

        if (ticket.getStatus() != TicketStatus.PENDING_APPROVAL) {
            throw new IllegalStateException("Only tickets with PENDING_APPROVAL status can be cancelled");
        }

        ticket.setStatus(TicketStatus.CANCELLED);
        return mapper.toResponse(repo.save(ticket));
    }

    @Override
    @Transactional(readOnly = true)
    public List<TicketDTOs.TicketResponse> getPendingApprovalTickets() {
        return repo.findByStatus(TicketStatus.PENDING_APPROVAL).stream()
                .map(mapper::toResponse)
                .toList();
    }

}
