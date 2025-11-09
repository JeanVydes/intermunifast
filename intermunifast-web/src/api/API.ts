import Axios from "axios";
import { API_REQUESTS_TIMEOUT, BASE_URL } from "../Config";

export const AXIOS = Axios.create({
    baseURL: BASE_URL,
    timeout: API_REQUESTS_TIMEOUT,
});

// Global state
let globalAuthToken: string | null = null;

/**
 * Set global authentication token
 * This token will be used for all requests that require authentication
 */
export const setAuthToken = (token: string | null): void => {
    globalAuthToken = token;
    if (token) {
        AXIOS.defaults.headers.common['Authorization'] = `Bearer ${token}`;
    } else {
        delete AXIOS.defaults.headers.common['Authorization'];
    }
};

/**
 * Get current global authentication token
 */
export const getAuthToken = (): string | null => {
    return globalAuthToken;
};

/**
 * Set base URL for all API requests
 */
export const setBaseURL = (url: string): void => {
    AXIOS.defaults.baseURL = url;
};

// Types
export type HttpMethod = 'GET' | 'POST' | 'PUT' | 'DELETE' | 'PATCH';

export interface PathParams {
    [key: string]: string | number;
}

export interface QueryParams {
    [key: string]: string | number | boolean | undefined | null;
}

export interface EndpointConfig {
    url: string;
    method: HttpMethod;
    requireAuth?: boolean;
}

export interface RequestOptions {
    pathParams?: PathParams;
    queryParams?: QueryParams;
    token?: string;
    config?: any;
}

export interface ApiResponse<T> {
    data: T;
    status: number;
    statusText: string;
    headers: any;
}

export interface ApiError {
    message: string;
    status?: number;
    data?: any;
}

// Utility Functions
export const setAuthHeader = (config: any, token: string): any => {
    if (!config.headers) {
        config.headers = {};
    }
    config.headers['Authorization'] = `Bearer ${token}`;
    return config;
};

export const buildQueryParams = (queryParams?: QueryParams): string => {
    if (!queryParams) return '';

    const params = new URLSearchParams();
    for (const key in queryParams) {
        const value = queryParams[key];
        if (value !== undefined && value !== null) {
            params.append(key, String(value));
        }
    }
    return params.toString() ? `?${params.toString()}` : '';
};

export const buildUrlWithPathParams = (url: string, pathParams?: PathParams): string => {
    if (!pathParams) return url;

    let builtUrl = url;
    for (const key in pathParams) {
        builtUrl = builtUrl.replace(`{${key}}`, encodeURIComponent(String(pathParams[key])));
    }
    return builtUrl;
};

export const getFullUrl = (url: string, pathParams?: PathParams, queryParams?: QueryParams): string => {
    const urlWithPath = buildUrlWithPathParams(url, pathParams);
    const queryString = buildQueryParams(queryParams);
    return `${urlWithPath}${queryString}`;
};

// Generic endpoint executor
export const createEndpoint = <TResponse = any, TRequest = any>(
    endpointConfig: EndpointConfig
) => {
    return async (
        data?: TRequest,
        options?: RequestOptions
    ): Promise<ApiResponse<TResponse>> => {
        try {
            const { pathParams, queryParams, token, config = {} } = options || {};

            // Build full URL
            const url = getFullUrl(endpointConfig.url, pathParams, queryParams);

            // Setup config
            let requestConfig: any = { ...config };

            // Add auth header if required (use provided token or global token)
            const authToken = token || globalAuthToken;
            if (endpointConfig.requireAuth && authToken) {
                requestConfig = setAuthHeader(requestConfig, authToken);
            }

            // Make request based on method
            let response: any;

            switch (endpointConfig.method) {
                case 'GET':
                    response = await AXIOS.get<TResponse>(url, requestConfig);
                    break;
                case 'POST':
                    response = await AXIOS.post<TResponse>(url, data, requestConfig);
                    break;
                case 'PUT':
                    response = await AXIOS.put<TResponse>(url, data, requestConfig);
                    break;
                case 'PATCH':
                    response = await AXIOS.patch<TResponse>(url, data, requestConfig);
                    break;
                case 'DELETE':
                    response = await AXIOS.delete<TResponse>(url, requestConfig);
                    break;
                default:
                    throw new Error(`Unsupported HTTP method: ${endpointConfig.method}`);
            }

            return {
                data: response.data,
                status: response.status,
                statusText: response.statusText,
                headers: response.headers,
            };
        } catch (error: any) {
            const apiError: ApiError = {
                message: error.response?.data?.message || error.message || 'An error occurred',
                status: error.response?.status,
                data: error.response?.data,
            };
            throw apiError;
        }
    };
};