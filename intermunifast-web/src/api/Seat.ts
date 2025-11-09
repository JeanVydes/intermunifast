import { createEndpoint } from './API';
import {
    SeatResponse,
    CreateSeatRequest,
    UpdateSeatRequest,
    SeatHoldResponse,
    CreateSeatHoldRequest,
    UpdateSeatHoldRequest,
} from './types/Booking';

/**
 * Seat API Endpoints
 */
export const SeatAPI = {
    /**
     * Create a new seat
     */
    create: createEndpoint<SeatResponse, CreateSeatRequest>({
        url: '/api/seats',
        method: 'POST',
        requireAuth: true,
    }),

    /**
     * Get seat by ID
     */
    getById: createEndpoint<SeatResponse>({
        url: '/api/seats/{id}',
        method: 'GET',
        requireAuth: true,
    }),

    /**
     * Update seat
     */
    update: createEndpoint<SeatResponse, UpdateSeatRequest>({
        url: '/api/seats/{id}',
        method: 'PATCH',
        requireAuth: true,
    }),

    /**
     * Delete seat
     */
    delete: createEndpoint<void>({
        url: '/api/seats/{id}',
        method: 'DELETE',
        requireAuth: true,
    }),
};

/**
 * Seat Hold API Endpoints
 */
export const SeatHoldAPI = {
    /**
     * Create a new seat hold
     */
    create: createEndpoint<SeatHoldResponse, CreateSeatHoldRequest>({
        url: '/api/seat-holds',
        method: 'POST',
        requireAuth: true,
    }),

    /**
     * Get seat hold by ID
     */
    getById: createEndpoint<SeatHoldResponse>({
        url: '/api/seat-holds/{id}',
        method: 'GET',
        requireAuth: true,
    }),

    /**
     * Update seat hold
     */
    update: createEndpoint<SeatHoldResponse, UpdateSeatHoldRequest>({
        url: '/api/seat-holds/{id}',
        method: 'PATCH',
        requireAuth: true,
    }),

    /**
     * Delete seat hold
     */
    delete: createEndpoint<void>({
        url: '/api/seat-holds/{id}',
        method: 'DELETE',
        requireAuth: true,
    }),
};

export default SeatAPI;
