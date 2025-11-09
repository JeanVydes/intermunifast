// Booking Types - Based on TripDTOs.java, TicketDTOs.java, SeatDTOs.java, SeatHoldDTOs.java

// Payment Method Enum
export type PaymentMethod = 'CASH' | 'CARD' | 'DIGITAL_WALLET' | 'BANK_TRANSFER';

// Seat Type Enum
export type SeatType = 'STANDARD' | 'VIP' | 'SEMI_BED' | 'BED';

// Trip Types - Based on TripDTOs.java
export interface TripResponse {
    id: number;
    routeId: number;
    busId: number;
}

export interface CreateTripRequest {
    routeId: number;
    busId: number;
}

export interface UpdateTripRequest {
    routeId?: number;
    busId?: number;
}

// Query params for Trip
export interface TripQueryParams {
    routeId?: string;
    status?: string;
}

// Ticket Types - Based on TicketDTOs.java
export interface TicketResponse {
    id: number;
    seatNumber: string;
    tripId: number;
    fromStopId: number;
    toStopId: number;
    paymentMethod: PaymentMethod;
    paymentIntentId?: string;
}

export interface CreateTicketRequest {
    seatNumber: string;
    tripId: number;
    fromStopId: number;
    toStopId: number;
    paymentMethod: PaymentMethod;
    paymentIntentId?: string; // stripe payment intent id
}

export interface UpdateTicketRequest {
    seatNumber?: string;
    tripId?: number;
    fromStopId?: number;
    toStopId?: number;
    paymentMethod?: PaymentMethod;
    paymentIntentId?: string;
}

// Query params for Ticket search
export interface TicketSearchParams {
    accountId?: number;
    seatNumber?: string;
}

// Seat Types - Based on SeatDTOs.java
export interface SeatResponse {
    id: number;
    number: string;
    type: SeatType;
    busId: number;
}

export interface CreateSeatRequest {
    number: string;
    type: SeatType;
    busId: number;
}

export interface UpdateSeatRequest {
    number?: string;
    type?: SeatType;
    busId?: number;
}

// Query params for Seat
export interface SeatQueryParams {
    status?: string;
}

// Seat Hold Types - Based on SeatHoldDTOs.java
export interface SeatHoldResponse {
    id: number;
    seatNumber: string;
    tripId: number;
}

export interface CreateSeatHoldRequest {
    seatNumber: string;
    tripId: number;
}

export interface UpdateSeatHoldRequest {
    seatNumber?: string;
    tripId?: number;
}

