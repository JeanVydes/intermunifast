package com.example.api.controllers;

import java.util.List;

import org.springframework.http.ResponseEntity;
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

    // we use this endpoints to cancel or modify ticket details, the required specs not follow the REST conventions
    @PatchMapping("/{id}")
    public ResponseEntity<TicketDTOs.TicketResponse> update(
            @PathVariable Long id,
            @Validated @RequestBody TicketDTOs.UpdateTicketRequest req) {
        TicketDTOs.TicketResponse updatedTicket = ticketService.updateTicket(id, req);
        return ResponseEntity.ok(updatedTicket);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        ticketService.deleteTicket(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping
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

    @GetMapping("/{id}/incidents")
    public ResponseEntity<List<IncidentDTOs.IncidentResponse>> getIncidentsByTicketId(@PathVariable Long id) {
        List<IncidentDTOs.IncidentResponse> incidents = ticketService.getIncidentsByTicketId(id);
        return ResponseEntity.ok(incidents);
    }
}
