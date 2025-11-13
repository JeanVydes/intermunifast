import { useEffect, useState } from "preact/hooks";
import useAccountStore from "../../stores/AccountStore";
import { TicketAPI, TicketResponse } from "../../api";
import { FunctionComponent } from "preact";

export const Account: FunctionComponent = () => {
    let { account, accountId } = useAccountStore();
    let [tickets, setTickets] = useState<TicketResponse[]>([]);

    if (!accountId) {
        return <div>Please log in to view your account.</div>;
    }

    useEffect(() => {
        async function fetchTicketsByAccountId() {
            console.log("Fetching tickets for accountId:", accountId);
            if (accountId) {
                //let response = await TicketAPI.getByAccountId(accountId);
                let response = null; // Simulación de respuesta de API
                if (response) {
                    setTickets(response.data);
                } else {
                    setTickets([{
                        id: 1,
                        seatNumber: "12A",
                        fromStopId: 1,
                        toStopId: 2,
                        tripId: 1,
                        paymentMethod: "CARD",
                        paymentIntentId: "pi_123456789",
                    }, {
                        id: 2,
                        seatNumber: "15B",
                        fromStopId: 2,
                        toStopId: 3,
                        tripId: 2,
                        paymentMethod: "CASH",
                    }, {
                        id: 3,
                        seatNumber: "7C",
                        fromStopId: 3,
                        toStopId: 4,
                        tripId: 3,
                        paymentMethod: "DIGITAL_WALLET",
                        paymentIntentId: "pi_987654321",
                    }]);
                }
            }
        }

        fetchTicketsByAccountId();
    }, [accountId]);

    return <div>
        <h1 className="text-2xl font-bold mb-4">Account Details</h1>
        <p><strong>Name:</strong> {account?.name}</p>
        <p><strong>Email:</strong> {account?.email}</p>
        <p><strong>Phone:</strong> {account?.phone}</p>
        <p><strong>Role:</strong> {account?.role}</p>
        <p><strong>Status:</strong> {account?.status}</p>

        <h2 className="text-xl font-bold mt-6 mb-4">Your Tickets</h2>
        {tickets.length === 0 ? (
            <p>No tickets found for your account.</p>
        ) : (
            <ul>
                {tickets.map(ticket => (
                    <li key={ticket.id} className="mb-2 p-4 border rounded">
                        <p><strong>Ticket ID:</strong> {ticket.id}</p>
                        <p><strong>Seat Number:</strong> {ticket.seatNumber}</p>
                        <p><strong>Origin:</strong> {ticket.fromStopId}</p>
                        <p><strong>Destination:</strong> {ticket.toStopId}</p>
                    </li>
                ))}
            </ul>
        )}
    </div>;
}

export default Account;