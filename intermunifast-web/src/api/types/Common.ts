// Common types used across the API

export interface Timestamps {
    createdAt: number;
    updatedAt: number;
}

export interface PaginationParams {
    page?: number;
    size?: number;
    sort?: string;
}

export interface PaginatedResponse<T> {
    content: T[];
    totalElements: number;
    totalPages: number;
    size: number;
    number: number;
}
