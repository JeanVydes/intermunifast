import { FunctionComponent } from 'preact';
import { useState, useEffect } from 'preact/hooks';
import { Check, X, Search, Filter, Clipboard } from 'lucide-preact';
import DashboardLayout from '../../components/dashboard/DashboardLayout';
import ProtectedRoute from '../../components/ProtectedRoute';
import { TicketAPI } from '../../api';
import { TicketResponse, TicketStatus, PaymentStatus } from '../../api/types/Booking';

const STATUS_COLORS: Record<TicketStatus, string> = {
    CONFIRMED: 'bg-green-100 text-green-800',
    PENDING_APPROVAL: 'bg-yellow-100 text-yellow-800',
    CANCELLED: 'bg-red-100 text-red-800',
    NO_SHOW: 'bg-gray-100 text-gray-800',
};

const PAYMENT_STATUS_COLORS: Record<PaymentStatus, string> = {
    COMPLETED: 'bg-green-100 text-green-800',
    PENDING: 'bg-yellow-100 text-yellow-800',
    FAILED: 'bg-red-100 text-red-800',
};

export const PendingTickets: FunctionComponent = () => {
    const [tickets, setTickets] = useState<TicketResponse[]>([]);
    const [filteredTickets, setFilteredTickets] = useState<TicketResponse[]>([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState<string | null>(null);
    const [searchTerm, setSearchTerm] = useState('');
    const [ticketStatusFilter, setTicketStatusFilter] = useState<TicketStatus | 'ALL'>('PENDING_APPROVAL');
    const [paymentStatusFilter, setPaymentStatusFilter] = useState<PaymentStatus | 'ALL'>('ALL');
    const [processingTicket, setProcessingTicket] = useState<number | null>(null);

    useEffect(() => {
        fetchTickets();
    }, []);

    useEffect(() => {
        filterTickets();
    }, [tickets, searchTerm, ticketStatusFilter, paymentStatusFilter]);

    const fetchTickets = async () => {
        try {
            setLoading(true);
            setError(null);
            // Fetch all tickets (ADMIN/DISPATCHER only)
            const response = await TicketAPI.getAll();
            setTickets(response.data);
        } catch (err: any) {
            setError(err.message || 'Failed to fetch tickets');
        } finally {
            setLoading(false);
        }
    };

    const filterTickets = () => {
        let filtered = [...tickets];

        if (ticketStatusFilter !== 'ALL') {
            filtered = filtered.filter(ticket => ticket.status === ticketStatusFilter);
        }
        if (paymentStatusFilter !== 'ALL') {
            filtered = filtered.filter(ticket => ticket.paymentStatus === paymentStatusFilter);
        }
        if (searchTerm.trim()) {
            const term = searchTerm.toLowerCase().trim();
            filtered = filtered.filter(ticket =>
                ticket.id.toString().includes(term) ||
                ticket.seatNumber.toLowerCase().includes(term) ||
                ticket.tripId.toString().includes(term) ||
                ticket.paymentMethod.toLowerCase().includes(term)
            );
        }

        setFilteredTickets(filtered);
    };

    const handleApprove = async (ticketId: number) => {
        try {
            setProcessingTicket(ticketId);
            await TicketAPI.approve(undefined, { pathParams: { id: ticketId } });
            setTickets(prev => prev.filter(t => t.id !== ticketId));
            alert('Ticket approved successfully!');
        } catch (err: any) {
            alert(err.message || 'Failed to approve ticket');
        } finally {
            setProcessingTicket(null);
        }
    };

    const handleCancel = async (ticketId: number) => {
        if (!confirm('Are you sure you want to cancel this pending ticket?')) return;

        try {
            setProcessingTicket(ticketId);
            await TicketAPI.cancelPending(undefined, { pathParams: { id: ticketId } });
            setTickets(prev => prev.filter(t => t.id !== ticketId));
            alert('Ticket cancelled successfully!');
        } catch (err: any) {
            alert(err.message || 'Failed to cancel ticket');
        } finally {
            setProcessingTicket(null);
        }
    };

    const handleCheckIn = async (ticketId: number, qrCode: string) => {
        if (!qrCode) {
            alert('This ticket does not have a QR code yet. Payment may not be completed.');
            return;
        }

        try {
            setProcessingTicket(ticketId);
            await TicketAPI.checkIn({ qrCode });
            // Refresh tickets to show updated status
            await fetchTickets();
            alert('Ticket checked in successfully!');
        } catch (err: any) {
            const errorMessage = err.response?.data?.message || err.response?.data?.error || err.message || 'Failed to check in ticket';
            alert(errorMessage);
        } finally {
            setProcessingTicket(null);
        }
    };

    const handleRefresh = () => fetchTickets();

    return (
        <ProtectedRoute allowedRoles={['DISPATCHER', 'ADMIN']}>
            <DashboardLayout>
                <div className="p-8">
                    <div className="flex items-center justify-between mb-8">
                        <div>
                            <h1 className="text-3xl font-bold text-gray-900">Ticket Management</h1>
                            <p className="text-gray-600 mt-2">
                                View and manage all tickets, approve or cancel pending tickets (occupation &gt; 95%)
                            </p>
                        </div>
                        <button
                            onClick={handleRefresh}
                            className="px-4 py-2 bg-purple-600 text-white rounded-lg hover:bg-purple-700 transition-colors"
                        >
                            Refresh
                        </button>
                    </div>

                    <div className="bg-white rounded-xl shadow-sm p-6 mb-6">
                        <div className="flex gap-4 flex-wrap">
                            <div className="flex-1 min-w-[300px]">
                                <div className="relative">
                                    <Search className="absolute left-3 top-1/2 transform -translate-y-1/2 w-5 h-5 text-gray-400" />
                                    <input
                                        type="text"
                                        placeholder="Search by ticket ID or seat number..."
                                        value={searchTerm}
                                        onInput={(e) => setSearchTerm((e.target as HTMLInputElement).value)}
                                        className="w-full pl-10 pr-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-purple-600 focus:border-transparent"
                                    />
                                </div>
                            </div>
                            <div className="flex items-center gap-2">
                                <Filter className="w-5 h-5 text-gray-400" />
                                <select
                                    value={ticketStatusFilter}
                                    onChange={(e) => {
                                        const value = (e.target as HTMLSelectElement).value as TicketStatus | 'ALL';
                                        setTicketStatusFilter(value);
                                    }}
                                    className="px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-purple-600 focus:border-transparent"
                                >
                                    <option value="PENDING_APPROVAL">⏳ Pending Approval (Awaiting Dispatcher)</option>
                                    <option value="ALL">All Ticket Statuses</option>
                                    <option value="CONFIRMED">✅ Confirmed (Can Board)</option>
                                    <option value="CANCELLED">❌ Cancelled</option>
                                    <option value="NO_SHOW">🚫 No Show (Didn't Arrive)</option>
                                </select>
                            </div>
                            <div className="flex items-center gap-2">
                                <Filter className="w-5 h-5 text-gray-400" />
                                <select
                                    value={paymentStatusFilter}
                                    onChange={(e) => {
                                        const value = (e.target as HTMLSelectElement).value as PaymentStatus | 'ALL';
                                        setPaymentStatusFilter(value);
                                    }}
                                    className="px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-purple-600 focus:border-transparent"
                                >
                                    <option value="ALL">All Payment Statuses</option>
                                    <option value="COMPLETED">💳 Completed</option>
                                    <option value="PENDING">⏱️ Pending</option>
                                    <option value="FAILED">⚠️ Failed</option>
                                </select>
                            </div>
                        </div>
                    </div>

                    {error && (
                        <div className="bg-red-50 border border-red-200 text-red-700 px-4 py-3 rounded-lg mb-6">
                            {error}
                        </div>
                    )}

                    {loading ? (
                        <div className="bg-white rounded-xl shadow-sm p-8 text-center">
                            <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-purple-600 mx-auto"></div>
                            <p className="text-gray-600 mt-4">Loading tickets...</p>
                        </div>
                    ) : (
                        <>
                            <div className="bg-white rounded-xl shadow-sm overflow-hidden">
                                <div className="overflow-x-auto">
                                    <table className="w-full">
                                        <thead className="bg-gray-50 border-b border-gray-200">
                                            <tr>
                                                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">Ticket ID</th>
                                                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">Seat</th>
                                                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">Trip ID</th>
                                                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">Price</th>
                                                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">Payment</th>
                                                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">Status</th>
                                                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">Check-In</th>
                                                <th className="px-6 py-3 text-right text-xs font-medium text-gray-500 uppercase">Actions</th>
                                            </tr>
                                        </thead>
                                        <tbody className="bg-white divide-y divide-gray-200">
                                            {filteredTickets.length === 0 ? (
                                                <tr>
                                                    <td colSpan={9} className="px-6 py-8 text-center text-gray-500">No tickets found</td>
                                                </tr>
                                            ) : (
                                                filteredTickets.map((ticket) => (
                                                    <tr key={ticket.id} className="hover:bg-gray-50">
                                                        <td className="px-6 py-4 whitespace-nowrap text-sm font-medium text-gray-900">#{ticket.id}</td>
                                                        <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-600">{ticket.seatNumber}</td>
                                                        <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-600">#{ticket.tripId}</td>
                                                        <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-900 font-semibold">${ticket.price.toFixed(2)}</td>
                                                        <td className="px-6 py-4 whitespace-nowrap">
                                                            <span className={`px-2 py-1 text-xs font-semibold rounded-full ${PAYMENT_STATUS_COLORS[ticket.paymentStatus]}`}>
                                                                {ticket.paymentStatus}
                                                            </span>
                                                        </td>
                                                        <td className="px-6 py-4 whitespace-nowrap">
                                                            <span className={`px-2 py-1 text-xs font-semibold rounded-full ${STATUS_COLORS[ticket.status]}`}>
                                                                {ticket.status.replace('_', ' ')}
                                                            </span>
                                                        </td>
                                                        <td className="px-6 py-4 whitespace-nowrap">
                                                            {ticket.checkedIn ? (
                                                                <span className="px-2 py-1 text-xs font-semibold rounded-full bg-blue-100 text-blue-800">
                                                                    ✓ Checked In
                                                                </span>
                                                            ) : (
                                                                <span className="px-2 py-1 text-xs font-semibold rounded-full bg-gray-100 text-gray-600">
                                                                    Not Checked In
                                                                </span>
                                                            )}
                                                        </td>
                                                        <td className="px-6 py-4 whitespace-nowrap text-right text-sm font-medium">
                                                            <div className="flex gap-2 justify-end">
                                                                {ticket.status === 'PENDING_APPROVAL' && (
                                                                    <>
                                                                        <button
                                                                            onClick={() => handleApprove(ticket.id)}
                                                                            disabled={processingTicket === ticket.id}
                                                                            className="inline-flex items-center gap-1 px-3 py-1.5 bg-green-600 text-white rounded-lg hover:bg-green-700 transition-colors disabled:opacity-50"
                                                                        >
                                                                            <Check className="w-4 h-4" />
                                                                            Approve
                                                                        </button>
                                                                        <button
                                                                            onClick={() => handleCancel(ticket.id)}
                                                                            disabled={processingTicket === ticket.id}
                                                                            className="inline-flex items-center gap-1 px-3 py-1.5 bg-red-600 text-white rounded-lg hover:bg-red-700 transition-colors disabled:opacity-50"
                                                                        >
                                                                            <X className="w-4 h-4" />
                                                                            Cancel
                                                                        </button>
                                                                    </>
                                                                )}
                                                                {ticket.status === 'CONFIRMED' && !ticket.checkedIn && ticket.qrCode && ticket.paymentStatus === 'COMPLETED' && (
                                                                    <button
                                                                        onClick={() => handleCheckIn(ticket.id, ticket.qrCode!)}
                                                                        disabled={processingTicket === ticket.id}
                                                                        className="inline-flex items-center gap-1 px-3 py-1.5 bg-blue-600 text-white rounded-lg hover:bg-blue-700 transition-colors disabled:opacity-50"
                                                                    >
                                                                        <Clipboard className="w-4 h-4" />
                                                                        Check In
                                                                    </button>
                                                                )}
                                                                {!ticket.qrCode && ticket.status === 'CONFIRMED' && (
                                                                    <span className="text-xs text-gray-400">No QR code</span>
                                                                )}
                                                                {ticket.checkedIn && (
                                                                    <span className="text-xs text-gray-400">Checked In</span>
                                                                )}
                                                                {ticket.status === 'CANCELLED' || ticket.status === 'NO_SHOW' ? (
                                                                    <span className="text-xs text-gray-400">—</span>
                                                                ) : null}
                                                            </div>
                                                        </td>
                                                    </tr>
                                                ))
                                            )}
                                        </tbody>
                                    </table>
                                </div>
                            </div>

                            <div className="mt-6 grid grid-cols-1 md:grid-cols-3 gap-6">
                                <div className="bg-white rounded-xl shadow-sm p-6">
                                    <div className="text-sm text-gray-600">Total Tickets</div>
                                    <div className="text-3xl font-bold text-gray-900 mt-2">{filteredTickets.length}</div>
                                </div>
                                <div className="bg-white rounded-xl shadow-sm p-6">
                                    <div className="text-sm text-gray-600">Pending Approval</div>
                                    <div className="text-3xl font-bold text-yellow-600 mt-2">
                                        {filteredTickets.filter(t => t.status === 'PENDING_APPROVAL').length}
                                    </div>
                                </div>
                                <div className="bg-white rounded-xl shadow-sm p-6">
                                    <div className="text-sm text-gray-600">Total Value</div>
                                    <div className="text-3xl font-bold text-gray-900 mt-2">
                                        ${filteredTickets.reduce((sum, t) => sum + t.price, 0).toFixed(2)}
                                    </div>
                                </div>
                            </div>
                        </>
                    )}
                </div>
            </DashboardLayout>
        </ProtectedRoute>
    );
};

export default PendingTickets;
