import Axios from "axios";
import { API_REQUESTS_TIMEOUT, BASE_URL } from "../Config";
import useAuthStore from "../stores/AuthStore";

// Axios instance with default config
export const AXIOS = Axios.create({
    baseURL: BASE_URL,
    timeout: API_REQUESTS_TIMEOUT,
});

// Get auth token
const getAuthToken = (): string | null => useAuthStore.getState().token;

// Request interceptor
AXIOS.interceptors.request.use(
    (config) => {
        const token = getAuthToken();
        if (token) {
            config.headers['Authorization'] = `Bearer ${token}`;
        }
        return config;
    },
    (error) => Promise.reject(error)
);

// Response interceptor
AXIOS.interceptors.response.use(
    (response) => response,
    (error) => {
        if (error.response?.status === 401) {
            useAuthStore.getState().setToken(null);
            localStorage.removeItem('authToken');
        }
        return Promise.reject(error);
    }
);

// Auth helpers
export const setAuthToken = (token: string | null): void => {
    useAuthStore.getState().setToken(token);
    if (token) {
        localStorage.setItem('authToken', token);
    } else {
        localStorage.removeItem('authToken');
    }
};

export const isAuthenticated = (): boolean => !!getAuthToken();
export const clearAuthToken = (): void => setAuthToken(null);

// Types
export interface ApiResponse<T> {
    data: T;
    status: number;
}

interface EndpointConfig {
    url: string;
    method: 'GET' | 'POST' | 'PUT' | 'DELETE' | 'PATCH';
    requireAuth?: boolean;
}

interface RequestOptions {
    pathParams?: Record<string, string | number>;
    queryParams?: Record<string, any>;
    token?: string;
}

// Build URL helpers
const buildUrl = (url: string, pathParams?: Record<string, string | number>, queryParams?: Record<string, any>): string => {
    let builtUrl = url;

    // Replace path params
    if (pathParams) {
        Object.entries(pathParams).forEach(([key, value]) => {
            builtUrl = builtUrl.replace(`{${key}}`, String(value));
        });
    }

    // Add query params
    if (queryParams) {
        const params = new URLSearchParams();
        Object.entries(queryParams).forEach(([key, value]) => {
            if (value !== undefined && value !== null) {
                params.append(key, String(value));
            }
        });
        const queryString = params.toString();
        if (queryString) builtUrl += `?${queryString}`;
    }

    return builtUrl;
};

// Generic endpoint creator
export const createEndpoint = <TResponse = any, TRequest = any>(
    endpointConfig: EndpointConfig
) => {
    return async (
        data?: TRequest,
        options?: RequestOptions
    ): Promise<ApiResponse<TResponse>> => {
        try {
            const { pathParams, queryParams, token } = options || {};
            const url = buildUrl(endpointConfig.url, pathParams, queryParams);

            const config: any = { headers: {} };

            // Add auth header
            const authToken = token || getAuthToken();
            if (endpointConfig.requireAuth && !authToken) {
                throw new Error('Authentication required');
            }
            if (authToken) {
                config.headers['Authorization'] = `Bearer ${authToken}`;
            }

            // Make request
            let response: any;
            switch (endpointConfig.method) {
                case 'GET':
                    response = await AXIOS.get<TResponse>(url, config);
                    break;
                case 'POST':
                    response = await AXIOS.post<TResponse>(url, data, config);
                    break;
                case 'PUT':
                    response = await AXIOS.put<TResponse>(url, data, config);
                    break;
                case 'PATCH':
                    response = await AXIOS.patch<TResponse>(url, data, config);
                    break;
                case 'DELETE':
                    response = await AXIOS.delete<TResponse>(url, config);
                    break;
            }

            return {
                data: response.data,
                status: response.status,
            };
        } catch (error: any) {
            throw {
                message: error.response?.data?.message || error.message || 'Request failed',
                status: error.response?.status,
                data: error.response?.data,
            };
        }
    };
};