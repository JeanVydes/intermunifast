import { FunctionComponent } from 'preact';
import Sidebar from '../../components/dashboard/Sidebar';

export interface DashboardLayoutProps {
    children: preact.ComponentChildren;
}

export const DashboardLayout: FunctionComponent<DashboardLayoutProps> = ({ children }) => {
    return (
        <div className="flex min-h-screen bg-gray-50">
            <Sidebar />
            <main className="flex-1 overflow-auto">
                {children}
            </main>
        </div>
    );
};

export default DashboardLayout;
