import { FunctionComponent } from 'preact';
import { useEffect, useState } from 'preact/hooks';
import { TrendingUp, TrendingDown, Calendar } from 'lucide-preact';
import DashboardLayout from '../../components/dashboard/DashboardLayout';
import StatCard from '../../components/dashboard/StatCard';
import ProtectedRoute from '../../components/ProtectedRoute';
import { MetricsAPI, type DashboardMetrics } from '../../api';

type Period = 'today' | 'this-week' | 'this-month' | 'this-year' | 'all-time';

export const DashboardHome: FunctionComponent = () => {
    const [metrics, setMetrics] = useState<DashboardMetrics | null>(null);
    const [selectedPeriod, setSelectedPeriod] = useState<Period>('all-time');
    const [loading, setLoading] = useState(true);

    useEffect(() => {
        fetchMetrics(selectedPeriod);
    }, [selectedPeriod]);

    async function fetchMetrics(period: Period) {
        setLoading(true);
        try {
            let response;

            switch (period) {
                case 'today':
                    response = await MetricsAPI.getToday(null as never);
                    break;
                case 'this-week':
                    response = await MetricsAPI.getThisWeek(null as never);
                    break;
                case 'this-month':
                    response = await MetricsAPI.getThisMonth(null as never);
                    break;
                case 'this-year':
                    response = await MetricsAPI.getThisYear(null as never);
                    break;
                case 'all-time':
                    response = await MetricsAPI.getAllTime(null as never);
                    break;
            }

            if (response?.data) {
                setMetrics(response.data);
            }
        } catch (error) {
            console.error('Failed to fetch metrics:', error);
        } finally {
            setLoading(false);
        }
    }

    const formatCurrency = (value: number) => {
        return new Intl.NumberFormat('en-US', {
            style: 'currency',
            currency: 'USD',
        }).format(value);
    };

    const formatPercentage = (value: number) => {
        return `${value >= 0 ? '+' : ''}${value.toFixed(1)}%`;
    };

    const getPeriodLabel = () => {
        switch (selectedPeriod) {
            case 'today': return 'Today';
            case 'this-week': return 'This Week';
            case 'this-month': return 'This Month';
            case 'this-year': return 'This Year';
            case 'all-time': return 'All Time';
        }
    };

    return (
        <ProtectedRoute allowedRoles={['ADMIN']}>
            <DashboardLayout>
                <div className="p-8">{/* Header */}
                    <div className="flex items-center justify-between mb-8">
                        <div>
                            <h1 className="text-3xl font-bold text-gray-900">Dashboard Overview</h1>
                            <p className="text-gray-600 mt-1">Monitoring your bus operations - {getPeriodLabel()}</p>
                        </div>

                        {/* Period Selector */}
                        <div className="flex gap-2">
                            <button
                                onClick={() => setSelectedPeriod('today')}
                                className={`px-4 py-2 rounded-lg transition-colors ${selectedPeriod === 'today'
                                    ? 'bg-purple-600 text-white'
                                    : 'bg-white border border-gray-300 text-gray-700 hover:bg-gray-50'
                                    }`}
                            >
                                Today
                            </button>
                            <button
                                onClick={() => setSelectedPeriod('this-week')}
                                className={`px-4 py-2 rounded-lg transition-colors ${selectedPeriod === 'this-week'
                                    ? 'bg-purple-600 text-white'
                                    : 'bg-white border border-gray-300 text-gray-700 hover:bg-gray-50'
                                    }`}
                            >
                                This Week
                            </button>
                            <button
                                onClick={() => setSelectedPeriod('this-month')}
                                className={`px-4 py-2 rounded-lg transition-colors ${selectedPeriod === 'this-month'
                                    ? 'bg-purple-600 text-white'
                                    : 'bg-white border border-gray-300 text-gray-700 hover:bg-gray-50'
                                    }`}
                            >
                                This Month
                            </button>
                            <button
                                onClick={() => setSelectedPeriod('this-year')}
                                className={`px-4 py-2 rounded-lg transition-colors ${selectedPeriod === 'this-year'
                                    ? 'bg-purple-600 text-white'
                                    : 'bg-white border border-gray-300 text-gray-700 hover:bg-gray-50'
                                    }`}
                            >
                                This Year
                            </button>
                            <button
                                onClick={() => setSelectedPeriod('all-time')}
                                className={`px-4 py-2 rounded-lg transition-colors ${selectedPeriod === 'all-time'
                                    ? 'bg-purple-600 text-white'
                                    : 'bg-white border border-gray-300 text-gray-700 hover:bg-gray-50'
                                    }`}
                            >
                                All Time
                            </button>
                        </div>
                    </div>

                    {loading ? (
                        <div className="flex items-center justify-center py-20">
                            <div className="text-gray-500">Loading metrics...</div>
                        </div>
                    ) : metrics ? (
                        <>
                            {/* Revenue Header */}
                            <div className="mb-8 p-6 bg-gradient-to-r from-purple-600 to-orange-400 rounded-xl text-white">
                                <h2 className="text-lg font-medium opacity-90">Total Revenue</h2>
                                <p className="text-5xl font-bold mt-2">
                                    {formatCurrency(metrics.revenue.totalRevenue)}
                                </p>
                                <div className="flex items-center mt-2">
                                    {metrics.revenue.changePercentage >= 0 ? (
                                        <TrendingUp className="w-4 h-4 mr-1" />
                                    ) : (
                                        <TrendingDown className="w-4 h-4 mr-1" />
                                    )}
                                    <span className="text-sm">
                                        {formatPercentage(metrics.revenue.changePercentage)} vs previous period
                                    </span>
                                </div>
                            </div>

                            {/* Metrics Grid */}
                            <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6 mb-8">
                                {/* Revenue */}
                                <div className="bg-white rounded-xl p-6 border border-gray-200">
                                    <h3 className="text-sm font-medium text-gray-600 mb-2">Tickets Sold</h3>
                                    <p className="text-3xl font-bold text-gray-900">
                                        {metrics.revenue.totalTicketsSold}
                                    </p>
                                    <p className="text-sm text-gray-500 mt-1">
                                        Avg: {formatCurrency(metrics.revenue.averageTicketPrice)}
                                    </p>
                                </div>

                                {/* Occupation */}
                                <div className="bg-white rounded-xl p-6 border border-gray-200">
                                    <h3 className="text-sm font-medium text-gray-600 mb-2">Occupation Rate</h3>
                                    <p className="text-3xl font-bold text-gray-900">
                                        {metrics.occupation.averageOccupation.toFixed(1)}%
                                    </p>
                                    <div className="flex items-center mt-1">
                                        <span className={`text-sm ${metrics.occupation.changePercentage >= 0 ? 'text-green-600' : 'text-red-600'
                                            }`}>
                                            {formatPercentage(metrics.occupation.changePercentage)}
                                        </span>
                                        <span className="text-sm text-gray-500 ml-1">vs previous</span>
                                    </div>
                                </div>

                                {/* Punctuality */}
                                <div className="bg-white rounded-xl p-6 border border-gray-200">
                                    <h3 className="text-sm font-medium text-gray-600 mb-2">Punctuality</h3>
                                    <p className="text-3xl font-bold text-gray-900">
                                        {metrics.punctuality.punctualityRate.toFixed(1)}%
                                    </p>
                                    <p className="text-sm text-gray-500 mt-1">
                                        {metrics.punctuality.totalTripsOnTime}/{metrics.punctuality.totalTripsCompleted} on time
                                    </p>
                                </div>

                                {/* Cancellations */}
                                <div className="bg-white rounded-xl p-6 border border-gray-200">
                                    <h3 className="text-sm font-medium text-gray-600 mb-2">Cancellation Rate</h3>
                                    <p className="text-3xl font-bold text-gray-900">
                                        {metrics.cancellations.cancellationRate.toFixed(1)}%
                                    </p>
                                    <p className="text-sm text-gray-500 mt-1">
                                        {metrics.cancellations.totalCancellations} cancellations
                                    </p>
                                </div>
                            </div>

                            {/* Detailed Stats */}
                            <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
                                <StatCard
                                    title="Total Trips"
                                    value={metrics.occupation.totalTrips.toString()}
                                    change={metrics.occupation.changePercentage}
                                    changeLabel="compared to previous period"
                                    trend={metrics.occupation.changePercentage >= 0 ? 'up' : 'down'}
                                />
                                <StatCard
                                    title="Seats Sold"
                                    value={metrics.occupation.totalSeatsSold.toString()}
                                    change={metrics.revenue.changePercentage}
                                    changeLabel="compared to previous period"
                                    trend={metrics.revenue.changePercentage >= 0 ? 'up' : 'down'}
                                />
                                <StatCard
                                    title="Delayed Trips"
                                    value={metrics.punctuality.totalTripsDelayed.toString()}
                                    change={-metrics.punctuality.changePercentage}
                                    changeLabel="compared to previous period"
                                    trend={metrics.punctuality.changePercentage >= 0 ? 'down' : 'up'}
                                />
                            </div>
                        </>
                    ) : (
                        <div className="flex items-center justify-center py-20">
                            <div className="text-gray-500">No metrics data available</div>
                        </div>
                    )}
                </div>
            </DashboardLayout>
        </ProtectedRoute>
    );
};

export default DashboardHome;
