// Account Types - Based on AccountDTOs.java

export type AccountRole = 'USER' | 'ADMIN' | 'DRIVER' | 'DISPATCHER';
export type AccountStatus = 'ACTIVE' | 'INACTIVE' | 'SUSPENDED' | 'DELETED';

export interface AccountResponse {
    id: number;
    name: string;
    email: string;
    phone: string;
    role: AccountRole;
    status: AccountStatus;
}

export interface CreateAccountRequest {
    name: string;
    email: string;
    phone: string;
    password: string;
    isAdmin?: boolean;
}

export interface UpdateAccountRequest {
    name?: string;
    email?: string;
    phone?: string;
    password?: string;
    // Restricted to admins
    role?: AccountRole;
    status?: AccountStatus;
}

