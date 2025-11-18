import { FunctionComponent } from 'preact';
import { useEffect, useState } from 'preact/hooks';
import DashboardLayout from '../../components/dashboard/DashboardLayout';
import { Plus, Search, Edit, Trash2, MapPin, Clock, DollarSign, Navigation } from 'lucide-preact';
import { RouteAPI, RouteResponse, StopAPI, StopResponse } from '../../api';
import useRouteStore from '../../stores/RouteStore';
import ProtectedRoute from '../../components/ProtectedRoute';

export const RoutesPage: FunctionComponent = () => {
    const { routes, setRoutes, addRoute, updateRoute, removeRoute, lastUpdated, isLoading, setLoading } = useRouteStore();
    const [showModal, setShowModal] = useState(false);
    const [selectedRoute, setSelectedRoute] = useState<RouteResponse | null>(null);
    const [showStopEditor, setShowStopEditor] = useState(false);

    async function createRoute(routeData: any) {
        try {
            const response = await RouteAPI.create({
                code: routeData.code,
                name: routeData.name,
                origin: routeData.origin,
                destination: routeData.destination,
                durationMinutes: routeData.durationMinutes,
                distanceKm: routeData.distanceKm,
                pricePerKm: routeData.pricePerKm
            });

            addRoute(response.data);
        } catch (error) {
            console.error('Failed to create route:', error);
            throw error;
        }
    }

    const handleEditRoute = (route: RouteResponse) => {
        setSelectedRoute(route);
        setShowModal(true);
    };

    const handleManageStops = (route: RouteResponse) => {
        setSelectedRoute(route);
        setShowStopEditor(true);
    };

    const handleDeleteRoute = async (routeId: number) => {
        if (!confirm('Are you sure you want to delete this route?')) return;

        try {
            await RouteAPI.delete(undefined, {
                pathParams: { id: routeId }
            });
            removeRoute(routeId);
        } catch (error) {
            console.error('Failed to delete route:', error);
        }
    };

    useEffect(() => {
        const fetchRoutes = async () => {
            // Only fetch if routes are empty or data is stale (older than 5 minutes)
            const fiveMinutes = 5 * 60 * 1000;
            const isStale = !lastUpdated || (Date.now() - lastUpdated) > fiveMinutes;

            if (routes.length === 0 || isStale) {
                try {
                    setLoading(true);
                    const response = await RouteAPI.getAll();
                    setRoutes(response.data);
                } catch (error) {
                    console.error('Failed to fetch routes:', error);
                } finally {
                    setLoading(false);
                }
            }
        };

        fetchRoutes();
    }, []);

    return (
        <ProtectedRoute allowedRoles={['ADMIN']}>
            <DashboardLayout>
                <div className="p-8">
                    {/* Header */}
                    <div className="flex items-center justify-between mb-8">
                        <div>
                            <h1 className="text-3xl font-bold text-white">Route Management</h1>
                            <p className="text-white/80 mt-1">Manage your bus routes and stops</p>
                        </div>
                        <button
                            onClick={() => {
                                setSelectedRoute(null);
                                setShowModal(true);
                            }}
                            className="flex items-center gap-2 px-4 py-2 bg-accent text-white rounded-xl hover:bg-accent-dark transition-all duration-200 font-medium shadow-lg"
                        >
                            <Plus className="w-4 h-4" />
                            Add New Route
                        </button>
                    </div>

                    {/* Search and Filters */}
                    <div className="flex gap-4 mb-6">
                        <div className="flex-1 relative">
                            <Search className="absolute left-3 top-1/2 transform -translate-y-1/2 w-5 h-5 text-neutral-400" />
                            <input
                                type="text"
                                placeholder="Search by route code, name, origin, or destination..."
                                className="w-full pl-10 pr-4 py-2 border border-white/10 bg-neutral-950 text-white placeholder-neutral-500 rounded-xl focus:outline-none focus:ring-2 focus:ring-accent"
                            />
                        </div>
                    </div>

                    {/* Routes Grid */}
                    <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
                        {routes.map((route) => (
                            <div key={route.id} className="bg-white/5 backdrop-blur-xl rounded-2xl shadow-lg border border-white/10 overflow-hidden hover:border-accent transition-all duration-200">
                                <div className="p-6">
                                    <div className="flex items-start justify-between mb-4">
                                        <div>
                                            <h3 className="text-lg font-semibold text-white">{route.name}</h3>
                                            <p className="text-sm text-neutral-400 mt-1">Code: {route.code}</p>
                                        </div>
                                    </div>

                                    <div className="space-y-3 mb-4">
                                        <div className="flex items-center gap-2 text-sm text-neutral-300">
                                            <Navigation className="w-4 h-4 text-accent" />
                                            <span>{route.origin} → {route.destination}</span>
                                        </div>
                                        <div className="grid grid-cols-2 gap-2 text-sm text-neutral-300">
                                            <div className="flex items-center gap-1">
                                                <Clock className="w-4 h-4 text-accent" />
                                                <span>{route.durationMinutes} min</span>
                                            </div>
                                            <div className="flex items-center gap-1">
                                                <MapPin className="w-4 h-4 text-accent" />
                                                <span>{route.distanceKm} km</span>
                                            </div>
                                        </div>
                                        <div className="flex items-center gap-2 text-sm font-medium text-accent bg-accent/10 px-3 py-1.5 rounded-lg">
                                            <DollarSign className="w-4 h-4" />
                                            <span>${route.pricePerKm}/km = ${(route.pricePerKm * route.distanceKm).toFixed(2)} total</span>
                                        </div>
                                    </div>

                                    <div className="flex gap-2">
                                        <button
                                            onClick={() => handleManageStops(route)}
                                            className="flex-1 flex items-center justify-center gap-2 px-3 py-2 bg-accent/10 text-accent rounded-xl hover:bg-accent/20 transition-all duration-200 text-sm font-medium"
                                        >
                                            <MapPin className="w-4 h-4" />
                                            Manage Stops
                                        </button>
                                        <button
                                            onClick={() => handleEditRoute(route)}
                                            className="px-3 py-2 bg-white/10 text-neutral-300 rounded-xl hover:bg-white/20 transition-all duration-200"
                                        >
                                            <Edit className="w-4 h-4" />
                                        </button>
                                        <button
                                            onClick={() => handleDeleteRoute(route.id)}
                                            className="px-3 py-2 bg-accent/20 text-accent rounded-xl hover:bg-accent/30 transition-all duration-200"
                                        >
                                            <Trash2 className="w-4 h-4" />
                                        </button>
                                    </div>
                                </div>
                            </div>
                        ))}
                    </div>

                    {/* Modals */}
                    {showModal && (
                        <RouteFormModal
                            route={selectedRoute}
                            onClose={() => {
                                setShowModal(false);
                                setSelectedRoute(null);
                            }}
                            onSave={async (routeData) => {
                                if (selectedRoute) {
                                    // Update existing route
                                    const response = await RouteAPI.update({
                                        code: routeData.code,
                                        name: routeData.name,
                                        origin: routeData.origin,
                                        destination: routeData.destination,
                                        durationMinutes: routeData.durationMinutes,
                                        distanceKm: routeData.distanceKm,
                                        pricePerKm: routeData.pricePerKm
                                    }, {
                                        pathParams: { id: selectedRoute.id }
                                    });
                                    updateRoute(selectedRoute.id, response.data);
                                } else {
                                    // Create new route
                                    await createRoute(routeData);
                                }
                                setShowModal(false);
                                setSelectedRoute(null);
                            }}
                        />
                    )}

                    {showStopEditor && selectedRoute && (
                        <StopEditorModal
                            route={selectedRoute}
                            onClose={() => {
                                setShowStopEditor(false);
                                setSelectedRoute(null);
                            }}
                        />
                    )}
                </div>
            </DashboardLayout>
        </ProtectedRoute>
    );
};

// Route Form Modal Component
const RouteFormModal: FunctionComponent<{
    route: RouteResponse | null;
    onClose: () => void;
    onSave: (routeData: any) => Promise<void>;
}> = ({ route, onClose, onSave }) => {
    const [formData, setFormData] = useState({
        code: route?.code || '',
        name: route?.name || '',
        origin: route?.origin || '',
        destination: route?.destination || '',
        durationMinutes: route?.durationMinutes || 60,
        distanceKm: route?.distanceKm || 0,
        pricePerKm: route?.pricePerKm || 0
    });
    const [loading, setLoading] = useState(false);

    const handleSubmit = async (e: Event) => {
        e.preventDefault();
        setLoading(true);

        try {
            await onSave(formData);
            onClose();
        } catch (error) {
            console.error('Failed to save route:', error);
        } finally {
            setLoading(false);
        }
    };

    return (
        <div className="fixed inset-0 bg-black/80 backdrop-blur-md flex items-center justify-center z-50 p-4">
            <div className="bg-neutral-900/95 backdrop-blur-xl rounded-2xl shadow-2xl w-full max-w-2xl m-4 border border-white/10 max-h-[90vh] overflow-y-auto">
                <div className="p-6 border-b border-neutral-800">
                    <h2 className="text-2xl font-bold text-white">
                        {route ? 'Edit Route' : 'Add New Route'}
                    </h2>
                </div>

                <div className="p-6">
                    <form onSubmit={handleSubmit} className="space-y-4">
                        <div className="grid grid-cols-2 gap-4">
                            <div>
                                <label className="block text-sm font-medium text-neutral-400 mb-1">
                                    Route Code *
                                </label>
                                <input
                                    type="text"
                                    value={formData.code}
                                    onChange={(e) => setFormData({ ...formData, code: (e.target as HTMLInputElement).value })}
                                    className="w-full px-3 py-2 bg-neutral-950 border border-white/10 text-white rounded-xl focus:outline-none focus:ring-2 focus:ring-accent"
                                    placeholder="R-001"
                                    required
                                />
                            </div>
                            <div>
                                <label className="block text-sm font-medium text-neutral-400 mb-1">
                                    Route Name *
                                </label>
                                <input
                                    type="text"
                                    value={formData.name}
                                    onChange={(e) => setFormData({ ...formData, name: (e.target as HTMLInputElement).value })}
                                    className="w-full px-3 py-2 bg-neutral-950 border border-white/10 text-white rounded-xl focus:outline-none focus:ring-2 focus:ring-accent"
                                    placeholder="Express Route"
                                    required
                                />
                            </div>
                        </div>

                        <div className="grid grid-cols-2 gap-4">
                            <div>
                                <label className="block text-sm font-medium text-neutral-400 mb-1">
                                    Origin *
                                </label>
                                <input
                                    type="text"
                                    value={formData.origin}
                                    onChange={(e) => setFormData({ ...formData, origin: (e.target as HTMLInputElement).value })}
                                    className="w-full px-3 py-2 bg-neutral-950 border border-white/10 text-white rounded-xl focus:outline-none focus:ring-2 focus:ring-accent"
                                    placeholder="City A"
                                    required
                                />
                            </div>
                            <div>
                                <label className="block text-sm font-medium text-neutral-400 mb-1">
                                    Destination *
                                </label>
                                <input
                                    type="text"
                                    value={formData.destination}
                                    onChange={(e) => setFormData({ ...formData, destination: (e.target as HTMLInputElement).value })}
                                    className="w-full px-3 py-2 bg-neutral-950 border border-white/10 text-white rounded-xl focus:outline-none focus:ring-2 focus:ring-accent"
                                    placeholder="City B"
                                    required
                                />
                            </div>
                        </div>

                        <div className="grid grid-cols-3 gap-4">
                            <div>
                                <label className="block text-sm font-medium text-neutral-400 mb-1">
                                    Duration (minutes) *
                                </label>
                                <input
                                    type="number"
                                    value={formData.durationMinutes}
                                    onChange={(e) => setFormData({ ...formData, durationMinutes: parseInt((e.target as HTMLInputElement).value) || 0 })}
                                    className="w-full px-3 py-2 bg-neutral-950 border border-white/10 text-white rounded-xl focus:outline-none focus:ring-2 focus:ring-accent"
                                    placeholder="60"
                                    min="1"
                                    required
                                />
                            </div>
                            <div>
                                <label className="block text-sm font-medium text-neutral-400 mb-1">
                                    Distance (km) *
                                </label>
                                <input
                                    type="number"
                                    step="0.1"
                                    value={formData.distanceKm}
                                    onChange={(e) => setFormData({ ...formData, distanceKm: parseFloat((e.target as HTMLInputElement).value) || 0 })}
                                    className="w-full px-3 py-2 bg-neutral-950 border border-white/10 text-white rounded-xl focus:outline-none focus:ring-2 focus:ring-accent"
                                    placeholder="50.5"
                                    min="0.1"
                                    required
                                />
                            </div>
                            <div>
                                <label className="block text-sm font-medium text-neutral-400 mb-1">
                                    Price per km *
                                </label>
                                <input
                                    type="number"
                                    step="0.01"
                                    value={formData.pricePerKm}
                                    onChange={(e) => setFormData({ ...formData, pricePerKm: parseFloat((e.target as HTMLInputElement).value) || 0 })}
                                    className="w-full px-3 py-2 bg-neutral-950 border border-white/10 text-white rounded-xl focus:outline-none focus:ring-2 focus:ring-accent"
                                    placeholder="1.50"
                                    min="0.01"
                                    required
                                />
                            </div>
                        </div>

                        <div className="bg-accent/10 p-4 rounded-lg">
                            <p className="text-sm text-accent">
                                <span className="font-medium">Total Price:</span> ${(formData.distanceKm * formData.pricePerKm).toFixed(2)}
                            </p>
                        </div>

                        <div className="pt-4 border-t border-white/10 flex gap-3 justify-end">
                            <button
                                type="button"
                                onClick={onClose}
                                className="px-4 py-2 bg-white/10 text-white rounded-xl hover:bg-white/20 transition-all duration-200"
                            >
                                Cancel
                            </button>
                            <button
                                type="submit"
                                disabled={loading}
                                className="px-4 py-2 bg-accent text-white font-medium rounded-xl hover:bg-accent-dark transition-all duration-200 disabled:bg-accent/50 shadow-lg"
                            >
                                {loading ? 'Saving...' : (route ? 'Save Changes' : 'Create Route')}
                            </button>
                        </div>
                    </form>
                </div>
            </div>
        </div>
    );
};

// Stop Editor Modal Component
const StopEditorModal: FunctionComponent<{ route: RouteResponse; onClose: () => void }> = ({ route, onClose }) => {
    const [stops, setStops] = useState<StopResponse[]>([]);
    const [loading, setLoading] = useState(true);
    const [showAddForm, setShowAddForm] = useState(false);
    const [formData, setFormData] = useState({
        name: '',
        latitude: 0,
        longitude: 0
    });

    useEffect(() => {
        fetchStops();
    }, [route.id]);

    const fetchStops = async () => {
        try {
            setLoading(true);
            const response = await RouteAPI.getStops(undefined, {
                pathParams: { id: route.id }
            });
            setStops(response.data);
        } catch (error) {
            console.error('Failed to fetch stops:', error);
            setStops([]);
        } finally {
            setLoading(false);
        }
    };

    const createStop = async () => {
        try {
            const response = await StopAPI.create({
                name: formData.name,
                sequence: stops.length + 1,
                latitude: formData.latitude,
                longitude: formData.longitude,
                routeId: route.id
            });
            setStops([...stops, response.data]);
            setFormData({ name: '', latitude: 0, longitude: 0 });
            setShowAddForm(false);
        } catch (error) {
            console.error('Failed to create stop:', error);
        }
    };

    const deleteStop = async (stopId: number) => {
        if (!confirm('Are you sure you want to delete this stop?')) return;

        try {
            await StopAPI.delete(undefined, {
                pathParams: { id: stopId }
            });
            setStops(stops.filter(s => s.id !== stopId));
        } catch (error) {
            console.error('Failed to delete stop:', error);
        }
    };

    return (
        <div className="fixed inset-0 bg-black/80 backdrop-blur-md flex items-center justify-center z-50 p-4">
            <div className="bg-neutral-900/95 backdrop-blur-xl rounded-2xl shadow-2xl w-full max-w-4xl m-4 border border-white/10 max-h-[90vh] overflow-y-auto">
                <div className="p-6 border-b border-white/10">
                    <h2 className="text-2xl font-bold text-white">
                        Manage Stops - {route.name}
                    </h2>
                    <p className="text-neutral-400 mt-1">
                        {route.origin} → {route.destination}
                    </p>
                </div>

                <div className="p-6">
                    {loading ? (
                        <div className="text-center py-12">
                            <div className="inline-block animate-spin rounded-full h-12 w-12 border-b-2 border-accent"></div>
                            <p className="text-neutral-400 mt-4">Loading stops...</p>
                        </div>
                    ) : (
                        <>
                            {/* Stops List */}
                            <div className="space-y-3 mb-6">
                                {stops.length === 0 ? (
                                    <div className="text-center py-8 bg-neutral-950 rounded-xl">
                                        <MapPin className="w-12 h-12 text-neutral-500 mx-auto mb-3" />
                                        <p className="text-neutral-400">No stops added yet</p>
                                        <p className="text-sm text-neutral-500 mt-1">Click "Add Stop" to create your first stop</p>
                                    </div>
                                ) : (
                                    stops.map((stop, index) => (
                                        <div key={stop.id} className="flex items-center gap-4 p-4 bg-white/5 backdrop-blur-xl rounded-xl hover:bg-white/10 transition-all duration-200 border border-white/10">
                                            <div className="flex items-center justify-center w-8 h-8 bg-accent text-white rounded-full font-semibold text-sm">
                                                {stop.sequence}
                                            </div>
                                            <div className="flex-1">
                                                <h4 className="font-medium text-white">{stop.name}</h4>
                                                <p className="text-sm text-neutral-400">
                                                    Lat: {stop.latitude.toFixed(6)}, Lng: {stop.longitude.toFixed(6)}
                                                </p>
                                            </div>
                                            <button
                                                onClick={() => deleteStop(stop.id)}
                                                className="px-3 py-2 bg-accent/20 text-accent rounded-xl hover:bg-accent/30 transition-all duration-200"
                                            >
                                                <Trash2 className="w-4 h-4" />
                                            </button>
                                        </div>
                                    ))
                                )}
                            </div>

                            {/* Add Stop Form */}
                            {showAddForm ? (
                                <div className="bg-accent/10 p-4 rounded-xl space-y-3 border border-accent/20">
                                    <h3 className="font-medium text-white">Add New Stop</h3>
                                    <div>
                                        <label className="block text-sm font-medium text-neutral-400 mb-1">
                                            Stop Name *
                                        </label>
                                        <input
                                            type="text"
                                            value={formData.name}
                                            onChange={(e) => setFormData({ ...formData, name: (e.target as HTMLInputElement).value })}
                                            className="w-full px-3 py-2 bg-neutral-950 border border-white/10 text-white rounded-xl focus:outline-none focus:ring-2 focus:ring-accent"
                                            placeholder="Main Street Station"
                                        />
                                    </div>
                                    <div className="grid grid-cols-2 gap-3">
                                        <div>
                                            <label className="block text-sm font-medium text-neutral-400 mb-1">
                                                Latitude *
                                            </label>
                                            <input
                                                type="number"
                                                step="0.000001"
                                                value={formData.latitude}
                                                onChange={(e) => setFormData({ ...formData, latitude: parseFloat((e.target as HTMLInputElement).value) || 0 })}
                                                className="w-full px-3 py-2 bg-neutral-950 border border-white/10 text-white rounded-xl focus:outline-none focus:ring-2 focus:ring-accent"
                                                placeholder="40.712776"
                                            />
                                        </div>
                                        <div>
                                            <label className="block text-sm font-medium text-neutral-400 mb-1">
                                                Longitude *
                                            </label>
                                            <input
                                                type="number"
                                                step="0.000001"
                                                value={formData.longitude}
                                                onChange={(e) => setFormData({ ...formData, longitude: parseFloat((e.target as HTMLInputElement).value) || 0 })}
                                                className="w-full px-3 py-2 bg-neutral-950 border border-white/10 text-white rounded-xl focus:outline-none focus:ring-2 focus:ring-accent"
                                                placeholder="-74.005974"
                                            />
                                        </div>
                                    </div>
                                    <div className="flex gap-2">
                                        <button
                                            onClick={createStop}
                                            disabled={!formData.name}
                                            className="flex-1 px-4 py-2 bg-accent text-white font-medium rounded-xl hover:bg-accent-dark transition-all duration-200 disabled:bg-accent/50 shadow-lg"
                                        >
                                            Save Stop
                                        </button>
                                        <button
                                            onClick={() => {
                                                setShowAddForm(false);
                                                setFormData({ name: '', latitude: 0, longitude: 0 });
                                            }}
                                            className="px-4 py-2 bg-white/10 text-white rounded-xl hover:bg-white/20 transition-all duration-200"
                                        >
                                            Cancel
                                        </button>
                                    </div>
                                </div>
                            ) : (
                                <button
                                    onClick={() => setShowAddForm(true)}
                                    className="w-full px-4 py-3 bg-accent text-white font-medium rounded-xl hover:bg-accent-dark transition-all duration-200 flex items-center justify-center gap-2 shadow-lg"
                                >
                                    <Plus className="w-4 h-4" />
                                    Add Stop
                                </button>
                            )}
                        </>
                    )}
                </div>

                <div className="p-6 border-t border-white/10 flex gap-3 justify-end">
                    <button
                        onClick={onClose}
                        className="px-4 py-2 bg-accent text-white font-medium rounded-xl hover:bg-accent-dark transition-all duration-200 shadow-lg"
                    >
                        Done
                    </button>
                </div>
            </div>
        </div>
    );
};

export default RoutesPage;
