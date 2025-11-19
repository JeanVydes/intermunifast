import { useEffect, useState } from "preact/hooks";
import useAccountStore from "../../stores/AccountStore";
import useAuthStore from "../../stores/AuthStore";
import { TicketAPI, TicketResponse, AccountAPI, TripAPI, RouteAPI, StopAPI } from "../../api";
import { FunctionComponent } from "preact";
import { UpdateAccountRequest } from "../../api/types/Account";

export const Account: FunctionComponent = () => {
    const { account, accountId, setAccount } = useAccountStore();
    const { token } = useAuthStore();
    const [tickets, setTickets] = useState<TicketResponse[]>([]);
    const [loading, setLoading] = useState(true);
    const [activeTab, setActiveTab] = useState<'tickets' | 'profile'>('tickets');
    const [editMode, setEditMode] = useState(false);
    const [formData, setFormData] = useState({
        name: account?.name || '',
        email: account?.email || '',
        phone: account?.phone || '',
        password: ''
    });

    // Redirect if not authenticated
    useEffect(() => {
        if (!token) {
            const currentPath = window.location.pathname;
            const encodedPath = encodeURIComponent(currentPath);
            location.assign(`/auth/signin?redirect=${encodedPath}`);
        }
    }, [token]);

    useEffect(() => {
        async function fetchTickets() {
            if (!accountId) {
                setLoading(false);
                return;
            }

            try {
                const response = await TicketAPI.search(null as never, {
                    queryParams: { accountId: accountId.toString() }
                });
                if (response?.data) {
                    setTickets(response.data);
                }
            } catch (error) {
                console.error("Failed to fetch tickets:", error);
            } finally {
                setLoading(false);
            }
        }

        fetchTickets();
    }, [accountId]);

    useEffect(() => {
        if (account) {
            setFormData({
                name: account.name,
                email: account.email,
                phone: account.phone,
                password: ''
            });
        }
    }, [account]);

    const handleCancelTicket = async (ticketId: number) => {
        if (!confirm('¿Estás seguro de que deseas cancelar este ticket?')) {
            return;
        }

        try {
            await TicketAPI.delete(undefined, { pathParams: { id: ticketId } });
            alert('Ticket cancelado exitosamente');
            // Refresh tickets
            const response = await TicketAPI.search(null as never, {
                queryParams: { accountId: accountId!.toString() }
            });
            if (response?.data) {
                setTickets(response.data);
            }
        } catch (error) {
            console.error('Error canceling ticket:', error);
            alert('Error al cancelar el ticket');
        }
    };

    const handleRequestRefund = (ticketId: number) => {
        // Mockup functionality
        alert(`Solicitud de reembolso para ticket #${ticketId} enviada. Nuestro equipo de soporte se pondrá en contacto contigo pronto.`);
    };

    const handleUpdateProfile = async (e: Event) => {
        e.preventDefault();

        try {
            const updateData: UpdateAccountRequest = {
                name: formData.name,
                email: formData.email,
                phone: formData.phone,
            };

            // Only include password if it was changed
            if (formData.password) {
                updateData.password = formData.password;
            }

            const response = await AccountAPI.update(updateData, {
                pathParams: { id: accountId! }
            });

            if (response?.data) {
                setAccount(response.data);
                alert('Perfil actualizado exitosamente');
                setEditMode(false);
                setFormData({ ...formData, password: '' });
            }
        } catch (error) {
            console.error('Error updating profile:', error);
            alert('Error al actualizar el perfil');
        }
    };

    const formatDate = (dateString: string) => {
        const date = new Date(dateString);
        return date.toLocaleString('es-ES', {
            year: 'numeric',
            month: 'long',
            day: 'numeric',
            hour: '2-digit',
            minute: '2-digit'
        });
    };

    const getTicketStatusColor = (status: string) => {
        switch (status) {
            case 'CONFIRMED': return 'bg-green-100 text-green-800';
            case 'PENDING_APPROVAL': return 'bg-yellow-100 text-yellow-800';
            case 'CANCELLED': return 'bg-red-100 text-red-800';
            case 'NO_SHOW': return 'bg-gray-100 text-gray-800';
            default: return 'bg-blue-100 text-blue-800';
        }
    };

    if (!token) {
        return null;
    }

    if (loading) {
        return (
            <div className="min-h-screen bg-gray-50 flex items-center justify-center">
                <div className="text-center">
                    <div className="inline-block animate-spin rounded-full h-12 w-12 border-b-2 border-blue-600"></div>
                    <p className="mt-4 text-gray-600">Cargando...</p>
                </div>
            </div>
        );
    }

    return (
        <div className="min-h-screen bg-gray-50">
            {/* Header */}
            <div className="bg-white shadow">
                <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-6">
                    <div className="flex items-center justify-between">
                        <div>
                            <h1 className="text-3xl font-bold text-gray-900">Mi Cuenta</h1>
                            <p className="mt-1 text-sm text-gray-600">Gestiona tus tickets y perfil</p>
                        </div>
                        <a href="/" className="text-blue-600 hover:text-blue-700">
                            ← Volver al inicio
                        </a>
                    </div>
                </div>
            </div>

            <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
                {/* Tabs */}
                <div className="mb-6 border-b border-gray-200">
                    <nav className="-mb-px flex space-x-8">
                        <button
                            onClick={() => setActiveTab('tickets')}
                            className={`py-4 px-1 border-b-2 font-medium text-sm ${activeTab === 'tickets'
                                ? 'border-blue-500 text-blue-600'
                                : 'border-transparent text-gray-500 hover:text-gray-700 hover:border-gray-300'
                                }`}
                        >
                            Mis Tickets
                        </button>
                        <button
                            onClick={() => setActiveTab('profile')}
                            className={`py-4 px-1 border-b-2 font-medium text-sm ${activeTab === 'profile'
                                ? 'border-blue-500 text-blue-600'
                                : 'border-transparent text-gray-500 hover:text-gray-700 hover:border-gray-300'
                                }`}
                        >
                            Mi Perfil
                        </button>
                    </nav>
                </div>

                {/* Content */}
                {activeTab === 'tickets' ? (
                    <div>
                        <h2 className="text-2xl font-bold text-gray-900 mb-6">Mis Tickets</h2>
                        {tickets.length === 0 ? (
                            <div className="bg-white rounded-lg shadow p-8 text-center">
                                <p className="text-gray-500">No tienes tickets registrados.</p>
                                <a href="/" className="mt-4 inline-block text-blue-600 hover:text-blue-700">
                                    Buscar viajes →
                                </a>
                            </div>
                        ) : (
                            <div className="space-y-4">
                                {tickets.map(ticket => (
                                    <div key={ticket.id} className="bg-white rounded-lg shadow-md p-6 hover:shadow-lg transition">
                                        <div className="flex justify-between items-start mb-4">
                                            <div>
                                                <h3 className="text-lg font-semibold text-gray-900">
                                                    Ticket #{ticket.id}
                                                </h3>
                                                <p className="text-sm text-gray-600">Asiento: {ticket.seatNumber}</p>
                                            </div>
                                            <span className={`px-3 py-1 rounded-full text-sm font-medium ${getTicketStatusColor(ticket.status)}`}>
                                                {ticket.status}
                                            </span>
                                        </div>

                                        <div className="grid grid-cols-1 md:grid-cols-2 gap-4 mb-4">
                                            <div>
                                                <p className="text-sm text-gray-600">Origen</p>
                                                <p className="font-medium">Parada #{ticket.fromStopId}</p>
                                            </div>
                                            <div>
                                                <p className="text-sm text-gray-600">Destino</p>
                                                <p className="font-medium">Parada #{ticket.toStopId}</p>
                                            </div>
                                            <div>
                                                <p className="text-sm text-gray-600">Precio</p>
                                                <p className="font-medium">${ticket.price.toFixed(2)}</p>
                                            </div>
                                            <div>
                                                <p className="text-sm text-gray-600">QR Code</p>
                                                <p className="font-medium font-mono text-sm">{ticket.qrCode || 'N/A'}</p>
                                            </div>
                                        </div>

                                        {ticket.status === 'CONFIRMED' && (
                                            <div className="flex gap-2 pt-4 border-t">
                                                <button
                                                    onClick={() => handleCancelTicket(ticket.id)}
                                                    className="flex-1 px-4 py-2 bg-red-600 text-white rounded-lg hover:bg-red-700 transition"
                                                >
                                                    Cancelar Ticket
                                                </button>
                                                <button
                                                    onClick={() => handleRequestRefund(ticket.id)}
                                                    className="flex-1 px-4 py-2 bg-yellow-600 text-white rounded-lg hover:bg-yellow-700 transition"
                                                >
                                                    Solicitar Reembolso
                                                </button>
                                            </div>
                                        )}

                                        {ticket.status === 'PENDING_APPROVAL' && (
                                            <div className="pt-4 border-t">
                                                <p className="text-sm text-yellow-700 mb-2">
                                                    Este ticket está pendiente de aprobación
                                                </p>
                                            </div>
                                        )}
                                    </div>
                                ))}
                            </div>
                        )}
                    </div>
                ) : (
                    <div className="max-w-2xl">
                        <div className="bg-white rounded-lg shadow p-6">
                            <div className="flex justify-between items-center mb-6">
                                <h2 className="text-2xl font-bold text-gray-900">Mi Perfil</h2>
                                {!editMode && (
                                    <button
                                        onClick={() => setEditMode(true)}
                                        className="px-4 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700 transition"
                                    >
                                        Editar Perfil
                                    </button>
                                )}
                            </div>

                            {!editMode ? (
                                <div className="space-y-4">
                                    <div>
                                        <p className="text-sm text-gray-600">Nombre</p>
                                        <p className="text-lg font-medium">{account?.name}</p>
                                    </div>
                                    <div>
                                        <p className="text-sm text-gray-600">Email</p>
                                        <p className="text-lg font-medium">{account?.email}</p>
                                    </div>
                                    <div>
                                        <p className="text-sm text-gray-600">Teléfono</p>
                                        <p className="text-lg font-medium">{account?.phone}</p>
                                    </div>
                                    <div>
                                        <p className="text-sm text-gray-600">Rol</p>
                                        <p className="text-lg font-medium">{account?.role}</p>
                                    </div>
                                    <div>
                                        <p className="text-sm text-gray-600">Estado</p>
                                        <span className={`inline-block px-3 py-1 rounded-full text-sm font-medium ${account?.status === 'ACTIVE'
                                            ? 'bg-green-100 text-green-800'
                                            : 'bg-red-100 text-red-800'
                                            }`}>
                                            {account?.status}
                                        </span>
                                    </div>
                                </div>
                            ) : (
                                <form onSubmit={handleUpdateProfile} className="space-y-4">
                                    <div>
                                        <label className="block text-sm font-medium text-gray-700 mb-1">
                                            Nombre
                                        </label>
                                        <input
                                            type="text"
                                            value={formData.name}
                                            onChange={(e) => setFormData({ ...formData, name: (e.target as HTMLInputElement).value })}
                                            className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent"
                                            required
                                        />
                                    </div>
                                    <div>
                                        <label className="block text-sm font-medium text-gray-700 mb-1">
                                            Email
                                        </label>
                                        <input
                                            type="email"
                                            value={formData.email}
                                            onChange={(e) => setFormData({ ...formData, email: (e.target as HTMLInputElement).value })}
                                            className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent"
                                            required
                                        />
                                    </div>
                                    <div>
                                        <label className="block text-sm font-medium text-gray-700 mb-1">
                                            Teléfono
                                        </label>
                                        <input
                                            type="tel"
                                            value={formData.phone}
                                            onChange={(e) => setFormData({ ...formData, phone: (e.target as HTMLInputElement).value })}
                                            className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent"
                                            required
                                        />
                                    </div>
                                    <div>
                                        <label className="block text-sm font-medium text-gray-700 mb-1">
                                            Nueva Contraseña (opcional)
                                        </label>
                                        <input
                                            type="password"
                                            value={formData.password}
                                            onChange={(e) => setFormData({ ...formData, password: (e.target as HTMLInputElement).value })}
                                            className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent"
                                            placeholder="Dejar en blanco para no cambiar"
                                        />
                                    </div>
                                    <div className="flex gap-2 pt-4">
                                        <button
                                            type="submit"
                                            className="flex-1 px-4 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700 transition"
                                        >
                                            Guardar Cambios
                                        </button>
                                        <button
                                            type="button"
                                            onClick={() => {
                                                setEditMode(false);
                                                setFormData({
                                                    name: account?.name || '',
                                                    email: account?.email || '',
                                                    phone: account?.phone || '',
                                                    password: ''
                                                });
                                            }}
                                            className="flex-1 px-4 py-2 bg-gray-300 text-gray-700 rounded-lg hover:bg-gray-400 transition"
                                        >
                                            Cancelar
                                        </button>
                                    </div>
                                </form>
                            )}
                        </div>
                    </div>
                )}
            </div>
        </div>
    );
};

export default Account;