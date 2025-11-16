package com.example.services.definitions;

import java.util.List;

import com.example.api.dto.BaggageDTOs;
import com.example.api.dto.IncidentDTOs;
import com.example.api.dto.TicketDTOs;

public interface TicketService {
    TicketDTOs.TicketResponse createTicket(TicketDTOs.CreateTicketRequest req);

    TicketDTOs.TicketResponse getTicketById(Long id);

    TicketDTOs.TicketResponse updateTicket(Long id, TicketDTOs.UpdateTicketRequest req);

    TicketDTOs.TicketResponse cancelTicket(Long id);

    TicketDTOs.TicketResponse markTicketAsPaid(Long id, String paymentIntentId);

    List<TicketDTOs.TicketResponse> markMultipleTicketsAsPaid(List<Long> ticketIds, String paymentIntentId);

    List<TicketDTOs.TicketResponse> getTicketsForCurrentUser(String status);

    void deleteTicket(Long id);

    List<TicketDTOs.TicketResponse> searchTickets(Long accountId, String seatNumber);

    List<BaggageDTOs.BaggageResponse> getBaggagesByTicketId(Long id);

    List<IncidentDTOs.IncidentResponse> getIncidentsByTicketId(Long id);
}
