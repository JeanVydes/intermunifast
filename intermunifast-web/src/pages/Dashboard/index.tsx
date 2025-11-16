import { FunctionComponent } from 'preact';
import { Calendar, Filter, Plus } from 'lucide-preact';
import DashboardLayout from '../../components/dashboard/DashboardLayout';
import StatCard from '../../components/dashboard/StatCard';
import CampaignCard from '../../components/dashboard/CampaignCard';
import { DollarSign, ShoppingCart, TrendingUp, Users } from 'lucide-preact';
import useAccountStore from '../../stores/AccountStore';

export const DashboardHome: FunctionComponent = () => {
    const { accountId, account } = useAccountStore();
    
    if (!accountId || !account) {
        location.assign('/auth/signup');
        return (
            <DashboardLayout>
                <div className="p-8">
                    <h1 className="text-3xl font-bold text-gray-900">Access Denied</h1>
                    <p className="text-gray-600 mt-2">You do not have permission to view this page.</p>
                </div>
            </DashboardLayout>
        );
    }

    if (account.role !== 'ADMIN') {
        return (
            <DashboardLayout>
                <div className="p-8">
                    <h1 className="text-3xl font-bold text-gray-900">Access Denied</h1>
                    <p className="text-gray-600 mt-2">You do not have permission to view this page.</p>
                </div>
            </DashboardLayout>
        );
    }

    return (
        <DashboardLayout>
            <div className="p-8">
                {/* Header */}
                <div className="flex items-center justify-between mb-8">
                    <div>
                        <h1 className="text-3xl font-bold text-gray-900">Total revenue this month</h1>
                        <p className="text-5xl font-bold bg-gradient-to-r from-purple-600 to-orange-400 bg-clip-text text-transparent mt-2">
                            $90,239.00
                        </p>
                    </div>
                    <div className="flex gap-3">
                        <button className="flex items-center gap-2 px-4 py-2 bg-white border border-gray-300 rounded-lg hover:bg-gray-50 transition-colors">
                            <Calendar className="w-4 h-4" />
                            Select Dates
                        </button>
                        <button className="flex items-center gap-2 px-4 py-2 bg-white border border-gray-300 rounded-lg hover:bg-gray-50 transition-colors">
                            <Filter className="w-4 h-4" />
                            Filter
                        </button>
                    </div>
                </div>

                {/* Stats Grid */}
                <div className="grid grid-cols-1 md:grid-cols-3 gap-6 mb-8">
                    <StatCard
                        title="Tickets"
                        value="752"
                        change={15}
                        changeLabel="compared to last week"
                        trend="up"
                        sparklineData={[10, 15, 12, 18, 20, 22]}
                    />
                    <StatCard
                        title="Kilometers Traveled"
                        value="6950"
                        change={-4}
                        changeLabel="compared to last week"
                        trend="down"
                        sparklineData={[350, 340, 345, 330, 325, 320]}
                    />
                    <StatCard
                        title="Average Ticket Price"
                        value="$17"
                        change={8}
                        changeLabel="compared to last week"
                        trend="up"
                        sparklineData={[17, 16, 15, 17, 17, 17]}
                    />
                </div>

                {/* Recent Campaigns */}
                <div className="mb-6">
                    <div className="flex items-center justify-between mb-6">
                        <h2 className="text-2xl font-bold text-gray-900">Recent campaigns</h2>
                        <a href="#" className="text-purple-600 hover:text-purple-700 font-medium">
                            View all
                        </a>
                    </div>

                    {/* Status Tabs */}
                    <div className="flex gap-6 mb-6 border-b border-gray-200">
                        <button className="pb-3 text-gray-900 border-b-2 border-purple-600 font-medium">
                            Draft <span className="ml-2 px-2 py-0.5 bg-gray-100 rounded-full text-sm">2</span>
                        </button>
                        <button className="pb-3 text-gray-600 hover:text-gray-900">
                            In Progress <span className="ml-2 px-2 py-0.5 bg-gray-100 rounded-full text-sm">2</span>
                        </button>
                        <button className="pb-3 text-gray-600 hover:text-gray-900">
                            Archived <span className="ml-2 px-2 py-0.5 bg-gray-100 rounded-full text-sm">1</span>
                        </button>
                    </div>

                    {/* Campaigns Grid */}
                    <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
                        <CampaignCard
                            title="10 Simple steps to revolutionise workflows with our product"
                            platform="facebook"
                            status="draft"
                            startDate="Not started"
                            lastUpdated="Apr 10, 2023"
                            avatars={['JD', 'SM', 'KL']}
                        />
                        <CampaignCard
                            title="Boost your performance: start using our amazing product"
                            platform="google"
                            status="in-progress"
                            startDate="Jun 1, 2023"
                            endDate="Aug 1, 2023"
                            lastUpdated="July 10, 2023"
                            avatars={['AB']}
                        />
                        <CampaignCard
                            title="The power of our product: A new era in SaaS"
                            platform="google"
                            status="archived"
                            endDate="Jun 11, 2023"
                            lastUpdated="Apr 10, 2023"
                            avatars={['CD', 'EF', 'GH', 'IJ']}
                        />
                        <CampaignCard
                            title="Beyond Boundaries: Explore our new product"
                            platform="instagram"
                            status="draft"
                            startDate="Not Started"
                            lastUpdated="Apr 10, 2023"
                            avatars={['MN', 'OP']}
                        />
                        <CampaignCard
                            title="Skyrocket your productivity: our product is revealed"
                            platform="facebook"
                            status="in-progress"
                            startDate="Jul 1, 2023"
                            endDate="Sep 30, 2023"
                            lastUpdated="July 10, 2023"
                            avatars={['QR', 'ST']}
                        />

                        {/* Add Campaign Card */}
                        <div className="bg-gray-50 rounded-xl border-2 border-dashed border-gray-300 hover:border-purple-400 hover:bg-purple-50 transition-all cursor-pointer flex items-center justify-center min-h-[280px]">
                            <div className="text-center">
                                <div className="inline-flex items-center justify-center w-12 h-12 bg-white rounded-full shadow-sm mb-3">
                                    <Plus className="w-6 h-6 text-purple-600" />
                                </div>
                                <p className="text-gray-600 font-medium">Add campaign</p>
                            </div>
                        </div>
                    </div>
                </div>

                {/* Get Extension Banner */}
                <div className="mt-8 bg-gradient-to-r from-green-400 to-green-500 rounded-xl p-6 flex items-center justify-between">
                    <div className="flex items-center gap-4">
                        <div className="w-12 h-12 bg-white rounded-full flex items-center justify-center">
                            <svg className="w-7 h-7" viewBox="0 0 24 24" fill="#10b981">
                                <path d="M12 2C6.48 2 2 6.48 2 12s4.48 10 10 10 10-4.48 10-10S17.52 2 12 2zm-2 15l-5-5 1.41-1.41L10 14.17l7.59-7.59L19 8l-9 9z" />
                            </svg>
                        </div>
                        <div>
                            <h3 className="text-white font-bold text-lg">Get the extension</h3>
                            <p className="text-green-100 text-sm">Install Now</p>
                        </div>
                    </div>
                </div>
            </div>
        </DashboardLayout>
    );
};

export default DashboardHome;
