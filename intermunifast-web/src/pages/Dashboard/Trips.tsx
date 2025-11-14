import { FunctionComponent } from 'preact';
import { useEffect, useState } from 'preact/hooks';
import DashboardLayout from '../../components/dashboard/DashboardLayout';
import { Plus, Search, Edit, Trash2, Calendar, Clock, Bus as BusIcon, Navigation } from 'lucide-preact';
import { TripAPI, TripResponse, BusAPI, BusResponse } from '../../api';
import useAccountStore from '../../stores/AccountStore';
import useRouteStore from '../../stores/RouteStore';
import { RouteAPI } from '../../api';

export const TripsPage: FunctionComponent = () => {
    const { accountId, account } = useAccountStore();
    const { routes, setRoutes, lastUpdated: routesLastUpdated, setLoading: setRoutesLoading } = useRouteStore();

    if (!accountId || !account) {
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

    const [trips, setTrips] = useState<TripResponse[]>([]);
    const [buses, setBuses] = useState<BusResponse[]>([]);
    const [showModal, setShowModal] = useState(false);
    const [selectedTrip, setSelectedTrip] = useState<TripResponse | null>(null);
    const [loading, setLoading] = useState(true);

    async function createTrip(tripData: any) {
        try {
            const response = await TripAPI.create({
                routeId: tripData.routeId,
                busId: tripData.busId,
                departureAt: tripData.departureAt,
                arrivalAt: tripData.arrivalAt
            });

            setTrips([...trips, response.data]);
        } catch (error) {
            console.error('Failed to create trip:', error);
            throw error;
        }
    }

    const handleEditTrip = (trip: TripResponse) => {
        setSelectedTrip(trip);
        setShowModal(true);
    };

    const handleDeleteTrip = async (tripId: number) => {
        if (!confirm('Are you sure you want to delete this trip?')) return;

        try {
            await TripAPI.delete(undefined, {
                pathParams: { id: tripId }
            });
            setTrips(trips.filter(t => t.id !== tripId));
        } catch (error) {
            console.error('Failed to delete trip:', error);
        }
    };

    useEffect(() => {
        const fetchData = async () => {
            try {
                setLoading(true);

                // Fetch routes if not already loaded
                const fiveMinutes = 5 * 60 * 1000;
                const isStale = !routesLastUpdated || (Date.now() - routesLastUpdated) > fiveMinutes;
                if (routes.length === 0 || isStale) {
                    setRoutesLoading(true);
                    const routesResponse = await RouteAPI.getAll();
                    setRoutes(routesResponse.data);
                    setRoutesLoading(false);
                }

                // Fetch trips
                const tripsResponse = await TripAPI.getAll();
                setTrips(tripsResponse.data);

                // Fetch buses
                const busesResponse = await BusAPI.getAll();
                setBuses(busesResponse.data);
            } catch (error) {
                console.error('Failed to fetch data:', error);
            } finally {
                setLoading(false);
            }
        };

        fetchData();
    }, []);

    const getRouteName = (routeId: number) => {
        const route = routes.find(r => r.id === routeId);
        return route ? `${route.origin} → ${route.destination}` : 'Unknown Route';
    };

    const getBusPlate = (busId: number) => {
        const bus = buses.find(b => b.id === busId);
        return bus ? bus.plate : 'Unknown Bus';
    };

    const formatDateTime = (dateTimeStr?: string) => {
        if (!dateTimeStr) return 'Not set';
        const date = new Date(dateTimeStr);
        return date.toLocaleString('en-US', {
            month: 'short',
            day: 'numeric',
            year: 'numeric',
            hour: '2-digit',
            minute: '2-digit'
        });
    };

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

                {/* Search */}
                <div className="flex gap-4 mb-6">
                    <div className="flex-1 relative">
                        <Search className="absolute left-3 top-1/2 transform -translate-y-1/2 w-5 h-5 text-gray-400" />
                        <input
                            type="text"
                            placeholder="Search trips by route or bus..."
                            className="w-full pl-10 pr-4 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-purple-500"
                        />
                    </div>
                </div>

                {/* Trips Grid */}
                {loading ? (
                    <div className="text-center py-12">
                        <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-purple-600 mx-auto"></div>
                        <p className="text-gray-600 mt-4">Loading trips...</p>
                    </div>
                ) : (
                    <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
                        {trips.map((trip) => (
                            <div key={trip.id} className="bg-white rounded-xl shadow-sm border border-gray-200 overflow-hidden hover:shadow-md transition-shadow">
                                <div className="p-6">
                                    <div className="flex items-start justify-between mb-4">
                                        <div className="flex-1">
                                            <h3 className="text-lg font-semibold text-gray-900 mb-1">
                                                {getRouteName(trip.routeId)}
                                            </h3>
                                            <p className="text-sm text-gray-500">Trip #{trip.id}</p>
                                        </div>
                                    </div>

                                    <div className="space-y-3 mb-4">
                                        <div className="flex items-center gap-2 text-sm text-gray-600">
                                            <BusIcon className="w-4 h-4" />
                                            <span>{getBusPlate(trip.busId)}</span>
                                        </div>
                                        <div className="flex items-center gap-2 text-sm text-gray-600">
                                            <Navigation className="w-4 h-4" />
                                            <span>Route ID: {trip.routeId}</span>
                                        </div>
                                        {trip.departureAt && (
                                            <div className="flex items-center gap-2 text-sm text-gray-600">
                                                <Clock className="w-4 h-4" />
                                                <span>Departs: {formatDateTime(trip.departureAt)}</span>
                                            </div>
                                        )}
                                        {trip.arrivalAt && (
                                            <div className="flex items-center gap-2 text-sm text-gray-600">
                                                <Clock className="w-4 h-4" />
                                                <span>Arrives: {formatDateTime(trip.arrivalAt)}</span>
                                            </div>
                                        )}
                                    </div>

                                    <div className="flex gap-2">
                                        <button
                                            onClick={() => handleEditTrip(trip)}
                                            className="flex-1 px-3 py-2 bg-gray-100 text-gray-700 rounded-lg hover:bg-gray-200 transition-colors text-sm font-medium"
                                        >
                                            <Edit className="w-4 h-4 inline mr-1" />
                                            Edit
                                        </button>
                                        <button
                                            onClick={() => handleDeleteTrip(trip.id)}
                                            className="px-3 py-2 bg-red-50 text-red-600 rounded-lg hover:bg-red-100 transition-colors"
                                        >
                                            <Trash2 className="w-4 h-4" />
                                        </button>
                                    </div>
                                </div>
                            </div>
                        ))}

                        {trips.length === 0 && (
                            <div className="col-span-full text-center py-12">
                                <Calendar className="w-16 h-16 text-gray-400 mx-auto mb-4" />
                                <p className="text-gray-600">No trips scheduled yet</p>
                                <p className="text-sm text-gray-500 mt-1">Click "Schedule New Trip" to create your first trip</p>
                            </div>
                        )}
                    </div>
                )}

                {/* Modals */}
                {showModal && (
                    <TripFormModal
                        trip={selectedTrip}
                        routes={routes}
                        buses={buses}
                        onClose={() => {
                            setShowModal(false);
                            setSelectedTrip(null);
                        }}
                        onSave={async (tripData) => {
                            if (selectedTrip) {
                                // Update existing trip
                                const response = await TripAPI.update({
                                    routeId: tripData.routeId,
                                    busId: tripData.busId,
                                    departureAt: tripData.departureAt,
                                    arrivalAt: tripData.arrivalAt
                                }, {
                                    pathParams: { id: selectedTrip.id }
                                });
                                setTrips(trips.map(t => t.id === selectedTrip.id ? response.data : t));
                            } else {
                                // Create new trip
                                await createTrip(tripData);
                            }
                            setShowModal(false);
                            setSelectedTrip(null);
                        }}
                    />
                )}
            </div>
        </DashboardLayout>
    );
};

// Trip Form Modal Component
const TripFormModal: FunctionComponent<{
    trip: TripResponse | null;
    routes: any[];
    buses: BusResponse[];
    onClose: () => void;
    onSave: (tripData: any) => Promise<void>;
}> = ({ trip, routes, buses, onClose, onSave }) => {
    // Helper to convert ISO string to datetime-local format
    const toDateTimeLocal = (isoString?: string) => {
        if (!isoString) return '';
        const date = new Date(isoString);
        const offset = date.getTimezoneOffset() * 60000;
        const localDate = new Date(date.getTime() - offset);
        return localDate.toISOString().slice(0, 16);
    };

    const [formData, setFormData] = useState({
        routeId: trip?.routeId || (routes[0]?.id || 0),
        busId: trip?.busId || (buses[0]?.id || 0),
        departureAt: toDateTimeLocal(trip?.departureAt) || '',
        arrivalAt: toDateTimeLocal(trip?.arrivalAt) || ''
    });
    const [loading, setLoading] = useState(false);

    const handleSubmit = async (e: Event) => {
        e.preventDefault();
        setLoading(true);

        try {
            // Convert datetime-local to ISO 8601 format
            const tripData = {
                ...formData,
                departureAt: formData.departureAt ? new Date(formData.departureAt).toISOString() : '',
                arrivalAt: formData.arrivalAt ? new Date(formData.arrivalAt).toISOString() : ''
            };
            await onSave(tripData);
            onClose();
        } catch (error) {
            console.error('Failed to save trip:', error);
        } finally {
            setLoading(false);
        }
    };

    return (
        <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50 p-4">
            <div className="bg-white rounded-xl max-w-2xl w-full max-h-[90vh] overflow-y-auto">
                <div className="p-6 border-b border-gray-200">
                    <h2 className="text-2xl font-bold text-gray-900">
                        {trip ? 'Edit Trip' : 'Schedule New Trip'}
                    </h2>
                </div>

                <div className="p-6">
                    <form onSubmit={handleSubmit} className="space-y-4">
                        <div>
                            <label className="block text-sm font-medium text-gray-700 mb-1">
                                Route *
                            </label>
                            <select
                                value={formData.routeId}
                                onChange={(e) => setFormData({ ...formData, routeId: parseInt((e.target as HTMLSelectElement).value) })}
                                className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-purple-500"
                                required
                            >
                                <option value="">Select a route</option>
                                {routes.map((route) => (
                                    <option key={route.id} value={route.id}>
                                        {route.code} - {route.origin} → {route.destination}
                                    </option>
                                ))}
                            </select>
                        </div>

                        <div>
                            <label className="block text-sm font-medium text-gray-700 mb-1">
                                Bus *
                            </label>
                            <select
                                value={formData.busId}
                                onChange={(e) => setFormData({ ...formData, busId: parseInt((e.target as HTMLSelectElement).value) })}
                                className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-purple-500"
                                required
                            >
                                <option value="">Select a bus</option>
                                {buses.map((bus) => (
                                    <option key={bus.id} value={bus.id}>
                                        {bus.plate} - {bus.capacity} seats
                                    </option>
                                ))}
                            </select>
                        </div>

                        <div>
                            <label className="block text-sm font-medium text-gray-700 mb-1">
                                Departure Time *
                            </label>
                            <input
                                type="datetime-local"
                                value={formData.departureAt}
                                onChange={(e) => setFormData({ ...formData, departureAt: (e.target as HTMLInputElement).value })}
                                className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-purple-500"
                                required
                            />
                        </div>

                        <div>
                            <label className="block text-sm font-medium text-gray-700 mb-1">
                                Arrival Time *
                            </label>
                            <input
                                type="datetime-local"
                                value={formData.arrivalAt}
                                onChange={(e) => setFormData({ ...formData, arrivalAt: (e.target as HTMLInputElement).value })}
                                className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-purple-500"
                                required
                            />
                        </div>

                        <div className="pt-4 border-t border-gray-200 flex gap-3 justify-end">
                            <button
                                type="button"
                                onClick={onClose}
                                className="px-4 py-2 border border-gray-300 text-gray-700 rounded-lg hover:bg-gray-50 transition-colors"
                            >
                                Cancel
                            </button>
                            <button
                                type="submit"
                                disabled={loading}
                                className="px-4 py-2 bg-purple-600 text-white rounded-lg hover:bg-purple-700 transition-colors disabled:bg-purple-400"
                            >
                                {loading ? 'Saving...' : (trip ? 'Save Changes' : 'Schedule Trip')}
                            </button>
                        </div>
                    </form>
                </div>
            </div>
        </div>
    );
};

export default TripsPage;
