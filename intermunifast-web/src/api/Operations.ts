import { createEndpoint } from './API';
import {
    AssignmentResponse,
    CreateAssignmentRequest,
    UpdateAssignmentRequest,
    BaggageResponse,
    CreateBaggageRequest,
    UpdateBaggageRequest,
    IncidentResponse,
    CreateIncidentRequest,
    UpdateIncidentRequest,
    ParcelResponse,
    CreateParcelRequest,
    UpdateParcelRequest,
    FareRuleResponse,
    CreateFareRuleRequest,
    UpdateFareRuleRequest,
} from './types/Operations';

/**
 * Assignment API Endpoints
 */
export const AssignmentAPI = {
    create: createEndpoint<AssignmentResponse, CreateAssignmentRequest>({
        url: '/api/assignments',
        method: 'POST',
        requireAuth: true,
    }),

    getById: createEndpoint<AssignmentResponse>({
        url: '/api/assignments/{id}',
        method: 'GET',
        requireAuth: true,
    }),

    update: createEndpoint<AssignmentResponse, UpdateAssignmentRequest>({
        url: '/api/assignments/{id}',
        method: 'PATCH',
        requireAuth: true,
    }),

    delete: createEndpoint<void>({
        url: '/api/assignments/{id}',
        method: 'DELETE',
        requireAuth: true,
    }),
};

/**
 * Baggage API Endpoints
 */
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

/**
 * Incident API Endpoints
 */
export const IncidentAPI = {
    create: createEndpoint<IncidentResponse, CreateIncidentRequest>({
        url: '/api/incidents',
        method: 'POST',
        requireAuth: true,
    }),

    getById: createEndpoint<IncidentResponse>({
        url: '/api/incidents/{id}',
        method: 'GET',
        requireAuth: true,
    }),

    update: createEndpoint<IncidentResponse, UpdateIncidentRequest>({
        url: '/api/incidents/{id}',
        method: 'PATCH',
        requireAuth: true,
    }),

    delete: createEndpoint<void>({
        url: '/api/incidents/{id}',
        method: 'DELETE',
        requireAuth: true,
    }),
};

/**
 * Parcel API Endpoints
 */
export const ParcelAPI = {
    create: createEndpoint<ParcelResponse, CreateParcelRequest>({
        url: '/api/parcels',
        method: 'POST',
        requireAuth: true,
    }),

    getById: createEndpoint<ParcelResponse>({
        url: '/api/parcels/{id}',
        method: 'GET',
        requireAuth: true,
    }),

    update: createEndpoint<ParcelResponse, UpdateParcelRequest>({
        url: '/api/parcels/{id}',
        method: 'PATCH',
        requireAuth: true,
    }),

    delete: createEndpoint<void>({
        url: '/api/parcels/{id}',
        method: 'DELETE',
        requireAuth: true,
    }),
};

/**
 * FareRule API Endpoints
 */
export const FareRuleAPI = {
    create: createEndpoint<FareRuleResponse, CreateFareRuleRequest>({
        url: '/api/fare-rules',
        method: 'POST',
        requireAuth: true,
    }),

    getById: createEndpoint<FareRuleResponse>({
        url: '/api/fare-rules/{id}',
        method: 'GET',
        requireAuth: true,
    }),

    update: createEndpoint<FareRuleResponse, UpdateFareRuleRequest>({
        url: '/api/fare-rules/{id}',
        method: 'PATCH',
        requireAuth: true,
    }),

    delete: createEndpoint<void>({
        url: '/api/fare-rules/{id}',
        method: 'DELETE',
        requireAuth: true,
    }),
};

export default AssignmentAPI;
