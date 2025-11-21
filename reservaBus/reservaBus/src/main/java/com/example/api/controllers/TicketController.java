package com.example.api.controllers;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;

import com.example.api.dto.BaggageDTOs;
import com.example.api.dto.IncidentDTOs;
import com.example.api.dto.TicketDTOs;
import com.example.services.definitions.TicketService;

@RestController
@RequestMapping("/api/tickets")
public class TicketController {
    private final TicketService ticketService;

    public TicketController(TicketService ticketService) {
        this.ticketService = ticketService;
    }

    @PostMapping
    public ResponseEntity<TicketDTOs.TicketResponse> create(
            @Validated @RequestBody TicketDTOs.CreateTicketRequest req,
            UriComponentsBuilder uriBuilder) {
        TicketDTOs.TicketResponse createdTicket = ticketService.createTicket(req);
        return ResponseEntity.created(
                uriBuilder.path("/api/tickets/{id}").buildAndExpand(createdTicket.id()).toUri())
                .body(createdTicket);
    }

    @GetMapping("/{id}")
    public ResponseEntity<TicketDTOs.TicketResponse> getById(@PathVariable Long id) {
        TicketDTOs.TicketResponse ticket = ticketService.getTicketById(id);
        return ResponseEntity.ok(ticket);
    }

    // we use this endpoints to cancel or modify ticket details, the required specs
    // not follow the REST conventions
    @PatchMapping("/{id}")
    public ResponseEntity<TicketDTOs.TicketResponse> update(
            @PathVariable Long id,
            @Validated @RequestBody TicketDTOs.UpdateTicketRequest req) {
        TicketDTOs.TicketResponse updatedTicket = ticketService.updateTicket(id, req);
        return ResponseEntity.ok(updatedTicket);
    }

    @PostMapping("/{id}/cancel")
    public ResponseEntity<TicketDTOs.TicketResponse> cancel(@PathVariable Long id) {
        TicketDTOs.TicketResponse canceledTicket = ticketService.cancelTicket(id);
        return ResponseEntity.ok(canceledTicket);
    }

    @PreAuthorize("hasAnyAuthority('DISPATCHER', 'ADMIN')")
    @PostMapping("/{id}/approve")
    public ResponseEntity<TicketDTOs.TicketResponse> approve(@PathVariable Long id) {
        TicketDTOs.TicketResponse approvedTicket = ticketService.approveTicket(id);
        return ResponseEntity.ok(approvedTicket);
    }

    @PreAuthorize("hasAnyAuthority('DISPATCHER', 'ADMIN')")
    @PostMapping("/{id}/cancel-pending")
    public ResponseEntity<TicketDTOs.TicketResponse> cancelPending(@PathVariable Long id) {
        TicketDTOs.TicketResponse canceledTicket = ticketService.cancelPendingTicket(id);
        return ResponseEntity.ok(canceledTicket);
    }

    @PreAuthorize("hasAnyAuthority('CLERK', 'DISPATCHER', 'ADMIN')")
    @GetMapping("/pending-approval")
    public ResponseEntity<List<TicketDTOs.TicketResponse>> getPendingApproval() {
        List<TicketDTOs.TicketResponse> tickets = ticketService.getPendingApprovalTickets();
        return ResponseEntity.ok(tickets);
    }

    @PreAuthorize("hasAnyAuthority('CLERK', 'DRIVER', 'ADMIN')")
    @PostMapping("/check-in")
    public ResponseEntity<TicketDTOs.TicketResponse> checkIn(
            @Validated @RequestBody TicketDTOs.CheckInRequest req) {
        TicketDTOs.TicketResponse checkedInTicket = ticketService.checkInTicket(req.qrCode());
        return ResponseEntity.ok(checkedInTicket);
    }

    @PostMapping("/payments/{id}")
    public ResponseEntity<TicketDTOs.TicketResponse> markAsPaid(
            @PathVariable Long id,
            @RequestParam(required = false) String paymentIntentId) {
        TicketDTOs.TicketResponse paidTicket = ticketService.markTicketAsPaid(id, paymentIntentId);
        return ResponseEntity.ok(paidTicket);
    }

    @PostMapping("/payments/confirm")
    public ResponseEntity<List<TicketDTOs.TicketResponse>> markMultipleAsPaid(
            @RequestBody List<Long> ticketIds,
            @RequestParam(required = false) String paymentIntentId) {
        List<TicketDTOs.TicketResponse> paidTickets = ticketService.markMultipleTicketsAsPaid(ticketIds,
                paymentIntentId);
        return ResponseEntity.ok(paidTickets);
    }

    @GetMapping("/my-tickets")
    public ResponseEntity<List<TicketDTOs.TicketResponse>> getMyTickets(
            @RequestParam(required = false) String status) {
        List<TicketDTOs.TicketResponse> tickets = ticketService.getTicketsForCurrentUser(status);
        return ResponseEntity.ok(tickets);
    }

    @PreAuthorize("hasAnyAuthority('CLERK', 'DISPATCHER', 'ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        ticketService.deleteTicket(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Get ALL tickets (ADMIN/DISPATCHER/CLERK only)
     */
    @PreAuthorize("hasAnyAuthority('CLERK', 'DISPATCHER', 'ADMIN')")
    @GetMapping
    public ResponseEntity<List<TicketDTOs.TicketResponse>> getAll() {
        List<TicketDTOs.TicketResponse> tickets = ticketService.getAllTickets();
        return ResponseEntity.ok(tickets);
    }

    /**
     * Search tickets by accountId and/or seatNumber
     */
    @GetMapping("/search")
    public ResponseEntity<List<TicketDTOs.TicketResponse>> search(
            @RequestParam(required = false) Long accountId,
            @RequestParam(required = false) String seatNumber) {
        List<TicketDTOs.TicketResponse> tickets = ticketService.searchTickets(accountId, seatNumber);
        return ResponseEntity.ok(tickets);
    }

    @GetMapping("/{id}/baggages")
    public ResponseEntity<List<BaggageDTOs.BaggageResponse>> getBaggagesByTicketId(@PathVariable Long id) {
        List<BaggageDTOs.BaggageResponse> baggages = ticketService.getBaggagesByTicketId(id);
        return ResponseEntity.ok(baggages);
    }

    @PreAuthorize("hasAnyAuthority('CLERK', 'DISPATCHER', 'ADMIN')")
    @GetMapping("/{id}/incidents")
    public ResponseEntity<List<IncidentDTOs.IncidentResponse>> getIncidentsByTicketId(@PathVariable Long id) {
        List<IncidentDTOs.IncidentResponse> incidents = ticketService.getIncidentsByTicketId(id);
        return ResponseEntity.ok(incidents);
    }
}
