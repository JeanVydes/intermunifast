import { createEndpoint } from './API';
import type { SeatHoldResponse, CreateSeatHoldRequest, UpdateSeatHoldRequest } from './types/Booking';

export const SeatHoldAPI = {
    create: createEndpoint<SeatHoldResponse, CreateSeatHoldRequest>({
        url: '/api/seat-holds',
        method: 'POST',
        requireAuth: true,
    }),

    getById: createEndpoint<SeatHoldResponse>({
        url: '/api/seat-holds/{id}',
        method: 'GET',
        requireAuth: true,
    }),

    update: createEndpoint<SeatHoldResponse, UpdateSeatHoldRequest>({
        url: '/api/seat-holds/{id}',
        method: 'PATCH',
        requireAuth: true,
    }),

    delete: createEndpoint<void>({
        url: '/api/seat-holds/{id}',
        method: 'DELETE',
        requireAuth: true,
    }),
};

export default SeatHoldAPI;
