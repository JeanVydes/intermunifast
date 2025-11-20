import { useLocation } from 'preact-iso';
import useAuthStore from '../stores/AuthStore';

/**
 * Handle user logout
 * Clears auth token, cart, and redirects to home
 */
export const handleLogout = () => {
    const { logout } = useAuthStore.getState();

    logout();
    localStorage.removeItem('authToken');
    localStorage.removeItem('cartTicketIds');

    // Need to use window.location for navigation outside of component
    window.location.href = '/';
};

/**
 * Check if user is authenticated
 * @returns boolean indicating if user has valid token
 */
export const isAuthenticated = (): boolean => {
    const { token } = useAuthStore.getState();
    return !!token;
};

/**
 * Redirect to login page with optional return URL
 * @param returnUrl - URL to redirect after successful login
 */
export const redirectToLogin = (returnUrl?: string) => {
    if (returnUrl) {
        localStorage.setItem('redirectAfterLogin', returnUrl);
    }
    window.location.href = '/auth/signin';
};
