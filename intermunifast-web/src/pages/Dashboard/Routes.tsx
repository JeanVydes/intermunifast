import { FunctionComponent } from 'preact';
import { useState } from 'preact/hooks';
import DashboardLayout from '../../components/dashboard/DashboardLayout';
import { Plus, Search, Edit, Trash2, MapPin, Clock, DollarSign } from 'lucide-preact';

interface Stop {
    id: number;
    name: string;
    city: string;
    arrivalTime?: string;
    departureTime?: string;
    order: number;
}

interface Route {
    id: number;
    name: string;
    origin: string;
    destination: string;
    distance: number;
    duration: string;
    basePrice: number;
    stops: Stop[];
    status: 'active' | 'inactive';
}

export const RoutesPage: FunctionComponent = () => {
    const [showModal, setShowModal] = useState(false);
    const [selectedRoute, setSelectedRoute] = useState<Route | null>(null);
    const [showStopsEditor, setShowStopsEditor] = useState(false);

    // Mock data
    const routes: Route[] = [
        {
            id: 1,
            name: 'Guatemala City - Quetzaltenango',
            origin: 'Guatemala City',
            destination: 'Quetzaltenango',
            distance: 206,
            duration: '4h 30m',
            basePrice: 75,
            status: 'active',
            stops: [
                { id: 1, name: 'Terminal Central', city: 'Guatemala City', departureTime: '08:00', order: 1 },
                { id: 2, name: 'Los Encuentros', city: 'Sololá', arrivalTime: '10:30', departureTime: '10:45', order: 2 },
                { id: 3, name: 'Terminal Minerva', city: 'Quetzaltenango', arrivalTime: '12:30', order: 3 },
            ]
        },
        {
            id: 2,
            name: 'Antigua - Panajachel',
            origin: 'Antigua Guatemala',
            destination: 'Panajachel',
            distance: 95,
            duration: '2h 15m',
            basePrice: 45,
            status: 'active',
            stops: [
                { id: 4, name: 'Parque Central', city: 'Antigua', departureTime: '09:00', order: 1 },
                { id: 5, name: 'Chimaltenango Terminal', city: 'Chimaltenango', arrivalTime: '09:45', departureTime: '10:00', order: 2 },
                { id: 6, name: 'Panajachel Centro', city: 'Panajachel', arrivalTime: '11:15', order: 3 },
            ]
        },
    ];

    const handleEditRoute = (route: Route) => {
        setSelectedRoute(route);
        setShowModal(true);
    };

    const handleManageStops = (route: Route) => {
        setSelectedRoute(route);
        setShowStopsEditor(true);
    };

    return (
        <DashboardLayout>
            <div className="p-8">
                {/* Header */}
                <div className="flex items-center justify-between mb-8">
                    <div>
                        <h1 className="text-3xl font-bold text-gray-900">Route Management</h1>
                        <p className="text-gray-600 mt-1">Manage bus routes and stops</p>
                    </div>
                    <button
                        onClick={() => {
                            setSelectedRoute(null);
                            setShowModal(true);
                        }}
                        className="flex items-center gap-2 px-4 py-2 bg-purple-600 text-white rounded-lg hover:bg-purple-700 transition-colors"
                    >
                        <Plus className="w-4 h-4" />
                        Add New Route
                    </button>
                </div>

                {/* Search */}
                <div className="flex gap-4 mb-6">
                    <div className="flex-1 relative">
                        <Search className="absolute left-3 top-1/2 transform -translate-y-1/2 w-5 h-5 text-gray-400" />
                        <input
                            type="text"
                            placeholder="Search routes..."
                            className="w-full pl-10 pr-4 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-purple-500"
                        />
                    </div>
                </div>

                {/* Routes List */}
                <div className="space-y-4">
                    {routes.map((route) => (
                        <div key={route.id} className="bg-white rounded-xl shadow-sm border border-gray-200 overflow-hidden hover:shadow-md transition-shadow">
                            <div className="p-6">
                                <div className="flex items-start justify-between mb-4">
                                    <div className="flex-1">
                                        <div className="flex items-center gap-3 mb-2">
                                            <h3 className="text-xl font-semibold text-gray-900">{route.name}</h3>
                                            <span className={`px-3 py-1 text-xs font-medium rounded-full ${route.status === 'active' ? 'bg-green-100 text-green-700' : 'bg-gray-100 text-gray-700'
                                                }`}>
                                                {route.status.charAt(0).toUpperCase() + route.status.slice(1)}
                                            </span>
                                        </div>
                                        <p className="text-gray-600">{route.origin} → {route.destination}</p>
                                    </div>
                                </div>

                                <div className="grid grid-cols-3 gap-6 mb-4">
                                    <div className="flex items-center gap-2 text-gray-600">
                                        <MapPin className="w-4 h-4" />
                                        <div>
                                            <p className="text-xs text-gray-500">Distance</p>
                                            <p className="font-semibold text-gray-900">{route.distance} km</p>
                                        </div>
                                    </div>
                                    <div className="flex items-center gap-2 text-gray-600">
                                        <Clock className="w-4 h-4" />
                                        <div>
                                            <p className="text-xs text-gray-500">Duration</p>
                                            <p className="font-semibold text-gray-900">{route.duration}</p>
                                        </div>
                                    </div>
                                    <div className="flex items-center gap-2 text-gray-600">
                                        <DollarSign className="w-4 h-4" />
                                        <div>
                                            <p className="text-xs text-gray-500">Base Price</p>
                                            <p className="font-semibold text-gray-900">${route.basePrice}</p>
                                        </div>
                                    </div>
                                </div>

                                {/* Stops Preview */}
                                <div className="mb-4">
                                    <p className="text-sm font-medium text-gray-700 mb-2">Stops ({route.stops.length})</p>
                                    <div className="flex items-center gap-2 overflow-x-auto pb-2">
                                        {route.stops.map((stop, index) => (
                                            <div key={stop.id} className="flex items-center gap-2">
                                                <div className="flex items-center gap-2 px-3 py-2 bg-purple-50 rounded-lg whitespace-nowrap">
                                                    <span className="w-6 h-6 bg-purple-600 text-white rounded-full flex items-center justify-center text-xs font-bold">
                                                        {index + 1}
                                                    </span>
                                                    <span className="text-sm font-medium text-gray-900">{stop.name}</span>
                                                </div>
                                                {index < route.stops.length - 1 && (
                                                    <svg className="w-4 h-4 text-gray-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                                                        <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M9 5l7 7-7 7" />
                                                    </svg>
                                                )}
                                            </div>
                                        ))}
                                    </div>
                                </div>

                                <div className="flex gap-2">
                                    <button
                                        onClick={() => handleManageStops(route)}
                                        className="flex-1 flex items-center justify-center gap-2 px-3 py-2 bg-purple-50 text-purple-700 rounded-lg hover:bg-purple-100 transition-colors text-sm font-medium"
                                    >
                                        <MapPin className="w-4 h-4" />
                                        Manage Stops
                                    </button>
                                    <button
                                        onClick={() => handleEditRoute(route)}
                                        className="px-3 py-2 bg-gray-100 text-gray-700 rounded-lg hover:bg-gray-200 transition-colors"
                                    >
                                        <Edit className="w-4 h-4" />
                                    </button>
                                    <button className="px-3 py-2 bg-red-50 text-red-600 rounded-lg hover:bg-red-100 transition-colors">
                                        <Trash2 className="w-4 h-4" />
                                    </button>
                                </div>
                            </div>
                        </div>
                    ))}
                </div>
            </div>

            {/* Route Form Modal */}
            {showModal && (
                <RouteFormModal
                    route={selectedRoute}
                    onClose={() => {
                        setShowModal(false);
                        setSelectedRoute(null);
                    }}
                />
            )}

            {/* Stops Editor Modal */}
            {showStopsEditor && selectedRoute && (
                <StopsEditorModal
                    route={selectedRoute}
                    onClose={() => {
                        setShowStopsEditor(false);
                        setSelectedRoute(null);
                    }}
                />
            )}
        </DashboardLayout>
    );
};

// Route Form Modal
const RouteFormModal: FunctionComponent<{ route: Route | null; onClose: () => void }> = ({ route, onClose }) => {
    return (
        <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50 p-4">
            <div className="bg-white rounded-xl max-w-2xl w-full max-h-[90vh] overflow-y-auto">
                <div className="p-6 border-b border-gray-200">
                    <h2 className="text-2xl font-bold text-gray-900">
                        {route ? 'Edit Route' : 'Add New Route'}
                    </h2>
                </div>

                <div className="p-6">
                    <form className="space-y-4">
                        <div>
                            <label className="block text-sm font-medium text-gray-700 mb-1">
                                Route Name
                            </label>
                            <input
                                type="text"
                                defaultValue={route?.name}
                                className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-purple-500"
                                placeholder="Guatemala City - Quetzaltenango"
                            />
                        </div>

                        <div className="grid grid-cols-2 gap-4">
                            <div>
                                <label className="block text-sm font-medium text-gray-700 mb-1">
                                    Origin
                                </label>
                                <input
                                    type="text"
                                    defaultValue={route?.origin}
                                    className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-purple-500"
                                    placeholder="Guatemala City"
                                />
                            </div>
                            <div>
                                <label className="block text-sm font-medium text-gray-700 mb-1">
                                    Destination
                                </label>
                                <input
                                    type="text"
                                    defaultValue={route?.destination}
                                    className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-purple-500"
                                    placeholder="Quetzaltenango"
                                />
                            </div>
                        </div>

                        <div className="grid grid-cols-3 gap-4">
                            <div>
                                <label className="block text-sm font-medium text-gray-700 mb-1">
                                    Distance (km)
                                </label>
                                <input
                                    type="number"
                                    defaultValue={route?.distance}
                                    className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-purple-500"
                                    placeholder="206"
                                />
                            </div>
                            <div>
                                <label className="block text-sm font-medium text-gray-700 mb-1">
                                    Duration
                                </label>
                                <input
                                    type="text"
                                    defaultValue={route?.duration}
                                    className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-purple-500"
                                    placeholder="4h 30m"
                                />
                            </div>
                            <div>
                                <label className="block text-sm font-medium text-gray-700 mb-1">
                                    Base Price ($)
                                </label>
                                <input
                                    type="number"
                                    defaultValue={route?.basePrice}
                                    className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-purple-500"
                                    placeholder="75"
                                />
                            </div>
                        </div>

                        <div>
                            <label className="block text-sm font-medium text-gray-700 mb-1">
                                Status
                            </label>
                            <select
                                defaultValue={route?.status}
                                className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-purple-500"
                            >
                                <option value="active">Active</option>
                                <option value="inactive">Inactive</option>
                            </select>
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
                        {route ? 'Save Changes' : 'Create Route'}
                    </button>
                </div>
            </div>
        </div>
    );
};

// Stops Editor Modal
const StopsEditorModal: FunctionComponent<{ route: Route; onClose: () => void }> = ({ route, onClose }) => {
    const [stops, setStops] = useState<Stop[]>(route.stops);

    const addStop = () => {
        setStops([...stops, {
            id: Date.now(),
            name: '',
            city: '',
            order: stops.length + 1
        }]);
    };

    const removeStop = (id: number) => {
        setStops(stops.filter(stop => stop.id !== id));
    };

    return (
        <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50 p-4">
            <div className="bg-white rounded-xl max-w-4xl w-full max-h-[90vh] overflow-y-auto">
                <div className="p-6 border-b border-gray-200">
                    <h2 className="text-2xl font-bold text-gray-900">
                        Manage Stops - {route.name}
                    </h2>
                    <p className="text-gray-600 mt-1">Add and configure stops for this route</p>
                </div>

                <div className="p-6">
                    <div className="space-y-4">
                        {stops.map((stop, index) => (
                            <div key={stop.id} className="border border-gray-200 rounded-lg p-4">
                                <div className="flex items-center gap-4 mb-4">
                                    <div className="w-8 h-8 bg-purple-600 text-white rounded-full flex items-center justify-center text-sm font-bold flex-shrink-0">
                                        {index + 1}
                                    </div>
                                    <div className="flex-1 grid grid-cols-2 gap-4">
                                        <input
                                            type="text"
                                            value={stop.name}
                                            onChange={(e) => {
                                                const newStops = [...stops];
                                                newStops[index].name = (e.target as HTMLInputElement).value;
                                                setStops(newStops);
                                            }}
                                            className="px-3 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-purple-500"
                                            placeholder="Stop name"
                                        />
                                        <input
                                            type="text"
                                            value={stop.city}
                                            onChange={(e) => {
                                                const newStops = [...stops];
                                                newStops[index].city = (e.target as HTMLInputElement).value;
                                                setStops(newStops);
                                            }}
                                            className="px-3 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-purple-500"
                                            placeholder="City"
                                        />
                                    </div>
                                    {stops.length > 2 && (
                                        <button
                                            onClick={() => removeStop(stop.id)}
                                            className="p-2 text-red-600 hover:bg-red-50 rounded-lg transition-colors"
                                        >
                                            <Trash2 className="w-4 h-4" />
                                        </button>
                                    )}
                                </div>
                                <div className="grid grid-cols-2 gap-4 ml-12">
                                    <div>
                                        <label className="block text-xs text-gray-500 mb-1">Arrival Time</label>
                                        <input
                                            type="time"
                                            value={stop.arrivalTime}
                                            onChange={(e) => {
                                                const newStops = [...stops];
                                                newStops[index].arrivalTime = (e.target as HTMLInputElement).value;
                                                setStops(newStops);
                                            }}
                                            className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-purple-500 text-sm"
                                            disabled={index === 0}
                                        />
                                    </div>
                                    <div>
                                        <label className="block text-xs text-gray-500 mb-1">Departure Time</label>
                                        <input
                                            type="time"
                                            value={stop.departureTime}
                                            onChange={(e) => {
                                                const newStops = [...stops];
                                                newStops[index].departureTime = (e.target as HTMLInputElement).value;
                                                setStops(newStops);
                                            }}
                                            className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-purple-500 text-sm"
                                            disabled={index === stops.length - 1}
                                        />
                                    </div>
                                </div>
                            </div>
                        ))}
                    </div>

                    <button
                        onClick={addStop}
                        className="mt-4 w-full flex items-center justify-center gap-2 px-4 py-3 border-2 border-dashed border-gray-300 text-gray-600 rounded-lg hover:border-purple-400 hover:text-purple-600 hover:bg-purple-50 transition-all"
                    >
                        <Plus className="w-4 h-4" />
                        Add Stop
                    </button>
                </div>

                <div className="p-6 border-t border-gray-200 flex gap-3 justify-end">
                    <button
                        onClick={onClose}
                        className="px-4 py-2 border border-gray-300 text-gray-700 rounded-lg hover:bg-gray-50 transition-colors"
                    >
                        Cancel
                    </button>
                    <button className="px-4 py-2 bg-purple-600 text-white rounded-lg hover:bg-purple-700 transition-colors">
                        Save Stops
                    </button>
                </div>
            </div>
        </div>
    );
};

export default RoutesPage;
