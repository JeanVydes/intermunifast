// Read from environment variables (Vite exposes import.meta.env)
export const PRODUCTION = import.meta.env.VITE_PRODUCTION === 'true' || import.meta.env.MODE === 'production';
export const BASE_URL = import.meta.env.VITE_API_URL || 'http://localhost:8080';
export const API_REQUESTS_TIMEOUT = 5000;

