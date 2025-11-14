import { createEndpoint } from './API';
import { BusResponse, CreateBusRequest, UpdateBusRequest } from './types/Transport';

/**
 * Bus API Endpoints
 */
export const BusAPI = {
    /**
     * Create a new bus
     */
    create: createEndpoint<BusResponse, CreateBusRequest>({
        url: '/api/buses',
        method: 'POST',
        requireAuth: true,
    }),

    /**
     * Get bus by ID
     */
    getById: createEndpoint<BusResponse>({
        url: '/api/buses/{id}',
        method: 'GET',
        requireAuth: true,
    }),

    getAll: createEndpoint<BusResponse[]>({
        url: '/api/buses/all',
        method: 'GET',
        requireAuth: true,
    }),

    /**
     * Update bus
     */
    update: createEndpoint<BusResponse, UpdateBusRequest>({
        url: '/api/buses/{id}',
        method: 'PATCH',
        requireAuth: true,
    }),

    /**
     * Delete bus
     */
    delete: createEndpoint<void>({
        url: '/api/buses/{id}',
        method: 'DELETE',
        requireAuth: true,
    }),
};

export default BusAPI;
