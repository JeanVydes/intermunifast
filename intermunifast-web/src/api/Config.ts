import { createEndpoint } from './API';
import { ConfigResponse, CreateConfigRequest, UpdateConfigRequest } from './types/Config';

/**
 * Config API Endpoints
 */
export const ConfigAPI = {
    /**
     * Create a new config
     */
    create: createEndpoint<ConfigResponse, CreateConfigRequest>({
        url: '/api/configs',
        method: 'POST',
        requireAuth: true,
    }),

    /**
     * Get config by ID
     */
    getById: createEndpoint<ConfigResponse>({
        url: '/api/configs/{id}',
        method: 'GET',
        requireAuth: true,
    }),

    /**
     * Update config
     */
    update: createEndpoint<ConfigResponse, UpdateConfigRequest>({
        url: '/api/configs/{id}',
        method: 'PATCH',
        requireAuth: true,
    }),

    /**
     * Delete config
     */
    delete: createEndpoint<void>({
        url: '/api/configs/{id}',
        method: 'DELETE',
        requireAuth: true,
    }),
};

export default ConfigAPI;
