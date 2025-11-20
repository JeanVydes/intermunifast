import { createEndpoint } from './API';
import {
    RouteResponse,
    CreateRouteRequest,
    UpdateRouteRequest,
    StopResponse,
} from './types/Transport';

/**
 * Route API Endpoints
 */
export const RouteAPI = {
    /**
     * Create a new route
     */
    create: createEndpoint<RouteResponse, CreateRouteRequest>({
        url: '/api/routes',
        method: 'POST',
        requireAuth: true,
    }),

    /**
     * Get route by ID
     */
    getById: createEndpoint<RouteResponse>({
        url: '/api/routes/{id}',
        method: 'GET',
        requireAuth: true,
    }),

    /**
     * Get all routes
     */
    getAll: createEndpoint<RouteResponse[]>({
        url: '/api/routes/all',
        method: 'GET',
        requireAuth: true,
    }),

    /**
     * Update route
     */
    update: createEndpoint<RouteResponse, UpdateRouteRequest>({
        url: '/api/routes/{id}',
        method: 'PATCH',
        requireAuth: true,
    }),

    /**
     * Delete route
     */
    delete: createEndpoint<void>({
        url: '/api/routes/{id}',
        method: 'DELETE',
        requireAuth: true,
    }),

    /**
     * Get all stops for a route
     */
    getStops: createEndpoint<StopResponse[]>({
        url: '/api/routes/{id}/stops',
        method: 'GET',
        requireAuth: true,
    }),
};

export default RouteAPI;
