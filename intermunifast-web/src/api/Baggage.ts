import { createEndpoint } from './API';
import type { BaggageResponse, CreateBaggageRequest, UpdateBaggageRequest } from './types/Booking';

export const BaggageAPI = {
    create: createEndpoint<BaggageResponse, CreateBaggageRequest>({
        url: '/api/baggages',
        method: 'POST',
        requireAuth: true,
    }),

    getById: createEndpoint<BaggageResponse>({
        url: '/api/baggages/{id}',
        method: 'GET',
        requireAuth: true,
    }),

    update: createEndpoint<BaggageResponse, UpdateBaggageRequest>({
        url: '/api/baggages/{id}',
        method: 'PATCH',
        requireAuth: true,
    }),

    delete: createEndpoint<void>({
        url: '/api/baggages/{id}',
        method: 'DELETE',
        requireAuth: true,
    }),
};

export default BaggageAPI;
