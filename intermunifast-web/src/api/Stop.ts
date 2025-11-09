import { createEndpoint } from './API';
import { StopResponse, CreateStopRequest, UpdateStopRequest } from './types/Transport';

/**
 * Stop API Endpoints
 */
export const StopAPI = {
    /**
     * Create a new stop
     */
    create: createEndpoint<StopResponse, CreateStopRequest>({
        url: '/api/stops',
        method: 'POST',
        requireAuth: true,
    }),

    /**
     * Get stop by ID
     */
    getById: createEndpoint<StopResponse>({
        url: '/api/stops/{id}',
        method: 'GET',
        requireAuth: true,
    }),

    /**
     * Update stop
     */
    update: createEndpoint<StopResponse, UpdateStopRequest>({
        url: '/api/stops/{id}',
        method: 'PATCH',
        requireAuth: true,
    }),

    /**
     * Delete stop
     */
    delete: createEndpoint<void>({
        url: '/api/stops/{id}',
        method: 'DELETE',
        requireAuth: true,
    }),
};

export default StopAPI;
