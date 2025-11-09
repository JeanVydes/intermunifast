import { createEndpoint } from './API';
import {
    TripResponse,
    CreateTripRequest,
    UpdateTripRequest,
    TicketResponse,
    SeatResponse,
} from './types/Booking';
import { AssignmentResponse, IncidentResponse } from './types/Operations';

/**
 * Trip API Endpoints
 */
export const TripAPI = {
    /**
     * Create a new trip
     */
    create: createEndpoint<TripResponse, CreateTripRequest>({
        url: '/api/trips',
        method: 'POST',
        requireAuth: true,
    }),

    /**
     * Get trip by ID
     * Supports optional query params: routeId, status
     */
    getById: createEndpoint<TripResponse>({
        url: '/api/trips/{id}',
        method: 'GET',
        requireAuth: true,
    }),

    /**
     * Update trip
     */
    update: createEndpoint<TripResponse, UpdateTripRequest>({
        url: '/api/trips/{id}',
        method: 'PATCH',
        requireAuth: true,
    }),

    /**
     * Delete trip
     */
    delete: createEndpoint<void>({
        url: '/api/trips/{id}',
        method: 'DELETE',
        requireAuth: true,
    }),

    /**
     * Get all tickets for a trip
     * Supports optional query param: status
     */
    getTickets: createEndpoint<TicketResponse[]>({
        url: '/api/trips/{id}/tickets',
        method: 'GET',
        requireAuth: true,
    }),

    /**
     * Get all seats for a trip
     * Supports optional query param: status
     */
    getSeats: createEndpoint<SeatResponse[]>({
        url: '/api/trips/{id}/seats',
        method: 'GET',
        requireAuth: true,
    }),

    /**
     * Get all assignments for a trip
     */
    getAssignments: createEndpoint<AssignmentResponse[]>({
        url: '/api/trips/{id}/assignments',
        method: 'GET',
        requireAuth: true,
    }),

    /**
     * Get all incidents for a trip
     */
    getIncidents: createEndpoint<IncidentResponse[]>({
        url: '/api/trips/{id}/incidents',
        method: 'GET',
        requireAuth: true,
    }),
};

export default TripAPI;
