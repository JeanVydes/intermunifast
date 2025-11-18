import { createEndpoint } from './API';

/**
 * Metrics response types
 */
export interface CancellationMetrics {
    totalCancellations: number;
    cancellationRate: number;
    previousPeriodRate: number;
    changePercentage: number;
}

export interface RevenueMetrics {
    totalRevenue: number;
    previousPeriodRevenue: number;
    changePercentage: number;
    totalTicketsSold: number;
    averageTicketPrice: number;
}

export interface OccupationMetrics {
    averageOccupation: number;
    previousPeriodOccupation: number;
    changePercentage: number;
    totalTrips: number;
    totalSeatsOffered: number;
    totalSeatsSold: number;
}

export interface PunctualityMetrics {
    punctualityRate: number;
    totalTripsCompleted: number;
    totalTripsOnTime: number;
    totalTripsDelayed: number;
    previousPeriodRate: number;
    changePercentage: number;
}

export interface DashboardMetrics {
    cancellations: CancellationMetrics;
    revenue: RevenueMetrics;
    occupation: OccupationMetrics;
    punctuality: PunctualityMetrics;
    startDate: string;
    endDate: string;
}

/**
 * Metrics API Endpoints
 */
export const MetricsAPI = {
    /**
     * Get dashboard metrics with optional date range
     * Query params: startDate (YYYY-MM-DD), endDate (YYYY-MM-DD)
     */
    getDashboard: createEndpoint<DashboardMetrics, never>({
        url: '/api/metrics/dashboard',
        method: 'GET',
        requireAuth: true,
    }),

    /**
     * Get metrics for today
     */
    getToday: createEndpoint<DashboardMetrics, never>({
        url: '/api/metrics/dashboard/today',
        method: 'GET',
        requireAuth: true,
    }),

    /**
     * Get metrics for this week
     */
    getThisWeek: createEndpoint<DashboardMetrics, never>({
        url: '/api/metrics/dashboard/this-week',
        method: 'GET',
        requireAuth: true,
    }),

    /**
     * Get metrics for this month
     */
    getThisMonth: createEndpoint<DashboardMetrics, never>({
        url: '/api/metrics/dashboard/this-month',
        method: 'GET',
        requireAuth: true,
    }),

    /**
     * Get metrics for this year
     */
    getThisYear: createEndpoint<DashboardMetrics, never>({
        url: '/api/metrics/dashboard/this-year',
        method: 'GET',
        requireAuth: true,
    }),
};

export default MetricsAPI;
