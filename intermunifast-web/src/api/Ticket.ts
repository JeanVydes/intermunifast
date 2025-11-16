import { createEndpoint } from './API';
import {
    TicketResponse,
    CreateTicketRequest,
    UpdateTicketRequest,
    TicketSearchParams,
    BaggageResponse,
} from './types/Booking';
import { IncidentResponse } from './types/Operations';

/**
 * Ticket API Endpoints
 */
export const TicketAPI = {
    /**
     * Create a new ticket
     */
    create: createEndpoint<TicketResponse, CreateTicketRequest>({
        url: '/api/tickets',
        method: 'POST',
        requireAuth: true,
    }),

    /**
     * Get ticket by ID
     */
    getById: createEndpoint<TicketResponse>({
        url: '/api/tickets/{id}',
        method: 'GET',
        requireAuth: true,
    }),

    getByAccountId: createEndpoint<TicketResponse[], number>({
        url: '/api/tickets?accountId={accountId}',
        method: 'GET',
        requireAuth: true,
    }),

    /**
     * Update ticket
     */
    update: createEndpoint<TicketResponse, UpdateTicketRequest>({
        url: '/api/tickets/{id}',
        method: 'PATCH',
        requireAuth: true,
    }),

    /**
     * Delete ticket
     */
    delete: createEndpoint<void>({
        url: '/api/tickets/{id}',
        method: 'DELETE',
        requireAuth: true,
    }),

    /**
     * Search tickets by accountId and/or seatNumber
     * Query params: accountId, seatNumber
     */
    search: createEndpoint<TicketResponse[], never>({
        url: '/api/tickets',
        method: 'GET',
        requireAuth: true,
    }),

    /**
     * Get all baggages for a ticket
     */
    getBaggages: createEndpoint<BaggageResponse[]>({
        url: '/api/tickets/{id}/baggages',
        method: 'GET',
        requireAuth: true,
    }),

    /**
     * Get all incidents for a ticket
     */
    getIncidents: createEndpoint<IncidentResponse[]>({
        url: '/api/tickets/{id}/incidents',
        method: 'GET',
        requireAuth: true,
    }),

    /**
     * Mark a ticket as paid
     */
    markAsPaid: createEndpoint<TicketResponse, { paymentIntentId?: string }>({
        url: '/api/tickets/{id}/mark-paid',
        method: 'POST',
        requireAuth: true,
    }),

    /**
     * Mark multiple tickets as paid (batch payment)
     */
    markMultipleAsPaid: createEndpoint<TicketResponse[], { ticketIds: number[]; paymentIntentId?: string }>({
        url: '/api/tickets/mark-paid-batch',
        method: 'POST',
        requireAuth: true,
    }),

    /**
     * Get all tickets for the current user
     * Query param: status (optional) - filter by UNPAID, SOLD, CANCELLED, NO_SHOW
     */
    getMyTickets: createEndpoint<TicketResponse[], never>({
        url: '/api/tickets/my-tickets',
        method: 'GET',
        requireAuth: true,
    }),
};

export default TicketAPI;
