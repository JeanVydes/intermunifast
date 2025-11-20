import { FunctionComponent } from 'preact';
import { useEffect, useState } from 'preact/hooks';
import DashboardLayout from '../../components/dashboard/DashboardLayout';
import ProtectedRoute from '../../components/ProtectedRoute';
import { Plus, Search, Edit, Trash2, MapPin, Users, Settings } from 'lucide-preact';
import { BusAPI, BusResponse, SeatType, SeatAPI, SeatResponse } from '../../api';

const getStatusColor = (status: string) => {
    switch (status) {
        case 'active':
            return 'bg-green-100 text-green-700';
        case 'maintenance':
            return 'bg-yellow-100 text-yellow-700';
        case 'inactive':
            return 'bg-gray-100 text-gray-700';
        default:
            return 'bg-gray-100 text-gray-700';
    }
};

export const BusesPage: FunctionComponent = () => {
    const [buses, setBuses] = useState<BusResponse[]>([]);
    const [filteredBuses, setFilteredBuses] = useState<BusResponse[]>([]);
    const [searchTerm, setSearchTerm] = useState('');

    // Flow control states
    const [showModal, setShowModal] = useState(false);
    const [selectedBus, setSelectedBus] = useState<BusResponse | null>(null);
    const [showSeatEditor, setShowSeatEditor] = useState(false);

    // Bus creation form
    const [busPlate, setBusPlate] = useState<string | null>(null);
    const [busCapacity, setBusCapacity] = useState<number | null>(null);
    const [amenities, setAmenities] = useState<string[]>([]);

    // Seat creation form
    const [busId, setBusId] = useState<number | null>(null);
    const [seatNumber, setSeatNumber] = useState<string | null>(null);
    const [seatType, setSeatType] = useState<SeatType | null>(null);

    async function createBus(busData: any) {
        try {
            let response = await BusAPI.create({
                plate: busData.plate,
                capacity: busData.capacity,
                amenities: busData.amenities
            });

            const newBus = response.data;
            setBuses([...buses, newBus]);
            setFilteredBuses([...filteredBuses, newBus]);
        } catch (error) {
            console.error('Failed to create bus:', error);
            throw error;
        }
    }

    // Filter buses based on search term
    useEffect(() => {
        if (!searchTerm.trim()) {
            setFilteredBuses(buses);
            return;
        }

        const term = searchTerm.toLowerCase().trim();
        const filtered = buses.filter(bus => {
            const plateMatch = bus.plate.toLowerCase().includes(term);
            const idMatch = bus.id.toString().includes(term);
            const capacityMatch = bus.capacity.toString().includes(term);
            const amenitiesMatch = bus.amenities.some(a =>
                a.name.toLowerCase().includes(term) ||
                a.description?.toLowerCase().includes(term)
            );

            return plateMatch || idMatch || capacityMatch || amenitiesMatch;
        });

        setFilteredBuses(filtered);
    }, [searchTerm, buses]);

    const handleEditBus = (bus: BusResponse) => {
        setSelectedBus(bus);
        setShowModal(true);
    };

    const handleManageSeats = (bus: BusResponse) => {
        setSelectedBus(bus);
        setShowSeatEditor(true);
    };

    useEffect(() => {
        const fetchBuses = async () => {
            try {
                const response = await BusAPI.getAll();
                setBuses(response.data);
                setFilteredBuses(response.data);
            } catch (error) {
                console.error('Failed to fetch buses:', error);
            }
        };

        fetchBuses();
    }, []);

    return (
        <ProtectedRoute allowedRoles={['ADMIN']}>
            <DashboardLayout>
                <div className="p-8">
                    {/* Header */}
                    <div className="flex items-center justify-between mb-8">
                        <div>
                            <h1 className="text-3xl font-bold text-gray-900">Bus Management</h1>
                            <p className="text-gray-600 mt-1">Manage your fleet of buses</p>
                        </div>
                        <button
                            onClick={() => {
                                setSelectedBus(null);
                                setShowModal(true);
                            }}
                            className="flex items-center gap-2 px-4 py-2 bg-purple-600 text-white rounded-lg hover:bg-purple-700 transition-colors"
                        >
                            <Plus className="w-4 h-4" />
                            Add New Bus
                        </button>
                    </div>

                    {/* Search and Filters */}
                    <div className="flex gap-4 mb-6">
                        <div className="flex-1 relative">
                            <Search className="absolute left-3 top-1/2 transform -translate-y-1/2 w-5 h-5 text-gray-400" />
                            <input
                                type="text"
                                placeholder="Search by license plate, capacity, or amenities..."
                                value={searchTerm}
                                onInput={(e) => setSearchTerm((e.target as HTMLInputElement).value)}
                                className="w-full pl-10 pr-4 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-purple-500"
                            />
                        </div>
                    </div>

                    {/* Buses Grid */}
                    <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
                        {filteredBuses.map((bus) => (
                            <div key={bus.id} className="bg-white rounded-xl shadow-sm border border-gray-200 overflow-hidden hover:shadow-md transition-shadow">
                                <div className="p-6">
                                    <div className="flex items-start justify-between mb-4">
                                        <div>
                                            <h3 className="text-lg font-semibold text-gray-900">{bus.plate}</h3>
                                            <p className="text-sm text-gray-500 mt-1">{bus.id}</p>
                                        </div>
                                        <span className={`px-3 py-1 text-xs font-medium rounded-full ${getStatusColor(bus.status)}`}>
                                            {bus.status.charAt(0).toUpperCase() + bus.status.slice(1)}
                                        </span>
                                    </div>

                                    <div className="space-y-3 mb-4">
                                        <div className="flex items-center gap-2 text-sm text-gray-600">
                                            <Users className="w-4 h-4" />
                                            <span>{bus.capacity} seats</span>
                                        </div>
                                        <div className="flex flex-wrap gap-2">
                                            {bus.amenities.map((amenity, index) => (
                                                <span key={index} className="px-2 py-1 bg-gray-100 text-gray-700 text-xs rounded-md">
                                                    {amenity.name}
                                                </span>
                                            ))}
                                        </div>
                                    </div>

                                    <div className="flex gap-2">
                                        <button
                                            onClick={() => handleManageSeats(bus)}
                                            className="flex-1 flex items-center justify-center gap-2 px-3 py-2 bg-purple-50 text-purple-700 rounded-lg hover:bg-purple-100 transition-colors text-sm font-medium"
                                        >
                                            <MapPin className="w-4 h-4" />
                                            Manage Seats
                                        </button>
                                        <button
                                            onClick={() => handleEditBus(bus)}
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

                        {filteredBuses.length === 0 && buses.length > 0 && (
                            <div className="col-span-full text-center py-12">
                                <Search className="w-16 h-16 text-gray-400 mx-auto mb-4" />
                                <p className="text-gray-600">No buses match your search</p>
                                <p className="text-sm text-gray-500 mt-1">Try a different search term</p>
                            </div>
                        )}

                        {buses.length === 0 && (
                            <div className="col-span-full text-center py-12">
                                <Users className="w-16 h-16 text-gray-400 mx-auto mb-4" />
                                <p className="text-gray-600">No buses yet</p>
                                <p className="text-sm text-gray-500 mt-1">Click "Add New Bus" to create your first bus</p>
                            </div>
                        )}
                    </div>

                    {/* Modals */}
                    {showModal && (
                        <BusFormModal
                            bus={selectedBus}
                            onClose={() => {
                                setShowModal(false);
                                setSelectedBus(null);
                            }}
                            onSave={async (busData) => {
                                if (selectedBus) {
                                    // Update existing bus
                                    const response = await BusAPI.update({
                                        plate: busData.plate,
                                        capacity: busData.capacity,
                                        amenities: busData.amenities,
                                        status: busData.status
                                    }, {
                                        pathParams: { id: selectedBus.id }
                                    });
                                    setBuses(buses.map(b => b.id === selectedBus.id ? response.data : b));
                                } else {
                                    // Create new bus
                                    await createBus(busData);
                                }
                                setShowModal(false);
                                setSelectedBus(null);
                            }}
                        />
                    )}

                    {showSeatEditor && selectedBus && (
                        <SeatEditorModal
                            bus={selectedBus}
                            onClose={() => {
                                setShowSeatEditor(false);
                                setSelectedBus(null);
                            }}
                        />
                    )}
                </div>
            </DashboardLayout>
        </ProtectedRoute>
    );
};

// Bus Form Modal Component
const BusFormModal: FunctionComponent<{
    bus: BusResponse | null;
    onClose: () => void;
    onSave: (busData: any) => Promise<void>;
}> = ({ bus, onClose, onSave }) => {
    const [formData, setFormData] = useState({
        plate: bus?.plate || '',
        capacity: bus?.capacity || 20,
        amenities: bus?.amenities?.map(a => a.name) || [] as string[],
        status: bus?.status || 'ACTIVE'
    });
    const [loading, setLoading] = useState(false);

    const availableAmenities = ['WiFi', 'AC', 'USB Charging', 'Restroom', 'Entertainment', 'Reclining Seats'];

    const handleAmenityToggle = (amenityName: string) => {
        if (formData.amenities.includes(amenityName)) {
            setFormData({
                ...formData,
                amenities: formData.amenities.filter(a => a !== amenityName)
            });
        } else {
            setFormData({
                ...formData,
                amenities: [...formData.amenities, amenityName]
            });
        }
    };

    const isAmenityChecked = (amenityName: string) => {
        return formData.amenities.includes(amenityName);
    };

    const handleSubmit = async (e: Event) => {
        e.preventDefault();
        setLoading(true);

        try {
            // Convert amenities to the correct format for API
            const busData = {
                ...formData,
                amenities: formData.amenities.map(name => ({ name }))
            };
            await onSave(busData);
            onClose();
        } catch (error) {
            console.error('Failed to save bus:', error);
        } finally {
            setLoading(false);
        }
    };

    return (
        <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50 p-4">
            <div className="bg-white rounded-xl max-w-2xl w-full max-h-[90vh] overflow-y-auto">
                <div className="p-6 border-b border-gray-200">
                    <h2 className="text-2xl font-bold text-gray-900">
                        {bus ? 'Edit Bus' : 'Add New Bus'}
                    </h2>
                </div>

                <div className="p-6">
                    <form onSubmit={handleSubmit} className="space-y-4">
                        <div className="grid grid-cols-2 gap-4">
                            <div>
                                <label className="block text-sm font-medium text-gray-700 mb-1">
                                    License Plate *
                                </label>
                                <input
                                    type="text"
                                    value={formData.plate}
                                    onChange={(e) => setFormData({ ...formData, plate: (e.target as HTMLInputElement).value })}
                                    className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-purple-500"
                                    placeholder="ABC-123"
                                    required
                                />
                            </div>
                            <div>
                                <label className="block text-sm font-medium text-gray-700 mb-1">
                                    Status
                                </label>
                                <select
                                    value={formData.status}
                                    onChange={(e) => setFormData({ ...formData, status: (e.target as HTMLSelectElement).value })}
                                    className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-purple-500"
                                >
                                    <option value="ACTIVE">Active</option>
                                    <option value="MAINTENANCE">Maintenance</option>
                                    <option value="INACTIVE">Inactive</option>
                                </select>
                            </div>
                        </div>

                        <div>
                            <label className="block text-sm font-medium text-gray-700 mb-1">
                                Total Seats *
                            </label>
                            <input
                                type="number"
                                value={formData.capacity}
                                onChange={(e) => setFormData({ ...formData, capacity: parseInt((e.target as HTMLInputElement).value) || 0 })}
                                className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-purple-500"
                                placeholder="20"
                                min="1"
                                required
                            />
                        </div>

                        <div>
                            <label className="block text-sm font-medium text-gray-700 mb-2">
                                Amenities
                            </label>
                            <div className="grid grid-cols-2 gap-2">
                                {availableAmenities.map((amenity) => (
                                    <label key={amenity} className="flex items-center gap-2">
                                        <input
                                            type="checkbox"
                                            checked={isAmenityChecked(amenity)}
                                            onChange={() => handleAmenityToggle(amenity)}
                                            className="w-4 h-4 text-purple-600 border-gray-300 rounded focus:ring-purple-500"
                                        />
                                        <span className="text-sm text-gray-700">{amenity}</span>
                                    </label>
                                ))}
                            </div>
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
                                {loading ? 'Saving...' : (bus ? 'Save Changes' : 'Create Bus')}
                            </button>
                        </div>
                    </form>
                </div>
            </div>
        </div>
    );
};

// Seat Editor Modal Component
const SeatEditorModal: FunctionComponent<{ bus: BusResponse; onClose: () => void }> = ({ bus, onClose }) => {
    const [seats, setSeats] = useState<SeatResponse[]>([]);
    const [loading, setLoading] = useState(true);
    const [creating, setCreating] = useState(false);

    useEffect(() => {
        fetchSeats();
    }, [bus.id]);

    const fetchSeats = async () => {
        try {
            setLoading(true);
            const response = await SeatAPI.getByBusId(undefined, {
                pathParams: { busId: bus.id }
            });
            setSeats(response.data);
        } catch (error) {
            console.error('Failed to fetch seats:', error);
            setSeats([]);
        } finally {
            setLoading(false);
        }
    };

    const createSeat = async (seatNumber: string, seatType: SeatType) => {
        try {
            setCreating(true);
            const response = await SeatAPI.create({
                number: seatNumber,
                type: seatType,
                busId: bus.id
            });
            setSeats([...seats, response.data]);
        } catch (error) {
            console.error('Failed to create seat:', error);
        } finally {
            setCreating(false);
        }
    };

    const deleteSeat = async (seatId: number) => {
        try {
            await SeatAPI.delete(undefined, {
                pathParams: { id: seatId }
            });
            setSeats(seats.filter(s => s.id !== seatId));
        } catch (error) {
            console.error('Failed to delete seat:', error);
        }
    };

    const toggleSeatType = async (seat: SeatResponse) => {
        const newType: SeatType = seat.type === 'STANDARD' ? 'PREFERENTIAL' : 'STANDARD';
        try {
            const response = await SeatAPI.update({
                type: newType
            }, {
                pathParams: { id: seat.id }
            });
            setSeats(seats.map(s => s.id === seat.id ? response.data : s));
        } catch (error) {
            console.error('Failed to update seat:', error);
        }
    };

    const getSeatColor = (seat: SeatResponse) => {
        if (seat.type === 'PREFERENTIAL') return 'bg-purple-500 text-white hover:bg-purple-600';
        return 'bg-green-500 text-white hover:bg-green-600';
    };

    const handleAddSeat = async () => {
        const nextNumber = (seats.length + 1).toString();
        await createSeat(nextNumber, 'STANDARD');
    };

    // Group seats into rows (4 seats per row for horizontal bus layout)
    const rows: SeatResponse[][] = [];
    for (let i = 0; i < seats.length; i += 4) {
        rows.push(seats.slice(i, i + 4));
    }

    return (
        <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50 p-4">
            <div className="bg-white rounded-xl max-w-4xl w-full max-h-[90vh] overflow-y-auto">
                <div className="p-6 border-b border-gray-200">
                    <h2 className="text-2xl font-bold text-gray-900">
                        Seat Configuration - Bus {bus.plate}
                    </h2>
                    <p className="text-gray-600 mt-1">
                        Capacity: {bus.capacity} | Current Seats: {seats.length}
                    </p>
                </div>

                <div className="p-6">
                    {loading ? (
                        <div className="text-center py-12">
                            <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-purple-600 mx-auto"></div>
                            <p className="text-gray-600 mt-4">Loading seats...</p>
                        </div>
                    ) : (
                        <>
                            {/* Legend */}
                            <div className="flex gap-4 mb-6 p-4 bg-gray-50 rounded-lg">
                                <div className="flex items-center gap-2">
                                    <div className="w-8 h-8 bg-green-500 rounded"></div>
                                    <span className="text-sm text-gray-700">Standard</span>
                                </div>
                                <div className="flex items-center gap-2">
                                    <div className="w-8 h-8 bg-purple-500 rounded"></div>
                                    <span className="text-sm text-gray-700">Preferential</span>
                                </div>
                            </div>

                            {/* Bus Layout - Horizontal rows */}
                            <div className="bg-gray-100 p-8 rounded-lg mb-6">
                                <div className="max-w-2xl mx-auto space-y-3">
                                    {rows.map((row, rowIndex) => (
                                        <div key={rowIndex} className="flex gap-3 justify-center">
                                            {row.map((seat) => (
                                                <div key={seat.id} className="relative group">
                                                    <button
                                                        onClick={() => toggleSeatType(seat)}
                                                        className={`w-16 h-16 rounded-lg font-semibold text-sm transition-all ${getSeatColor(seat)} shadow-md`}
                                                    >
                                                        {seat.number}
                                                    </button>
                                                    <button
                                                        onClick={() => deleteSeat(seat.id)}
                                                        className="absolute -top-2 -right-2 bg-red-500 text-white rounded-full w-6 h-6 flex items-center justify-center opacity-0 group-hover:opacity-100 transition-opacity text-xs"
                                                    >
                                                        ×
                                                    </button>
                                                </div>
                                            ))}
                                        </div>
                                    ))}
                                </div>
                            </div>

                            <div className="text-center">
                                <button
                                    onClick={handleAddSeat}
                                    disabled={creating || seats.length >= bus.capacity}
                                    className="px-6 py-3 bg-purple-600 text-white rounded-lg hover:bg-purple-700 transition-colors disabled:bg-gray-400 disabled:cursor-not-allowed"
                                >
                                    {creating ? 'Adding...' : `Add Seat (${seats.length}/${bus.capacity})`}
                                </button>
                            </div>

                            <p className="text-sm text-gray-500 text-center mt-4">
                                Click on a seat to toggle between Standard and Preferential. Hover to delete.
                            </p>
                        </>
                    )}
                </div>

                <div className="p-6 border-t border-gray-200 flex gap-3 justify-end">
                    <button
                        onClick={onClose}
                        className="px-4 py-2 bg-purple-600 text-white rounded-lg hover:bg-purple-700 transition-colors"
                    >
                        Done
                    </button>
                </div>
            </div>
        </div>
    );
};

export default BusesPage;
