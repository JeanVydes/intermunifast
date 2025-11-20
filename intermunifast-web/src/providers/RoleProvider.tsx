import { createContext } from 'preact';
import { useContext, useMemo } from 'preact/hooks';
import { ComponentChildren } from 'preact';
import useAccountStore from '../stores/AccountStore';
import { AccountRole } from '../api/types/Account';

interface RoleContextValue {
    role: AccountRole | null;
    isAuthenticated: boolean;
    hasRole: (roles: AccountRole | AccountRole[]) => boolean;
    isAdmin: boolean;
    isDispatcher: boolean;
    isDriver: boolean;
    isUser: boolean;
}

const RoleContext = createContext<RoleContextValue | null>(null);

interface RoleProviderProps {
    children: ComponentChildren;
}

export const RoleProvider = ({ children }: RoleProviderProps) => {
    const { account } = useAccountStore();

    const value = useMemo<RoleContextValue>(() => {
        const role = account?.role || null;
        const isAuthenticated = !!account;

        return {
            role,
            isAuthenticated,
            hasRole: (roles: AccountRole | AccountRole[]) => {
                if (!role) return false;
                const roleArray = Array.isArray(roles) ? roles : [roles];
                return roleArray.includes(role);
            },
            isAdmin: role === 'ADMIN',
            isDispatcher: role === 'DISPATCHER',
            isDriver: role === 'DRIVER',
            isUser: role === 'USER',
        };
    }, [account]);

    return <RoleContext.Provider value={value}>{children}</RoleContext.Provider>;
};

export const useRole = (): RoleContextValue => {
    const context = useContext(RoleContext);
    if (!context) {
        throw new Error('useRole must be used within a RoleProvider');
    }
    return context;
};
