import { FunctionComponent } from 'preact';
import { useEffect, useState } from 'preact/hooks';
import DashboardLayout from '../../components/dashboard/DashboardLayout';
import ProtectedRoute from '../../components/ProtectedRoute';
import { Plus, Search, Edit, Trash2, MapPin, Users, Settings } from 'lucide-preact';
import { BusAPI, BusResponse, SeatType, SeatAPI, SeatResponse } from '../../api';

type Bus = {
    id: string;
    plate: string;
    brand: string;
    model: string;
    capacity: number;
    status: 'active' | 'maintenance' | 'inactive';
    amenities: { id: string; name: string }[];
};

const getStatusColor = (status: string) => {
    const lowerStatus = status.toLowerCase();
    switch (lowerStatus) {
        case 'active':
            return 'bg-green-500/20 text-green-300';
        case 'maintenance':
            return 'bg-yellow-500/20 text-yellow-300';
        case 'inactive':
            return 'bg-neutral-600 text-neutral-300';
        default:
            return 'bg-gray-500/20 text-gray-300';
    }
};

export const BusesPage: FunctionComponent = () => {
    const [buses, setBuses] = useState<BusResponse[]>([]);
    const [isLoading, setIsLoading] = useState(true);
    const [error, setError] = useState<string | null>(null);

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

            setBuses([...buses, response.data]);
        } catch (error) {
            console.error('Failed to create bus:', error);
            throw error;
        }
    }

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
                setIsLoading(true);
                const response = await BusAPI.getAll();
                setBuses(response.data);
                setError(null);
            } catch (err) {
                setError('Failed to fetch buses. Please try again later.');
                console.error(err);
            } finally {
                setIsLoading(false);
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
                            <h1 className="text-3xl font-bold text-white">Bus Management</h1>
                            <p className="text-white/80 mt-1">Manage your fleet of buses</p>
                        </div>
                        <button
                            onClick={() => {
                                setSelectedBus(null);
                                setShowModal(true);
                            }}
                            className="flex items-center gap-2 px-4 py-2 bg-accent text-white rounded-xl hover:bg-accent-dark transition-all duration-200 font-medium shadow-lg"
                        >
                            <Plus className="w-4 h-4" />
                            Add New Bus
                        </button>
                    </div>

                    {/* Search and Filters */}
                    <div className="flex gap-4 mb-6">
                        <div className="flex-1 relative">
                            <Search className="absolute left-3 top-1/2 transform -translate-y-1/2 w-5 h-5 text-neutral-400" />
                            <input
                                type="text"
                                placeholder="Search by license plate..."
                                className="w-full pl-10 pr-4 py-2 bg-neutral-950 border border-white/10 text-white placeholder-neutral-500 rounded-xl focus:outline-none focus:ring-2 focus:ring-accent"
                            />
                        </div>
                        <select className="px-4 py-2 bg-neutral-950 border border-white/10 text-white rounded-xl focus:outline-none focus:ring-2 focus:ring-accent">
                            <option value="">All Status</option>
                            <option value="active">Active</option>
                            <option value="maintenance">Maintenance</option>
                            <option value="inactive">Inactive</option>
                        </select>
                    </div>

                    {/* Buses Grid */}
                    <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
                        {buses.map((bus) => (
                            <div key={bus.id} className="bg-white/5 backdrop-blur-xl rounded-2xl shadow-lg border border-white/10 overflow-hidden hover:border-accent transition-all duration-200">
                                <div className="p-6">
                                    <div className="flex items-start justify-between mb-4">
                                        <div>
                                            <h3 className="text-lg font-semibold text-white">{bus.plate}</h3>
                                        </div>
                                        <span className={`px-3 py-1 text-xs font-medium rounded-full ${getStatusColor(bus.status)}`}>
                                            {bus.status.charAt(0).toUpperCase() + bus.status.slice(1).toLowerCase()}
                                        </span>
                                    </div>

                                    <div className="space-y-3 mb-4">
                                        <div className="flex items-center gap-2 text-sm text-neutral-300">
                                            <Users className="w-4 h-4 text-accent" />
                                            <span>{bus.capacity} seats</span>
                                        </div>
                                        <div className="flex flex-wrap gap-2">
                                            {bus.amenities.map((amenity, index) => (
                                                <span key={index} className="px-2 py-1 bg-white/10 text-neutral-300 text-xs rounded-lg">
                                                    {amenity.name}
                                                </span>
                                            ))}
                                        </div>
                                    </div>

                                    <div className="flex gap-2">
                                        <button
                                            onClick={() => handleManageSeats(bus)}
                                            className="flex-1 flex items-center justify-center gap-2 px-3 py-2 bg-accent/10 text-accent rounded-xl hover:bg-accent/20 transition-all duration-200 text-sm font-medium"
                                        >
                                            <Settings className="w-4 h-4" />
                                            Manage Seats
                                        </button>
                                        <button
                                            onClick={() => handleEditBus(bus)}
                                            className="px-3 py-2 bg-white/10 text-neutral-300 rounded-xl hover:bg-white/20 transition-all duration-200"
                                        >
                                            <Edit className="w-4 h-4" />
                                        </button>
                                        <button className="px-3 py-2 bg-accent/20 text-accent rounded-xl hover:bg-accent/30 transition-all duration-200">
                                            <Trash2 className="w-4 h-4" />
                                        </button>
                                    </div>
                                </div>
                            </div>
                        ))}
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
        <div className="fixed inset-0 bg-black/80 backdrop-blur-md flex items-center justify-center z-50">
            <div className="bg-neutral-900/95 backdrop-blur-xl rounded-2xl shadow-2xl p-8 w-full max-w-lg border border-white/10">
                <h2 className="text-2xl font-bold text-white mb-6">
                    {bus ? 'Edit Bus' : 'Create New Bus'}
                </h2>
                <form onSubmit={handleSubmit} className="space-y-6">
                    <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
                        <div>
                            <label htmlFor="plate" className="block text-sm font-medium text-neutral-400 mb-2">
                                License Plate
                            </label>
                            <input
                                type="text"
                                name="plate"
                                id="plate"
                                value={formData.plate}
                                onChange={(e) => setFormData({ ...formData, plate: (e.target as HTMLInputElement).value })}
                                className="w-full px-4 py-2 bg-neutral-950 border border-white/10 text-white rounded-xl focus:outline-none focus:ring-2 focus:ring-accent"
                                required
                            />
                        </div>
                        <div>
                            <label htmlFor="capacity" className="block text-sm font-medium text-neutral-400 mb-2">
                                Capacity
                            </label>
                            <input
                                type="number"
                                name="capacity"
                                id="capacity"
                                value={formData.capacity}
                                onChange={(e) => setFormData({ ...formData, capacity: parseInt((e.target as HTMLInputElement).value) || 0 })}
                                className="w-full px-4 py-2 bg-neutral-950 border border-white/10 text-white rounded-xl focus:outline-none focus:ring-2 focus:ring-accent"
                                required
                            />
                        </div>
                    </div>
                    <div>
                        <label className="block text-sm font-medium text-neutral-400 mb-2">
                            Amenities
                        </label>
                        <div className="grid grid-cols-2 gap-4">
                            {availableAmenities.map(amenity => (
                                <label key={amenity} className="flex items-center gap-3 cursor-pointer">
                                    <input
                                        type="checkbox"
                                        checked={isAmenityChecked(amenity)}
                                        onChange={() => handleAmenityToggle(amenity)}
                                        className="h-5 w-5 rounded border-neutral-600 bg-neutral-800 text-accent focus:ring-accent"
                                    />
                                    <span className="text-neutral-300">{amenity}</span>
                                </label>
                            ))}
                        </div>
                    </div>
                    <div className="flex justify-end gap-4 pt-4">
                        <button
                            type="button"
                            onClick={onClose}
                            className="px-6 py-2 bg-white/10 border border-white/10 text-white rounded-xl hover:bg-white/20 transition-all duration-200"
                        >
                            Cancel
                        </button>
                        <button
                            type="submit"
                            className="px-6 py-2 bg-accent text-white font-medium rounded-xl hover:bg-accent-dark transition-all duration-200 shadow-lg"
                        >
                            {bus ? 'Save Changes' : 'Create Bus'}
                        </button>
                    </div>
                </form>
            </div>
        </div>
    );
};

// Seat Editor Modal Component
const SeatEditorModal = ({ bus, onClose, onSave }: { bus: BusResponse, onClose: () => void, onSave?: (seats: any) => void }) => {
    const [seats, setSeats] = useState(
        Array.from({ length: bus.capacity }, (_, i) => ({
            number: i + 1,
            status: 'available', // available, occupied, locked
        }))
    );

    const toggleSeatLock = (seatNumber) => {
        setSeats(prevSeats =>
            prevSeats.map(seat =>
                seat.number === seatNumber
                    ? { ...seat, status: seat.status === 'locked' ? 'available' : 'locked' }
                    : seat
            )
        );
    };

    return (
        <div className="fixed inset-0 bg-black/80 backdrop-blur-md flex items-center justify-center z-50">
            <div className="bg-neutral-900/95 backdrop-blur-xl rounded-2xl shadow-2xl p-8 w-full max-w-2xl border border-white/10">
                <h2 className="text-2xl font-bold text-white mb-2">Manage Seats for {bus.plate}</h2>
                <p className="text-neutral-400 mb-6">Click on a seat to lock or unlock it for booking.</p>

                <div className="grid grid-cols-5 gap-3 p-6 bg-neutral-950 rounded-xl border border-white/10">
                    {seats.map(seat => (
                        <button
                            key={seat.number}
                            onClick={() => toggleSeatLock(seat.number)}
                            className={`w-12 h-12 flex items-center justify-center rounded-md font-semibold text-sm transition-colors
                                ${seat.status === 'available' && 'bg-green-500/20 text-green-300 hover:bg-green-500/40'}
                                ${seat.status === 'occupied' && 'bg-neutral-700 text-neutral-400 cursor-not-allowed'}
                                ${seat.status === 'locked' && 'bg-red-500/30 text-red-300 hover:bg-red-500/50'}
                            `}
                        >
                            {seat.number}
                        </button>
                    ))}
                </div>

                <div className="flex justify-end gap-4 mt-8">
                    <button
                        type="button"
                        onClick={onClose}
                        className="px-6 py-2 bg-white/10 border border-white/10 text-white rounded-xl hover:bg-white/20 transition-all duration-200"
                    >
                        Cancel
                    </button>
                    <button
                        type="button"
                        onClick={() => onSave?.(seats)}
                        className="px-6 py-2 bg-accent text-white font-medium rounded-xl hover:bg-accent-dark transition-all duration-200 shadow-lg"
                    >
                        Save Seat Layout
                    </button>
                </div>
            </div>
        </div>
    );
};

const Buses = () => {
    return (
        <div>
            {/* Existing content for Buses page */}
        </div>
    );
};

export default BusesPage;
