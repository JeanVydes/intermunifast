// Booking Types - Based on TripDTOs.java, TicketDTOs.java, SeatDTOs.java, SeatHoldDTOs.java

// Payment Method Enum
export type PaymentMethod = 'CASH' | 'TRANSFER' | 'QR' | 'CARD' | 'DIGITAL_WALLET';

// Seat Type Enum
export type SeatType = 'STANDARD' | 'PREFERENTIAL';

// Passenger Type Enum
export type PassengerType = 'ADULT' | 'CHILD' | 'SENIOR' | 'STUDENT';

// Ticket Status Enum
export type TicketStatus = 'CONFIRMED' | 'CANCELLED' | 'PENDING_APPROVAL' | 'NO_SHOW';

// Trip Types - Based on TripDTOs.java
export interface TripResponse {
    id: number;
    routeId: number;
    busId: number;
    departureAt?: string; // ISO 8601 datetime string
    arrivalAt?: string;   // ISO 8601 datetime string
}

export interface CreateTripRequest {
    routeId: number;
    busId: number;
    departureAt: string; // ISO 8601 datetime string (LocalDateTime)
    arrivalAt: string;   // ISO 8601 datetime string (LocalDateTime)
}

export interface UpdateTripRequest {
    routeId?: number;
    busId?: number;
    departureAt?: string; // ISO 8601 datetime string
    arrivalAt?: string;   // ISO 8601 datetime string
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
    fromStopId: number | null;
    toStopId: number | null;
    paymentMethod: PaymentMethod;
    paymentIntentId?: string;
    status: TicketStatus;  // Ticket payment status
    price: number;         // Ticket price
    qrCode?: string;       // QR code for validation
}

export interface CreateTicketRequest {
    seatNumber: string;
    tripId: number;
    fromStopId: number | null;
    toStopId: number | null;
    paymentMethod: PaymentMethod;
    paymentIntentId?: string; // stripe payment intent id
    passengerType: PassengerType;
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
    fromStopId: number | null;
    toStopId: number | null;
    expiresAt: string; // ISO 8601 datetime string
    createdAt: number; // Unix timestamp
}

export interface CreateSeatHoldRequest {
    seatNumber: string;
    tripId: number;
    fromStopId: number | null;
    toStopId: number | null;
    expiresAt: string; // ISO 8601 datetime string
}

export interface UpdateSeatHoldRequest {
    seatNumber?: string;
    tripId?: number;
    fromStopId?: number;
    toStopId?: number;
    expiresAt?: string;
}

// Baggage Types - Based on BaggageDTOs.java
export interface BaggageResponse {
    id: number;
    weightKg: number;
    fee: number; // BigDecimal
    tagCode: string;
    ticketId: number;
}

export interface CreateBaggageRequest {
    weightKg: number;
    tagCode: string;
    ticketId: number;
}

export interface UpdateBaggageRequest {
    weightKg?: number;
    tagCode?: string;
    ticketId?: number;
}

// Trip Search Response - includes trips, routes, and stops
export interface TripSearchResponse {
    trips: TripResponse[];
    routes: import('./Transport').RouteResponse[];
    stops: import('./Transport').StopResponse[];
}
