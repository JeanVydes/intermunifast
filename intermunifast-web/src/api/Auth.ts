import { createEndpoint } from './API';
import { AccountResponse, CreateAccountRequest, UpdateAccountRequest } from './types/Account';
import { AuthRequest, AuthResponse, AuthSession } from './types/Auth';

export const AuthAPI = {
    /**
     * Sign in - Create a new session
     */
    signIn: createEndpoint<AuthResponse, AuthRequest>({
        url: '/api/auth/signin',
        method: 'POST',
        requireAuth: false,
    }),

    /**
     * Legacy alias for signIn
     */
    createSession: createEndpoint<AuthResponse, AuthRequest>({
        url: '/api/auth/signin',
        method: 'POST',
        requireAuth: false,
    }),

    /**
     * Get current user profile
     */
    getMe: createEndpoint<AuthSession>({
        url: '/api/auth/me',
        method: 'GET',
        requireAuth: true,
    }),
};

export default AuthAPI;
