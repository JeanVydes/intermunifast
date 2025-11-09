// Operations Types - Based on AssignmentDTOs.java, BaggageDTOs.java, IncidentDTOs.java, ParcelDTOs.java, FareRuleDTOs.java

// Incident Type Enum
export type IncidentType = 'DELAY' | 'CANCELLATION' | 'ACCIDENT' | 'MECHANICAL' | 'CUSTOMER_COMPLAINT' | 'OTHER';

// Entity Type for Incidents
export type EntityType = 'TRIP' | 'TICKET' | 'BUS' | 'DRIVER' | 'OTHER';

// Assignment Types - Based on AssignmentDTOs.java
export interface AssignmentResponse {
    id: number;
    checklistOk: boolean;
    assignedAt: string; // LocalDateTime as ISO string
    driverId: number;
    dispatcherId: number;
    tripId: number;
}

export interface CreateAssignmentRequest {
    checklistOk: boolean;
    assignedAt: string; // LocalDateTime as ISO string
    driverId: number;
    dispatcherId: number;
    tripId: number;
}

export interface UpdateAssignmentRequest {
    id?: number;
    checklistOk?: boolean;
    assignedAt?: string; // LocalDateTime as ISO string
    driverId?: number;
    dispatcherId?: number;
    tripId?: number;
}

// Baggage Types - Based on BaggageDTOs.java
export interface BaggageResponse {
    id: number;
    weightKg: number;
    fee: number; // BigDecimal as number
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

// Incident Types - Based on IncidentDTOs.java
export interface IncidentResponse {
    id: number;
    type: IncidentType;
    note: string;
    relatedEntity: EntityType;
    relatedEntityId: number;
}

export interface CreateIncidentRequest {
    type: IncidentType;
    note: string;
    relatedEntity: EntityType;
    relatedEntityId: number;
}

export interface UpdateIncidentRequest {
    type?: IncidentType;
    note?: string;
    relatedEntity?: EntityType;
    relatedEntityId?: number;
}

// Parcel Types - Based on ParcelDTOs.java
export interface ParcelResponse {
    id: number;
    code: string;
    senderName: string;
    senderPhone: string;
    receiverName: string;
    receiverPhone: string;
    fromStopId: number;
    toStopId: number;
    status: string;
    proofPhotoUrl?: string;
    price: number;
}

export interface CreateParcelRequest {
    code: string;
    senderName: string;
    senderPhone: string;
    receiverName: string;
    receiverPhone: string;
    fromStopId: number;
    toStopId: number;
}

export interface UpdateParcelRequest {
    code?: string;
    senderName?: string;
    senderPhone?: string;
    receiverName?: string;
    receiverPhone?: string;
    fromStopId?: number;
    toStopId?: number;
    status?: string;
    proofPhotoUrl?: string;
    price?: number;
}

// FareRule Types - Based on FareRuleDTOs.java
export interface FareRuleResponse {
    id: number;
    fromStopId: number;
    toStopId: number;
    routeId: number;
    discounts: number[];
    dynamicPricing: boolean;
}

export interface CreateFareRuleRequest {
    fromStopId: number;
    toStopId: number;
    routeId: number;
}

export interface UpdateFareRuleRequest {
    fromStopId?: number;
    toStopId?: number;
    routeId?: number;
}

