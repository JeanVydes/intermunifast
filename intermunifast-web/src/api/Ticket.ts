import { createEndpoint } from './API';
import {
    TicketResponse,
    CreateTicketRequest,
    CheckInRequest,
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
     * Get ALL tickets (ADMIN/DISPATCHER/CLERK only)
     */
    getAll: createEndpoint<TicketResponse[], never>({
        url: '/api/tickets',
        method: 'GET',
        requireAuth: true,
    }),

    /**
     * Search tickets by accountId and/or seatNumber
     * Pass query params via options: { queryParams: { accountId: 123, seatNumber: 'A1' } }
     */
    search: createEndpoint<TicketResponse[], never>({
        url: '/api/tickets/search',
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
     * Body: number[] (array of ticket IDs)
     * Query param: paymentIntentId (optional)
     */
    markMultipleAsPaid: createEndpoint<TicketResponse[], number[]>({
        url: '/api/tickets/payments/confirm',
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

    /**
     * Approve a pending ticket (DISPATCHER/ADMIN only)
     */
    approve: createEndpoint<TicketResponse>({
        url: '/api/tickets/{id}/approve',
        method: 'POST',
        requireAuth: true,
    }),

    /**
     * Cancel a pending approval ticket (DISPATCHER/ADMIN only)
     */
    cancelPending: createEndpoint<TicketResponse>({
        url: '/api/tickets/{id}/cancel-pending',
        method: 'POST',
        requireAuth: true,
    }),

    /**
     * Get all tickets with PENDING_APPROVAL status (DISPATCHER/ADMIN only)
     */
    getPendingApproval: createEndpoint<TicketResponse[], never>({
        url: '/api/tickets/pending-approval',
        method: 'GET',
        requireAuth: true,
    }),

    /**
     * Check in a ticket using QR code (CLERK/DRIVER/ADMIN only)
     */
    checkIn: createEndpoint<TicketResponse, CheckInRequest>({
        url: '/api/tickets/check-in',
        method: 'POST',
        requireAuth: true,
    }),
};

export default TicketAPI;
