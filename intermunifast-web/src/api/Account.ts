import { createEndpoint } from './API';
import { AccountResponse, CreateAccountRequest, UpdateAccountRequest } from './types/Account';

// Account Endpoints
export const AccountAPI = {
    /**
     * Create a new account
     */
    create: createEndpoint<AccountResponse, CreateAccountRequest>({
        url: '/api/accounts',
        method: 'POST',
        requireAuth: false,
    }),

    /**
     * Get account by ID
     */
    getById: createEndpoint<AccountResponse>({
        url: '/api/accounts/{id}',
        method: 'GET',
        requireAuth: true,
    }),

    /**
     * Get account by email
     */
    getByEmail: createEndpoint<AccountResponse>({
        url: '/api/accounts/email/{email}',
        method: 'GET',
        requireAuth: true,
    }),

    /**
     * Update account
     */
    update: createEndpoint<AccountResponse, UpdateAccountRequest>({
        url: '/api/accounts/{id}',
        method: 'PATCH',
        requireAuth: true,
    }),

    /**
     * Delete account
     */
    delete: createEndpoint<void>({
        url: '/api/accounts/{id}',
        method: 'DELETE',
        requireAuth: true,
    }),
};

export default AccountAPI;
