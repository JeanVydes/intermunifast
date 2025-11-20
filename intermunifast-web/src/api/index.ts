/**
 * Centralized API exports
 * 
 * Usage:
 *   import { AccountAPI, BusAPI, type AccountResponse } from '@/api';
 */

// Core API
export {
    createEndpoint,
    setAuthToken,
    isAuthenticated,
    clearAuthToken,
    AXIOS
} from './API';
export type {
    ApiResponse
} from './API';

// API Endpoints
export { AccountAPI } from './Account';
export { BusAPI } from './Bus';
export { RouteAPI } from './Route';
export { TripAPI } from './Trip';
export { TicketAPI } from './Ticket';
export { StopAPI } from './Stop';
export { SeatAPI } from './Seat';
export { SeatHoldAPI } from './SeatHold';
export { BaggageAPI } from './Baggage';
export { AssignmentAPI, IncidentAPI, ParcelAPI, FareRuleAPI } from './Operations';
export { ConfigAPI } from './Config';
export { MetricsAPI } from './Metrics';
export type { DashboardMetrics, CancellationMetrics, RevenueMetrics, OccupationMetrics, PunctualityMetrics } from './Metrics';

// Type exports
export * from './types/Common';
export * from './types/Account';
export * from './types/Transport';
export * from './types/Booking';
export * from './types/Operations';
export * from './types/Config';
