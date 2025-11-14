import { FunctionComponent } from 'preact';
import { useLocation } from 'preact-iso';
import {
    LayoutDashboard,
    Bus,
    MapPin,
    Route,
    Ticket,
    Users,
    Settings,
    LogOut,
    ChevronRight
} from 'lucide-preact';
import { clearAuthToken } from '../../api';

export interface SidebarProps {
    collapsed?: boolean;
}

interface MenuItem {
    label: string;
    icon: any;
    path: string;
    badge?: string;
}

const menuItems: MenuItem[] = [
    { label: 'Dashboard', icon: LayoutDashboard, path: '/dashboard' },
    { label: 'Buses', icon: Bus, path: '/dashboard/buses' },
    { label: 'Routes', icon: Route, path: '/dashboard/routes' },
    { label: 'Trips', icon: MapPin, path: '/dashboard/trips' },
    { label: 'Tickets', icon: Ticket, path: '/dashboard/tickets' },
    { label: 'Users', icon: Users, path: '/dashboard/users' },
];

export const Sidebar: FunctionComponent<SidebarProps> = ({ collapsed = false }) => {
    const location = useLocation();

    const handleLogout = () => {
        clearAuthToken();
        window.location.href = '/';
    };

    const isActive = (path: string) => {
        return location.path === path;
    };

    return (
        <aside className={`bg-white border-r border-gray-200 h-screen sticky top-0 transition-all duration-300 ${collapsed ? 'w-20' : 'w-64'}`}>
            <div className="flex flex-col h-full">
                {/* Logo */}
                <div className="p-6 border-b border-gray-200">
                    <div className="flex items-center gap-3">
                        <div className="w-10 h-10 bg-gradient-to-br from-purple-600 to-purple-400 rounded-xl flex items-center justify-center">
                            <Bus className="w-6 h-6 text-white" />
                        </div>
                        {!collapsed && (
                            <div>
                                <h1 className="text-lg font-bold text-gray-900">InterMuniFast</h1>
                                <p className="text-xs text-gray-500">Admin Panel</p>
                            </div>
                        )}
                    </div>
                </div>

                {/* Navigation */}
                <nav className="flex-1 p-4 space-y-1">
                    {menuItems.map((item) => {
                        const Icon = item.icon;
                        const active = isActive(item.path);

                        return (
                            <a
                                key={item.path}
                                href={item.path}
                                className={`
                                    flex items-center gap-3 px-4 py-3 rounded-lg transition-all
                                    ${active
                                        ? 'bg-purple-50 text-purple-700 font-medium'
                                        : 'text-gray-700 hover:bg-gray-50'
                                    }
                                    ${collapsed ? 'justify-center' : ''}
                                `}
                            >
                                <Icon className="w-5 h-5 flex-shrink-0" />
                                {!collapsed && (
                                    <>
                                        <span className="flex-1">{item.label}</span>
                                        {item.badge && (
                                            <span className="px-2 py-0.5 text-xs font-medium bg-purple-100 text-purple-700 rounded-full">
                                                {item.badge}
                                            </span>
                                        )}
                                        {active && <ChevronRight className="w-4 h-4" />}
                                    </>
                                )}
                            </a>
                        );
                    })}
                </nav>

                {/* Footer */}
                <div className="p-4 border-t border-gray-200 space-y-1">
                    <button
                        onClick={handleLogout}
                        className={`
                            w-full flex items-center gap-3 px-4 py-3 rounded-lg
                            text-red-600 hover:bg-red-50 transition-all
                            ${collapsed ? 'justify-center' : ''}
                        `}
                    >
                        <LogOut className="w-5 h-5 flex-shrink-0" />
                        {!collapsed && <span>Logout</span>}
                    </button>
                </div>
            </div>
        </aside>
    );
};

export default Sidebar;
