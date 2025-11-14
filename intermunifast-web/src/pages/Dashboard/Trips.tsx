import { FunctionComponent } from 'preact';
import { useState } from 'preact/hooks';
import DashboardLayout from '../../components/dashboard/DashboardLayout';
import { Plus, Search, Edit, Trash2, Calendar, Clock, Bus as BusIcon, Route as RouteIcon } from 'lucide-preact';

interface Trip {
    id: number;
    routeName: string;
    busPlate: string;
    date: string;
    departureTime: string;
    arrivalTime: string;
    status: 'scheduled' | 'in-progress' | 'completed' | 'cancelled';
    price: number;
    availableSeats: number;
    totalSeats: number;
}

export const TripsPage: FunctionComponent = () => {
    const [showModal, setShowModal] = useState(false);
    const [selectedTrip, setSelectedTrip] = useState<Trip | null>(null);
    const [filterStatus, setFilterStatus] = useState<string>('all');

    // Mock data
    const trips: Trip[] = [
        {
            id: 1,
            routeName: 'Guatemala City - Quetzaltenango',
            busPlate: 'ABC-123',
            date: '2025-11-15',
            departureTime: '08:00',
            arrivalTime: '12:30',
            status: 'scheduled',
            price: 75,
            availableSeats: 15,
            totalSeats: 20
        },
        {
            id: 2,
            routeName: 'Antigua - Panajachel',
            busPlate: 'XYZ-789',
            date: '2025-11-15',
            departureTime: '09:00',
            arrivalTime: '11:15',
            status: 'in-progress',
            price: 45,
            availableSeats: 5,
            totalSeats: 45
        },
        {
            id: 3,
            routeName: 'Guatemala City - Quetzaltenango',
            busPlate: 'DEF-456',
            date: '2025-11-14',
            departureTime: '14:00',
            arrivalTime: '18:30',
            status: 'completed',
            price: 75,
            availableSeats: 0,
            totalSeats: 35
        },
    ];

    const getStatusColor = (status: string) => {
        switch (status) {
            case 'scheduled':
                return 'bg-blue-100 text-blue-700';
            case 'in-progress':
                return 'bg-green-100 text-green-700';
            case 'completed':
                return 'bg-gray-100 text-gray-700';
            case 'cancelled':
                return 'bg-red-100 text-red-700';
            default:
                return 'bg-gray-100 text-gray-700';
        }
    };

    const getOccupancyColor = (available: number, total: number) => {
        const percentage = ((total - available) / total) * 100;
        if (percentage >= 80) return 'text-green-600';
        if (percentage >= 50) return 'text-yellow-600';
        return 'text-red-600';
    };

    const filteredTrips = filterStatus === 'all'
        ? trips
        : trips.filter(trip => trip.status === filterStatus);

    return (
        <DashboardLayout>
            <div className="p-8">
                {/* Header */}
                <div className="flex items-center justify-between mb-8">
                    <div>
                        <h1 className="text-3xl font-bold text-gray-900">Trip Management</h1>
                        <p className="text-gray-600 mt-1">Schedule and manage bus trips</p>
                    </div>
                    <button
                        onClick={() => {
                            setSelectedTrip(null);
                            setShowModal(true);
                        }}
                        className="flex items-center gap-2 px-4 py-2 bg-purple-600 text-white rounded-lg hover:bg-purple-700 transition-colors"
                    >
                        <Plus className="w-4 h-4" />
                        Schedule New Trip
                    </button>
                </div>

                {/* Stats */}
                <div className="grid grid-cols-1 md:grid-cols-4 gap-6 mb-6">
                    <div className="bg-white rounded-xl p-6 shadow-sm border border-gray-200">
                        <div className="flex items-center justify-between">
                            <div>
                                <p className="text-sm text-gray-600 mb-1">Today's Trips</p>
                                <p className="text-2xl font-bold text-gray-900">12</p>
                            </div>
                            <div className="p-3 bg-blue-50 rounded-lg">
                                <Calendar className="w-6 h-6 text-blue-600" />
                            </div>
                        </div>
                    </div>
                    <div className="bg-white rounded-xl p-6 shadow-sm border border-gray-200">
                        <div className="flex items-center justify-between">
                            <div>
                                <p className="text-sm text-gray-600 mb-1">In Progress</p>
                                <p className="text-2xl font-bold text-green-600">3</p>
                            </div>
                            <div className="p-3 bg-green-50 rounded-lg">
                                <BusIcon className="w-6 h-6 text-green-600" />
                            </div>
                        </div>
                    </div>
                    <div className="bg-white rounded-xl p-6 shadow-sm border border-gray-200">
                        <div className="flex items-center justify-between">
                            <div>
                                <p className="text-sm text-gray-600 mb-1">Scheduled</p>
                                <p className="text-2xl font-bold text-blue-600">8</p>
                            </div>
                            <div className="p-3 bg-blue-50 rounded-lg">
                                <Clock className="w-6 h-6 text-blue-600" />
                            </div>
                        </div>
                    </div>
                    <div className="bg-white rounded-xl p-6 shadow-sm border border-gray-200">
                        <div className="flex items-center justify-between">
                            <div>
                                <p className="text-sm text-gray-600 mb-1">Completed</p>
                                <p className="text-2xl font-bold text-gray-900">45</p>
                            </div>
                            <div className="p-3 bg-gray-50 rounded-lg">
                                <RouteIcon className="w-6 h-6 text-gray-600" />
                            </div>
                        </div>
                    </div>
                </div>

                {/* Search and Filters */}
                <div className="flex gap-4 mb-6">
                    <div className="flex-1 relative">
                        <Search className="absolute left-3 top-1/2 transform -translate-y-1/2 w-5 h-5 text-gray-400" />
                        <input
                            type="text"
                            placeholder="Search trips by route, bus, or date..."
                            className="w-full pl-10 pr-4 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-purple-500"
                        />
                    </div>
                    <select
                        value={filterStatus}
                        onChange={(e) => setFilterStatus((e.target as HTMLSelectElement).value)}
                        className="px-4 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-purple-500"
                    >
                        <option value="all">All Status</option>
                        <option value="scheduled">Scheduled</option>
                        <option value="in-progress">In Progress</option>
                        <option value="completed">Completed</option>
                        <option value="cancelled">Cancelled</option>
                    </select>
                    <input
                        type="date"
                        className="px-4 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-purple-500"
                    />
                </div>

                {/* Trips Table */}
                <div className="bg-white rounded-xl shadow-sm border border-gray-200 overflow-hidden">
                    <table className="w-full">
                        <thead className="bg-gray-50 border-b border-gray-200">
                            <tr>
                                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                                    Route
                                </th>
                                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                                    Bus
                                </th>
                                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                                    Date & Time
                                </th>
                                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                                    Status
                                </th>
                                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                                    Occupancy
                                </th>
                                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                                    Price
                                </th>
                                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                                    Actions
                                </th>
                            </tr>
                        </thead>
                        <tbody className="bg-white divide-y divide-gray-200">
                            {filteredTrips.map((trip) => (
                                <tr key={trip.id} className="hover:bg-gray-50">
                                    <td className="px-6 py-4 whitespace-nowrap">
                                        <div className="flex items-center gap-2">
                                            <RouteIcon className="w-4 h-4 text-purple-600" />
                                            <span className="text-sm font-medium text-gray-900">{trip.routeName}</span>
                                        </div>
                                    </td>
                                    <td className="px-6 py-4 whitespace-nowrap">
                                        <div className="flex items-center gap-2">
                                            <BusIcon className="w-4 h-4 text-gray-400" />
                                            <span className="text-sm text-gray-900">{trip.busPlate}</span>
                                        </div>
                                    </td>
                                    <td className="px-6 py-4 whitespace-nowrap">
                                        <div className="text-sm text-gray-900">{new Date(trip.date).toLocaleDateString()}</div>
                                        <div className="text-xs text-gray-500">{trip.departureTime} - {trip.arrivalTime}</div>
                                    </td>
                                    <td className="px-6 py-4 whitespace-nowrap">
                                        <span className={`px-3 py-1 text-xs font-medium rounded-full ${getStatusColor(trip.status)}`}>
                                            {trip.status.split('-').map(word => word.charAt(0).toUpperCase() + word.slice(1)).join(' ')}
                                        </span>
                                    </td>
                                    <td className="px-6 py-4 whitespace-nowrap">
                                        <div className="flex items-center gap-2">
                                            <div className="flex-1 bg-gray-200 rounded-full h-2 max-w-[60px]">
                                                <div
                                                    className={`h-2 rounded-full ${((trip.totalSeats - trip.availableSeats) / trip.totalSeats) * 100 >= 80
                                                            ? 'bg-green-600'
                                                            : ((trip.totalSeats - trip.availableSeats) / trip.totalSeats) * 100 >= 50
                                                                ? 'bg-yellow-600'
                                                                : 'bg-red-600'
                                                        }`}
                                                    style={{ width: `${((trip.totalSeats - trip.availableSeats) / trip.totalSeats) * 100}%` }}
                                                />
                                            </div>
                                            <span className={`text-sm font-medium ${getOccupancyColor(trip.availableSeats, trip.totalSeats)}`}>
                                                {trip.totalSeats - trip.availableSeats}/{trip.totalSeats}
                                            </span>
                                        </div>
                                    </td>
                                    <td className="px-6 py-4 whitespace-nowrap">
                                        <span className="text-sm font-semibold text-gray-900">${trip.price}</span>
                                    </td>
                                    <td className="px-6 py-4 whitespace-nowrap">
                                        <div className="flex gap-2">
                                            <button
                                                onClick={() => {
                                                    setSelectedTrip(trip);
                                                    setShowModal(true);
                                                }}
                                                className="p-2 text-gray-600 hover:bg-gray-100 rounded-lg transition-colors"
                                            >
                                                <Edit className="w-4 h-4" />
                                            </button>
                                            <button className="p-2 text-red-600 hover:bg-red-50 rounded-lg transition-colors">
                                                <Trash2 className="w-4 h-4" />
                                            </button>
                                        </div>
                                    </td>
                                </tr>
                            ))}
                        </tbody>
                    </table>
                </div>
            </div>

            {/* Trip Form Modal */}
            {showModal && (
                <TripFormModal
                    trip={selectedTrip}
                    onClose={() => {
                        setShowModal(false);
                        setSelectedTrip(null);
                    }}
                />
            )}
        </DashboardLayout>
    );
};

// Trip Form Modal
const TripFormModal: FunctionComponent<{ trip: Trip | null; onClose: () => void }> = ({ trip, onClose }) => {
    return (
        <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50 p-4">
            <div className="bg-white rounded-xl max-w-2xl w-full max-h-[90vh] overflow-y-auto">
                <div className="p-6 border-b border-gray-200">
                    <h2 className="text-2xl font-bold text-gray-900">
                        {trip ? 'Edit Trip' : 'Schedule New Trip'}
                    </h2>
                </div>

                <div className="p-6">
                    <form className="space-y-4">
                        <div>
                            <label className="block text-sm font-medium text-gray-700 mb-1">
                                Route
                            </label>
                            <select
                                defaultValue={trip?.routeName}
                                className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-purple-500"
                            >
                                <option value="">Select a route</option>
                                <option value="Guatemala City - Quetzaltenango">Guatemala City - Quetzaltenango</option>
                                <option value="Antigua - Panajachel">Antigua - Panajachel</option>
                            </select>
                        </div>

                        <div>
                            <label className="block text-sm font-medium text-gray-700 mb-1">
                                Bus
                            </label>
                            <select
                                defaultValue={trip?.busPlate}
                                className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-purple-500"
                            >
                                <option value="">Select a bus</option>
                                <option value="ABC-123">ABC-123 - Mercedes-Benz Sprinter</option>
                                <option value="XYZ-789">XYZ-789 - Volvo 9700</option>
                                <option value="DEF-456">DEF-456 - Scania Touring</option>
                            </select>
                        </div>

                        <div className="grid grid-cols-3 gap-4">
                            <div>
                                <label className="block text-sm font-medium text-gray-700 mb-1">
                                    Date
                                </label>
                                <input
                                    type="date"
                                    defaultValue={trip?.date}
                                    className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-purple-500"
                                />
                            </div>
                            <div>
                                <label className="block text-sm font-medium text-gray-700 mb-1">
                                    Departure
                                </label>
                                <input
                                    type="time"
                                    defaultValue={trip?.departureTime}
                                    className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-purple-500"
                                />
                            </div>
                            <div>
                                <label className="block text-sm font-medium text-gray-700 mb-1">
                                    Arrival
                                </label>
                                <input
                                    type="time"
                                    defaultValue={trip?.arrivalTime}
                                    className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-purple-500"
                                />
                            </div>
                        </div>

                        <div className="grid grid-cols-2 gap-4">
                            <div>
                                <label className="block text-sm font-medium text-gray-700 mb-1">
                                    Price ($)
                                </label>
                                <input
                                    type="number"
                                    defaultValue={trip?.price}
                                    className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-purple-500"
                                    placeholder="75"
                                />
                            </div>
                            <div>
                                <label className="block text-sm font-medium text-gray-700 mb-1">
                                    Status
                                </label>
                                <select
                                    defaultValue={trip?.status}
                                    className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-purple-500"
                                >
                                    <option value="scheduled">Scheduled</option>
                                    <option value="in-progress">In Progress</option>
                                    <option value="completed">Completed</option>
                                    <option value="cancelled">Cancelled</option>
                                </select>
                            </div>
                        </div>
                    </form>
                </div>

                <div className="p-6 border-t border-gray-200 flex gap-3 justify-end">
                    <button
                        onClick={onClose}
                        className="px-4 py-2 border border-gray-300 text-gray-700 rounded-lg hover:bg-gray-50 transition-colors"
                    >
                        Cancel
                    </button>
                    <button className="px-4 py-2 bg-purple-600 text-white rounded-lg hover:bg-purple-700 transition-colors">
                        {trip ? 'Save Changes' : 'Schedule Trip'}
                    </button>
                </div>
            </div>
        </div>
    );
};

export default TripsPage;
