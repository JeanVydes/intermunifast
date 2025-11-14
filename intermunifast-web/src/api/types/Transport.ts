// Transport Types - Based on BusDTOs.java, RouteDTOs.java, StopDTOs.java, AmenityDTOs.java

// Amenity Type (embedded in Bus) - Based on AmenityDTOs.java
export interface AmenityResponse {
    id: number;
    name: string;
    description: string;
}

export interface CreateAmenityRequest {
    name: string;
    description?: string;
}

// Bus Types - Based on BusDTOs.java
export interface BusResponse {
    id: number;
    plate: string;
    capacity: number;
    amenities: AmenityResponse[];
    status: string; // Can be: 'ACTIVE', 'INACTIVE', 'MAINTENANCE', etc.
}

export interface CreateBusRequest {
    plate: string;
    capacity: number;
    amenities?: CreateAmenityRequest[];
}

export interface UpdateBusRequest {
    plate?: string;
    capacity?: number;
    amenities?: AmenityResponse[];
    status?: string;
}

// Route Types - Based on RouteDTOs.java
export interface RouteResponse {
    id: number;
    code: string;
    name: string;
    origin: string;
    destination: string;
    durationMinutes: number;
    distanceKm: number;
}

export interface CreateRouteRequest {
    code: string;
    name: string;
    origin: string;
    destination: string;
    durationMinutes: number;
    distanceKm: number;
}

export interface UpdateRouteRequest {
    code?: string;
    name?: string;
    origin?: string;
    destination?: string;
    durationMinutes?: number;
    distanceKm?: number;
}

// Stop Types - Based on StopDTOs.java
export interface StopResponse {
    id: number;
    name: string;
    sequence: number;
    latitude: number;
    longitude: number;
    routeId: number;
}

export interface CreateStopRequest {
    name: string;
    sequence: number;
    latitude: number;
    longitude: number;
    routeId: number;
}

export interface UpdateStopRequest {
    name?: string;
    sequence?: number;
    latitude?: number;
    longitude?: number;
    routeId?: number;
}

