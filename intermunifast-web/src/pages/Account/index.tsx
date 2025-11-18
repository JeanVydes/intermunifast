import { useEffect, useState } from "preact/hooks";
import useAccountStore from "../../stores/AccountStore";
import useAuthStore from "../../stores/AuthStore";
import { TicketAPI, TicketResponse, AccountAPI } from "../../api";
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

    const handleUpdateProfile = async (e: Event) => {
        e.preventDefault();

        try {
            const updateData: UpdateAccountRequest = {
                name: formData.name,
                email: formData.email,
                phone: formData.phone,
            };

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
            day: 'numeric',
            month: 'long',
            year: 'numeric'
        });
    };

    if (!token) {
        return null;
    }

    if (loading) {
        return (
            <div className="min-h-screen bg-neutral-950 flex items-center justify-center">
                <div className="text-center">
                    <div className="inline-block animate-spin rounded-full h-12 w-12 border-b-2 border-accent"></div>
                    <p className="mt-4 text-white font-medium">Cargando...</p>
                </div>
            </div>
        );
    }

    return (
        <div className="min-h-screen bg-neutral-950 pt-16 pb-20">
            <div className="bg-white/5 backdrop-blur-xl px-4 pt-8 pb-6 border-b border-white/10">
                <div className="max-w-4xl mx-auto">
                    <div className="flex items-center justify-between mb-6">
                        <div>
                            <h1 className="text-3xl font-bold text-white">Mi Cuenta</h1>
                            <p className="text-neutral-400 mt-1">Gestiona tus tickets y perfil</p>
                        </div>
                    </div>

                    <div className="flex gap-3">
                        <button
                            onClick={() => setActiveTab('tickets')}
                            className={`px-6 py-2.5 rounded-2xl font-medium transition-all duration-200 ${activeTab === 'tickets'
                                ? 'bg-accent text-white shadow-lg'
                                : 'bg-white/10 text-white hover:bg-white/15'
                                }`}
                        >
                            Mis Tickets
                        </button>
                        <button
                            onClick={() => setActiveTab('profile')}
                            className={`px-6 py-2.5 rounded-2xl font-medium transition-all duration-200 ${activeTab === 'profile'
                                ? 'bg-accent text-white shadow-lg'
                                : 'bg-white/10 text-white hover:bg-white/15'
                                }`}
                        >
                            Perfil
                        </button>
                    </div>
                </div>
            </div>

            <div className="max-w-4xl mx-auto px-4 mt-8">
                {activeTab === 'tickets' ? (
                    <>
                        {tickets.length === 0 ? (
                            <div className="bg-white/5 backdrop-blur-xl border border-white/10 rounded-3xl p-12 text-center">
                                <div className="inline-flex items-center justify-center w-20 h-20 rounded-2xl bg-accent/10 mb-6">
                                    <svg className="w-10 h-10 text-accent" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                                        <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M15 5v2m0 4v2m0 4v2M5 5a2 2 0 00-2 2v3a2 2 0 110 4v3a2 2 0 002 2h14a2 2 0 002-2v-3a2 2 0 110-4V7a2 2 0 00-2-2H5z" />
                                    </svg>
                                </div>
                                <p className="text-white text-lg font-medium mb-2">No tienes tickets activos</p>
                                <p className="text-neutral-400 mb-6">Busca viajes y reserva tus asientos</p>
                                <a href="/" className="inline-block bg-accent text-white px-8 py-3 rounded-2xl font-medium hover:bg-accent-dark transition-all duration-200 shadow-lg">
                                    Buscar Viajes
                                </a>
                            </div>
                        ) : (
                            <div className="space-y-4">
                                {tickets.map(ticket => (
                                    <div key={ticket.id} className="bg-white rounded-ticket shadow-ticket overflow-hidden">
                                        <div className="p-5 border-b-2 border-dashed border-neutral-200">
                                            <div className="flex items-start justify-between mb-3">
                                                <span className={`px-3 py-1 rounded-full text-xs font-bold ${ticket.status === 'CONFIRMED' ? 'bg-accent text-white' : 'bg-neutral-200 text-neutral-700'
                                                    }`}>
                                                    {ticket.status === 'CONFIRMED' ? 'Active' : ticket.status}
                                                </span>
                                            </div>

                                            <div className="mb-4">
                                                <div className="flex items-center justify-between mb-2">
                                                    <div>
                                                        <p className="text-xs text-neutral-500">Bus station</p>
                                                        <p className="text-lg font-bold text-neutral-900">Stop #{ticket.fromStopId}</p>
                                                    </div>
                                                    <div className="text-right">
                                                        <p className="text-xs text-neutral-500">Departure</p>
                                                        <p className="text-lg font-bold text-neutral-900">--:--</p>
                                                    </div>
                                                </div>

                                                <div className="relative py-2">
                                                    <div className="absolute left-0 right-0 top-1/2 h-0.5 bg-accent"></div>
                                                    <div className="absolute left-0 top-1/2 w-2 h-2 bg-accent rounded-full -translate-y-1/2"></div>
                                                    <div className="absolute right-0 top-1/2 w-2 h-2 bg-accent rounded-full -translate-y-1/2"></div>
                                                </div>

                                                <div className="flex items-center justify-between mt-2">
                                                    <div>
                                                        <p className="text-xs text-neutral-500">Bus station</p>
                                                        <p className="text-lg font-bold text-neutral-900">Stop #{ticket.toStopId}</p>
                                                    </div>
                                                    <div className="text-right">
                                                        <p className="text-xs text-neutral-500">Arrival</p>
                                                        <p className="text-lg font-bold text-neutral-900">--:--</p>
                                                    </div>
                                                </div>
                                            </div>

                                            <div className="flex items-center justify-between py-3 border-t border-neutral-100">
                                                <div className="flex items-center gap-4">
                                                    <div className="flex items-center gap-1 text-sm">
                                                        <svg className="w-4 h-4" fill="currentColor" viewBox="0 0 20 20">
                                                            <path d="M10 9a3 3 0 100-6 3 3 0 000 6zm-7 9a7 7 0 1114 0H3z" />
                                                        </svg>
                                                        <span className="font-medium">1</span>
                                                    </div>
                                                    <div className="flex items-center gap-1 text-sm">
                                                        <svg className="w-4 h-4" fill="currentColor" viewBox="0 0 20 20">
                                                            <path fillRule="evenodd" d="M4 4a2 2 0 012-2h8a2 2 0 012 2v12a1 1 0 110 2h-3a1 1 0 01-1-1v-2a1 1 0 00-1-1H9a1 1 0 00-1 1v2a1 1 0 01-1 1H4a1 1 0 110-2V4zm3 1h2v2H7V5zm2 4H7v2h2V9zm2-4h2v2h-2V5zm2 4h-2v2h2V9z" clipRule="evenodd" />
                                                        </svg>
                                                        <span className="font-medium">#{ticket.seatNumber}</span>
                                                    </div>
                                                </div>
                                            </div>
                                        </div>

                                        <div className="px-5 py-3 bg-neutral-50">
                                            <p className="text-xs text-neutral-500 text-center font-mono">
                                                {ticket.id}-{ticket.fromStopId}-{ticket.toStopId}
                                            </p>
                                        </div>

                                        <div className="p-4">
                                            <button
                                                onClick={() => alert(`QR Code: ${ticket.qrCode || 'N/A'}`)}
                                                className="w-full bg-accent text-white py-3 rounded-2xl font-bold hover:bg-accent-dark transition-all duration-200 shadow-lg"
                                            >
                                                Show Ticket
                                            </button>
                                            {ticket.status === 'CONFIRMED' && (
                                                <button
                                                    onClick={() => handleCancelTicket(ticket.id)}
                                                    className="w-full mt-2 text-accent py-2 rounded-full font-medium hover:bg-accent/10 transition"
                                                >
                                                    Cancel Ticket
                                                </button>
                                            )}
                                        </div>
                                    </div>
                                ))}
                            </div>
                        )}
                    </>
                ) : (
                    <div className="bg-white rounded-ticket shadow-ticket p-6">
                        <h2 className="text-2xl font-bold text-neutral-900 mb-6">Profile</h2>

                        {!editMode ? (
                            <>
                                <div className="space-y-4 mb-6">
                                    <div>
                                        <p className="text-xs text-neutral-500 mb-1">Name</p>
                                        <p className="text-lg font-semibold text-neutral-900">{account?.name}</p>
                                    </div>
                                    <div>
                                        <p className="text-xs text-neutral-500 mb-1">Email</p>
                                        <p className="text-lg font-semibold text-neutral-900">{account?.email}</p>
                                    </div>
                                    <div>
                                        <p className="text-xs text-neutral-500 mb-1">Phone</p>
                                        <p className="text-lg font-semibold text-neutral-900">{account?.phone}</p>
                                    </div>
                                    <div>
                                        <p className="text-xs text-neutral-500 mb-1">Role</p>
                                        <p className="text-lg font-semibold text-neutral-900">{account?.role}</p>
                                    </div>
                                </div>
                                <button
                                    onClick={() => setEditMode(true)}
                                    className="w-full bg-accent text-white py-3 rounded-2xl font-bold hover:bg-accent-dark transition-all duration-200 shadow-lg"
                                >
                                    Edit Profile
                                </button>
                            </>
                        ) : (
                            <form onSubmit={handleUpdateProfile} className="space-y-4">
                                <div>
                                    <label className="block text-xs font-medium text-neutral-600 mb-2">Name</label>
                                    <input
                                        type="text"
                                        value={formData.name}
                                        onChange={(e) => setFormData({ ...formData, name: (e.target as HTMLInputElement).value })}
                                        className="w-full px-4 py-3 border border-neutral-300 rounded-xl focus:ring-2 focus:ring-accent focus:border-transparent outline-none"
                                        required
                                    />
                                </div>
                                <div>
                                    <label className="block text-xs font-medium text-neutral-600 mb-2">Email</label>
                                    <input
                                        type="email"
                                        value={formData.email}
                                        onChange={(e) => setFormData({ ...formData, email: (e.target as HTMLInputElement).value })}
                                        className="w-full px-4 py-3 border border-neutral-300 rounded-xl focus:ring-2 focus:ring-accent focus:border-transparent outline-none"
                                        required
                                    />
                                </div>
                                <div>
                                    <label className="block text-xs font-medium text-neutral-600 mb-2">Phone</label>
                                    <input
                                        type="tel"
                                        value={formData.phone}
                                        onChange={(e) => setFormData({ ...formData, phone: (e.target as HTMLInputElement).value })}
                                        className="w-full px-4 py-3 border border-neutral-300 rounded-xl focus:ring-2 focus:ring-accent focus:border-transparent outline-none"
                                        required
                                    />
                                </div>
                                <div>
                                    <label className="block text-xs font-medium text-neutral-600 mb-2">New Password (optional)</label>
                                    <input
                                        type="password"
                                        value={formData.password}
                                        onChange={(e) => setFormData({ ...formData, password: (e.target as HTMLInputElement).value })}
                                        className="w-full px-4 py-3 border border-neutral-300 rounded-xl focus:ring-2 focus:ring-accent focus:border-transparent outline-none"
                                        placeholder="Leave blank to keep current"
                                    />
                                </div>
                                <div className="flex gap-2 pt-2">
                                    <button
                                        type="submit"
                                        className="flex-1 bg-accent text-white py-3 rounded-2xl font-bold hover:bg-accent-dark transition-all duration-200 shadow-lg"
                                    >
                                        Save Changes
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
                                        className="flex-1 bg-white/10 text-white py-3 rounded-2xl font-bold hover:bg-white/20 transition-all duration-200"
                                    >
                                        Cancel
                                    </button>
                                </div>
                            </form>
                        )}
                    </div>
                )}
            </div>

            <div className="fixed bottom-0 left-0 right-0 bg-white border-t border-neutral-200 px-4 py-3">
                <div className="max-w-md mx-auto flex items-center justify-around">
                    <a href="/" className="flex flex-col items-center text-neutral-400 hover:text-accent transition">
                        <svg className="w-6 h-6" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M21 21l-6-6m2-5a7 7 0 11-14 0 7 7 0 0114 0z" />
                        </svg>
                        <span className="text-xs mt-1">Booking</span>
                    </a>
                    <a href="/account" className="flex flex-col items-center text-accent">
                        <svg className="w-6 h-6" fill="currentColor" viewBox="0 0 20 20">
                            <path d="M9 2a1 1 0 000 2h2a1 1 0 100-2H9z" />
                            <path fillRule="evenodd" d="M4 5a2 2 0 012-2 3 3 0 003 3h2a3 3 0 003-3 2 2 0 012 2v11a2 2 0 01-2 2H6a2 2 0 01-2-2V5zm3 4a1 1 0 000 2h.01a1 1 0 100-2H7zm3 0a1 1 0 000 2h3a1 1 0 100-2h-3zm-3 4a1 1 0 100 2h.01a1 1 0 100-2H7zm3 0a1 1 0 100 2h3a1 1 0 100-2h-3z" clipRule="evenodd" />
                        </svg>
                        <span className="text-xs mt-1 font-bold">Tickets</span>
                    </a>
                    <a href={account?.role === 'ADMIN' ? '/dashboard' : '#'} className="flex flex-col items-center text-neutral-400 hover:text-accent transition">
                        <svg className="w-6 h-6" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M16 7a4 4 0 11-8 0 4 4 0 018 0zM12 14a7 7 0 00-7 7h14a7 7 0 00-7-7z" />
                        </svg>
                        <span className="text-xs mt-1">Profile</span>
                    </a>
                </div>
            </div>
        </div>
    );
};

export default Account;
