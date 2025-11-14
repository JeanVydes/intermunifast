import { AccountRole, AccountStatus } from "./Account";

export interface AuthRequest {
    email: string;
    password: string;
}

export interface AuthResponse {
    token: string;
    userId: number;
    role: AccountRole;
}

export interface AuthSession {
    phone: string;
    id: number;
    status: AccountStatus;
    email: string;
    name: string;
    role: AccountRole;
}