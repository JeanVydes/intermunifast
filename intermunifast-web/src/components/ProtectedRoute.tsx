import { FunctionComponent, ComponentChildren } from 'preact';
import { useRole } from '../providers/RoleProvider';
import { AccountRole } from '../api/types/Account';
import DashboardLayout from './dashboard/DashboardLayout';

interface ProtectedRouteProps {
    children: ComponentChildren;
    allowedRoles?: AccountRole[];
    requireAuth?: boolean;
    redirectTo?: string;
    showAccessDenied?: boolean;
}

export const ProtectedRoute: FunctionComponent<ProtectedRouteProps> = ({
    children,
    allowedRoles,
    requireAuth = true,
    redirectTo = '/auth/signup',
    showAccessDenied = true,
}) => {
    const { isAuthenticated, hasRole } = useRole();

    // Check authentication
    if (requireAuth && !isAuthenticated) {
        // Save the current path as redirect parameter
        const currentPath = window.location.pathname + window.location.search;
        const encodedPath = encodeURIComponent(currentPath);
        location.assign(`${redirectTo}?redirect=${encodedPath}`);
        return null;
    }

    // Check role-based access
    if (allowedRoles && allowedRoles.length > 0 && !hasRole(allowedRoles)) {
        if (showAccessDenied) {
            return (
                <DashboardLayout>
                    <div className="p-8">
                        <h1 className="text-3xl font-bold text-gray-900">Access Denied</h1>
                        <p className="text-gray-600 mt-2">
                            You do not have permission to view this page.
                        </p>
                    </div>
                </DashboardLayout>
            );
        }
        location.assign('/dashboard');
        return null;
    }

    return <>{children}</>;
};

export default ProtectedRoute;
