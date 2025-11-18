import { FunctionComponent } from 'preact';
import { useState, useEffect } from 'preact/hooks';
import { useLocation } from 'preact-iso';
import { BusSearchBar, SearchParams } from '../../components/BusSearchBar';
import { BookingModal } from '../../components/BookingModal';
import { TripAPI, TicketAPI, RouteAPI, TripResponse, PassengerType } from '../../api';
import { RouteResponse, StopResponse } from '../../api/types/Transport';

interface CartItem {
	trip: TripResponse;
	route: RouteResponse;
	stops: StopResponse[];
	passengerType?: PassengerType;
	selectedSeat?: string;
	ticketId: number; // ID of the created ticket
	baggageId?: number | null; // ID of baggage if any
	baggage?: {
		weightKg: number;
		tagCode: string;
	};
}

export const Home: FunctionComponent = () => {
	const location = useLocation();
	const [results, setResults] = useState<TripResponse[]>([]);
	const [routes, setRoutes] = useState<RouteResponse[]>([]);
	const [stops, setStops] = useState<StopResponse[]>([]);
	const [cart, setCart] = useState<CartItem[]>([]);
	const [showBookingModal, setShowBookingModal] = useState(false);
	const [selectedTrip, setSelectedTrip] = useState<{ trip: TripResponse; route: RouteResponse; stops: StopResponse[] } | null>(null);
	const [isAuthenticated, setIsAuthenticated] = useState<boolean>(false);
	const [isProcessingPayment, setIsProcessingPayment] = useState(false);
	const [isLoadingCart, setIsLoadingCart] = useState(true);
	// State for initial search params from URL
	const [initialOrigin, setInitialOrigin] = useState('');
	const [initialDestination, setInitialDestination] = useState('');
	const [initialDate, setInitialDate] = useState('');

	// Check authentication status
	useEffect(() => {
		const token = localStorage.getItem('authToken');
		setIsAuthenticated(!!token);
	}, []);

	// Load cart from localStorage and fetch pending tickets on mount
	useEffect(() => {
		const loadCart = async () => {
			if (!isAuthenticated) {
				setIsLoadingCart(false);
				return;
			}
			try {
				// Get saved ticket IDs from localStorage
				const savedTicketIds = localStorage.getItem('cartTicketIds');
				if (!savedTicketIds) {
					setIsLoadingCart(false);
					return;
				}
				const ticketIds: number[] = JSON.parse(savedTicketIds);
				if (ticketIds.length === 0) {
					setIsLoadingCart(false);
					return;
				}
				console.log('📦 Restaurando carrito con tickets:', ticketIds);
				// Fetch tickets from backend to verify they still exist and are pending
				const cartItems: CartItem[] = [];
				for (const ticketId of ticketIds) {
					try {
						const ticketResponse = await TicketAPI.getById(undefined, {
							pathParams: { id: ticketId.toString() }
						});
						const ticket = ticketResponse.data;
						// Only add to cart if ticket is still CONFIRMED or PENDING_APPROVAL
						if (ticket.status === 'CONFIRMED' || ticket.status === 'PENDING_APPROVAL') {
							// Fetch trip details
							const tripResponse = await TripAPI.getById(undefined, {
								pathParams: { id: ticket.tripId.toString() }
							});
							const trip = tripResponse.data;
							// Fetch route details
							const routeResponse = await RouteAPI.getById(undefined, {
								pathParams: { id: trip.routeId.toString() }
							});
							const route = routeResponse.data;
							// Fetch stops for the route
							const stopsResponse = await RouteAPI.getStops(undefined, {
								pathParams: { id: trip.routeId.toString() }
							});
							const routeStops = stopsResponse.data;
							cartItems.push({
								trip,
								route,
								stops: routeStops,
								selectedSeat: ticket.seatNumber,
								ticketId: ticket.id,
								baggageId: null
							});
							console.log(`✅ Ticket ${ticketId} restaurado al carrito`);
						} else {
							console.log(`⚠️ Ticket ${ticketId} no está en estado válido (${ticket.status})`);
						}
					} catch (error) {
						console.error(`❌ Error al cargar ticket ${ticketId}:`, error);
					}
				}
				setCart(cartItems);
				// Update localStorage with valid ticket IDs
				const validTicketIds = cartItems.map(item => item.ticketId);
				localStorage.setItem('cartTicketIds', JSON.stringify(validTicketIds));
				if (cartItems.length > 0) {
					console.log(`✅ Carrito restaurado con ${cartItems.length} ticket(s)`);
				} else {
					console.log('ℹ️ No hay tickets válidos para restaurar');
					localStorage.removeItem('cartTicketIds');
				}
			} catch (error) {
				console.error('❌ Error al cargar el carrito:', error);
			} finally {
				setIsLoadingCart(false);
			}
		};
		loadCart();
	}, [isAuthenticated]);

	// Save cart ticket IDs to localStorage whenever cart changes
	useEffect(() => {
		if (!isLoadingCart) {
			const ticketIds = cart.map(item => item.ticketId);
			localStorage.setItem('cartTicketIds', JSON.stringify(ticketIds));
		}
	}, [cart, isLoadingCart]);

	// Load search from URL params on mount
	useEffect(() => {
		const params = new URLSearchParams(window.location.search);
		const origin = params.get('origin') || '';
		const destination = params.get('destination') || '';
		const date = params.get('date') || '';
		setInitialOrigin(origin);
		setInitialDestination(destination);
		setInitialDate(date);
		if (origin && destination) {
			handleSearch({ origin, destination, date });
		}
	}, []);

	const handleSearch = async (params: SearchParams) => {
		let departureDate = params.date;
		if (params.date != null && params.date !== '') {
			departureDate = new Date(params.date).toISOString();
		}
		// Update URL with search params
		const urlParams = new URLSearchParams();
		urlParams.set('origin', params.origin);
		urlParams.set('destination', params.destination);
		if (params.date) {
			urlParams.set('date', params.date);
		}
		location.route(`/?${urlParams.toString()}`, true);
		try {
			const response = await TripAPI.search(undefined, {
				queryParams: {
					origin: params.origin,
					destination: params.destination,
					departureDate: departureDate || undefined,
				}
			});
			const { trips, routes: routesData, stops: stopsData } = response.data;
			console.log('Search results:', { trips, routes: routesData, stops: stopsData });
			setResults(trips || []);
			setRoutes(routesData || []);
			setStops(stopsData || []);
		} catch (error) {
			console.error('Error during trip search:', error);
		}
	};

	const addToCart = (trip: TripResponse, route: RouteResponse, routeStops: StopResponse[]) => {
		// Check authentication before allowing booking
		if (!isAuthenticated) {
			// Save current URL for redirect after login
			const currentUrl = window.location.pathname + window.location.search;
			localStorage.setItem('redirectAfterLogin', currentUrl);
			// Redirect to login page
			location.route('/auth/signin');
			return;
		}
		setSelectedTrip({ trip, route, stops: routeStops });
		setShowBookingModal(true);
	};

	const handleBookingComplete = (tripId: number, seat: string, passengerType: PassengerType, ticketId: number, baggageId: number | null) => {
		if (!selectedTrip) return;
		const newCartItem: CartItem = {
			trip: selectedTrip.trip,
			route: selectedTrip.route,
			stops: selectedTrip.stops,
			passengerType: passengerType,
			selectedSeat: seat,
			ticketId: ticketId,
			baggageId: baggageId
		};
		setCart([...cart, newCartItem]);
		setShowBookingModal(false);
		setSelectedTrip(null);
	};

	const removeFromCart = async (ticketId: number) => {
		try {
			// Delete the ticket from the backend
			await TicketAPI.delete(undefined, {
				pathParams: { id: ticketId.toString() }
			});
			// Remove from cart state
			setCart(cart.filter(item => item.ticketId !== ticketId));
		} catch (error) {
			console.error('Error removing ticket from cart:', error);
			alert('Error al remover el ticket del carrito.');
		}
	};

	const handlePayAllTickets = async () => {
		if (cart.length === 0) {
			alert('El carrito está vacío');
			return;
		}
		setIsProcessingPayment(true);
		try {
			// Mock payment intent (en producción, integrar con Stripe real)
			const paymentIntentId = `pi_mock_${Date.now()}`;
			// Get all ticket IDs from cart
			const ticketIds = cart.map(item => item.ticketId);
			// Call the API to mark multiple tickets as paid
			// Backend expects: ticketIds as body array, paymentIntentId as query param
			const response = await TicketAPI.markMultipleAsPaid(
				ticketIds,
				{ queryParams: { paymentIntentId } }
			);
			// Clear the cart after successful payment
			setCart([]);
			// Clear cart from localStorage
			localStorage.removeItem('cartTicketIds');
			// Show success message
			alert(`✅ Pago exitoso! ${ticketIds.length} ticket${ticketIds.length > 1 ? 's' : ''} pagado${ticketIds.length > 1 ? 's' : ''}.`);
			// Optionally redirect to account/tickets page
			// location.route('/account');
		} catch (error) {
			console.error('Error processing payment:', error);
			alert('Error al procesar el pago. Por favor, intenta de nuevo.');
		} finally {
			setIsProcessingPayment(false);
		}
	};

	const formatDateTime = (dateString: string | undefined) => {
		if (!dateString) return 'N/A';
		const date = new Date(dateString);
		return date.toLocaleString('es-ES', {
			day: '2-digit',
			month: 'short',
			year: 'numeric',
			hour: '2-digit',
			minute: '2-digit'
		});
	};

	const formatTime = (dateString: string | undefined) => {
		if (!dateString) return 'N/A';
		const date = new Date(dateString);
		return date.toLocaleTimeString('es-ES', {
			hour: '2-digit',
			minute: '2-digit'
		});
	};

	const openMobileSearchModal = () => {
		// Placeholder
	};

	return (
		<div className="">
			<div className="flex flex-col items-center w-full">
				<div className="w-full mt-12">
					<BusSearchBar onSubmit={handleSearch} onMobileClick={openMobileSearchModal} initialOrigin={initialOrigin} initialDestination={initialDestination} initialDate={initialDate} />
				</div>
				<div className="grid lg:grid-cols-3 gap-12 items-start w-full max-w-7xl">
					<div className="lg:col-span-2 space-y-6">
						{results.length > 0 ? (
							<>
								<div className="mb-4">
									<h2 className="text-lg font-bold text-white flex items-center gap-2">
										<svg className="w-5 h-5 text-accent" fill="none" stroke="currentColor" viewBox="0 0 24 24">
											<path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M9 5H7a2 2 0 00-2 2v12a2 2 0 002 2h10a2 2 0 002-2V7a2 2 0 00-2-2h-2M9 5a2 2 0 002 2h2a2 2 0 002-2M9 5a2 2 0 012-2h2a2 2 0 012 2" />
										</svg>
										{results.length} viajes disponibles
									</h2>
								</div>
								{results.map((trip) => {
									const route = routes.find(r => r.id === trip.routeId);
									const routeStops = stops.filter(s => s.routeId === trip.routeId);
									const isInCart = cart.some(item => item.trip.id === trip.id);
									const cartItem = cart.find(item => item.trip.id === trip.id);
									if (!route) return null;
									return (
										<div key={trip.id} className="bg-gradient-to-br from-neutral-900/90 to-neutral-900/70 rounded-3xl border border-white/10 shadow-2xl shadow-black/50 backdrop-blur-xl overflow-hidden hover:border-accent/30 transition-all duration-300 group">
											<div className="px-6 py-5 bg-gradient-to-r from-neutral-900/80 to-neutral-800/80 border-b border-white/5 flex items-center gap-4">
												<div className="bg-gradient-to-br from-accent/20 to-accent/10 text-accent rounded-2xl p-3 group-hover:scale-110 transition-transform duration-300">
													<svg className="w-6 h-6" fill="none" stroke="currentColor" viewBox="0 0 24 24">
														<path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M8 7V3m8 4V3m-9 8h10M5 21h14a2 2 0 002-2V7a2 2 0 00-2-2H5a2 2 0 00-2 2v12a2 2 0 002 2z" />
													</svg>
												</div>
												<div>
													<p className="text-base font-bold text-white">{route.code}</p>
													<p className="text-sm text-neutral-300">{route.name}</p>
												</div>
											</div>
											<div className="p-7 space-y-7">
												<div className="flex items-center gap-4">
													<div className="text-center">
														<p className="text-3xl font-bold text-white">{formatTime(trip.departureAt)}</p>
														<p className="text-xs text-neutral-400 mt-1 font-medium">{route.origin}</p>
													</div>
													<div className="flex-1 relative flex items-center h-10">
														<div className="w-full h-1 bg-gradient-to-r from-accent/30 via-accent/60 to-accent/30 rounded-full"></div>
														<div className="absolute inset-x-0 flex justify-between px-2">
															<div className="w-3 h-3 rounded-full bg-accent shadow-lg shadow-accent/50"></div>
															<div className="w-3 h-3 rounded-full bg-accent shadow-lg shadow-accent/50"></div>
														</div>
														<div className="absolute left-1/2 -translate-x-1/2 bg-gradient-to-r from-neutral-950 to-neutral-900 px-4 py-1.5 rounded-full border border-accent text-xs text-accent font-bold shadow-lg">
															{route.durationMinutes} min
														</div>
													</div>
													<div className="text-center">
														<p className="text-3xl font-bold text-white">{formatTime(trip.arrivalAt)}</p>
														<p className="text-xs text-neutral-400 mt-1 font-medium">{route.destination}</p>
													</div>
												</div>
												<div className="grid grid-cols-2 gap-4">
													<div className="bg-gradient-to-br from-neutral-900/80 to-neutral-800/80 border border-white/10 rounded-2xl p-4 flex items-center gap-3 hover:border-accent/30 transition-all duration-300">
														<div className="p-2 bg-accent/10 rounded-xl">
															<svg className="w-6 h-6 text-accent" fill="none" stroke="currentColor" viewBox="0 0 24 24">
																<path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M13 7h8m0 0v8m0-8l-8 8-4-4-6 6" />
															</svg>
														</div>
														<div>
															<p className="text-xs text-neutral-400 font-medium">Distancia</p>
															<p className="text-base font-bold text-white">{route.distanceKm} km</p>
														</div>
													</div>
													<div className="bg-gradient-to-br from-neutral-900/80 to-neutral-800/80 border border-white/10 rounded-2xl p-4 flex items-center gap-3 hover:border-emerald-400/30 transition-all duration-300">
														<div className="p-2 bg-emerald-400/10 rounded-xl">
															<svg className="w-6 h-6 text-emerald-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
																<path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 8c-1.657 0-3 .895-3 2s1.343 2 3 2 3 .895 3 2-1.343 2-3 2m0-8c1.11 0 2.08.402 2.599 1M12 8V7m0 1v8m0 0v1m0-1c-1.11 0-2.08-.402-2.599-1" />
															</svg>
														</div>
														<div>
															<p className="text-xs text-neutral-400 font-medium">Precio estimado</p>
															<p className="text-base font-bold text-emerald-400">${(route.pricePerKm * route.distanceKm).toFixed(2)}</p>
														</div>
													</div>
												</div>
												{routeStops.length > 0 && (
													<div className="bg-gradient-to-br from-neutral-900/80 to-neutral-800/80 border border-white/10 rounded-2xl p-4">
														<details>
															<summary className="cursor-pointer flex items-center justify-between text-sm text-white font-semibold hover:text-accent transition-colors">
																<span className="flex items-center gap-2">
																	<svg className="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
																		<path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M17.657 16.657L13.414 20.9a1.998 1.998 0 01-2.827 0l-4.244-4.243a8 8 0 1111.314 0z" />
																	</svg>
																	{routeStops.length} paradas
																</span>
																<svg className="w-5 h-5 text-neutral-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
																	<path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M19 9l-7 7-7-7" />
																</svg>
															</summary>
															<div className="mt-4 space-y-3">
																{routeStops.map((stop) => (
																	<div key={stop.id} className="flex items-center gap-3 p-2 rounded-xl hover:bg-white/5 transition-colors">
																		<div className="w-8 h-8 rounded-full bg-gradient-to-br from-accent/20 to-accent/10 border border-accent text-accent text-xs font-bold flex items-center justify-center">
																			{stop.sequence}
																		</div>
																		<span className="text-sm text-neutral-200 font-medium">{stop.name}</span>
																	</div>
																))}
															</div>
														</details>
													</div>
												)}
												<button
													onClick={() => isInCart && cartItem ? removeFromCart(cartItem.ticketId) : addToCart(trip, route, routeStops)}
													className={`w-full py-4 rounded-2xl font-bold transition-all duration-300 text-sm shadow-xl ${isInCart ? 'bg-white/5 text-white/70 border border-white/10 hover:bg-white/10' : 'bg-gradient-to-r from-accent to-accent-light hover:from-accent-light hover:to-accent text-white shadow-accent/40 hover:shadow-2xl hover:scale-[1.02]'}`}
												>
													{isInCart ? 'Remover del carrito' : 'Reservar asiento'}
												</button>
											</div>
										</div>
									);
								})}
							</>
						) : (
							<div className="rounded-2xl bg-gradient-to-br from-white/5 to-white/[0.02] border border-dashed border-white/20 p-12 text-center backdrop-blur-sm">
								<svg className="w-16 h-16 mx-auto mb-4 text-neutral-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
									<path strokeLinecap="round" strokeLinejoin="round" strokeWidth={1.5} d="M21 21l-6-6m2-5a7 7 0 11-14 0 7 7 0 0114 0z" />
								</svg>
								<h3 className="text-lg font-bold text-white mb-2">Empieza buscando</h3>
								<p className="text-sm text-neutral-400">Usa el buscador para encontrar viajes disponibles</p>
							</div>
						)}
					</div>
					<aside className="lg:col-span-1">
						<div className="sticky top-36 bg-gradient-to-br from-neutral-900/95 to-neutral-900/90 rounded-3xl overflow-hidden max-h-[calc(100vh-10rem)] flex flex-col border border-white/10 shadow-2xl shadow-black/60 backdrop-blur-xl">
							<div className="bg-gradient-to-r from-accent via-accent-dark to-accent px-6 py-5 flex items-center gap-3 shadow-lg">
								<div className="p-2 bg-white/20 rounded-xl backdrop-blur-sm">
									<svg className="w-6 h-6 text-white" fill="none" stroke="currentColor" viewBox="0 0 24 24">
										<path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M3 3h2l.4 2M7 13h10l4-8H5.4M7 13L5.4 5M7 13l-2.293 2.293c-.63.63-.184 1.707.707 1.707H17m0 0a2 2 0 100 4 2 2 0 000-4zm-8 2a2 2 0 11-4 0 2 2 0 014 0z" />
									</svg>
								</div>
								<div>
									<h3 className="font-bold text-white text-base">Mi Carrito</h3>
									<p className="text-xs text-white/80">{cart.length} {cart.length === 1 ? 'viaje' : 'viajes'}</p>
								</div>
							</div>
							<div className="p-6 overflow-y-auto flex-1 space-y-4">
								{isLoadingCart ? (
									<div className="text-center py-12">
										<div className="inline-block animate-spin rounded-full h-12 w-12 border-4 border-accent border-t-transparent mb-4"></div>
										<p className="text-sm text-neutral-400">Cargando...</p>
									</div>
								) : cart.length > 0 ? (
									<>
										{cart.map((item) => (
											<div key={item.ticketId} className="bg-gradient-to-br from-neutral-900/80 to-neutral-800/80 border border-white/10 rounded-2xl p-5 text-white hover:border-accent/30 transition-all duration-300 group">
												<div className="flex justify-between items-start gap-3">
													<div className="space-y-2 text-sm flex-1">
														<p className="font-bold text-base group-hover:text-accent transition-colors">{item.route.origin} → {item.route.destination}</p>
														<p className="text-xs text-neutral-300 flex items-center gap-1">
															<svg className="w-3 h-3" fill="none" stroke="currentColor" viewBox="0 0 24 24">
																<path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 8v4l3 3m6-3a9 9 0 11-18 0 9 9 0 0118 0z" />
															</svg>
															{formatDateTime(item.trip.departureAt)}
														</p>
														{item.selectedSeat && (
															<p className="text-xs text-neutral-300 flex items-center gap-1">
																<svg className="w-3 h-3" fill="none" stroke="currentColor" viewBox="0 0 24 24">
																	<path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M5 13l4 4L19 7" />
																</svg>
																Asiento: <span className="text-accent font-bold">{item.selectedSeat}</span>
															</p>
														)}
													</div>
													<button onClick={() => removeFromCart(item.ticketId)} disabled={isProcessingPayment} className="text-red-400 hover:text-red-300 disabled:opacity-40 p-2 hover:bg-red-400/10 rounded-xl transition-all">
														<svg className="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
															<path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M6 18L18 6M6 6l12 12" />
														</svg>
													</button>
												</div>
											</div>
										))}
										<div className="pt-4 border-t border-white/10 flex flex-col gap-4 text-white/90">
											<div className="flex items-center justify-between text-sm bg-gradient-to-br from-white/5 to-white/[0.02] p-4 rounded-2xl border border-white/10">
												<span className="font-semibold">Total</span>
												<span className="text-2xl font-bold bg-gradient-to-r from-accent to-accent-light bg-clip-text text-transparent">${cart.reduce((sum, item) => sum + (item.route.pricePerKm * item.route.distanceKm), 0).toFixed(2)}</span>
											</div>
											<button onClick={handlePayAllTickets} disabled={isProcessingPayment} className="w-full bg-gradient-to-r from-accent to-accent-light text-white font-bold py-4 rounded-2xl shadow-xl shadow-accent/40 hover:shadow-2xl hover:scale-[1.02] disabled:opacity-50 disabled:cursor-not-allowed transition-all duration-300 text-sm">
												{isProcessingPayment ? (
													<span className="flex items-center justify-center gap-2">
														<svg className="animate-spin h-4 w-4" fill="none" viewBox="0 0 24 24">
															<circle className="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" strokeWidth="4"></circle>
															<path className="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"></path>
														</svg>
														Procesando...
													</span>
												) : `Pagar todo (${cart.length})`}
											</button>
										</div>
									</>
								) : (
									<div className="text-center py-16 text-white/60 border border-dashed border-white/10 rounded-2xl bg-gradient-to-br from-white/[0.02] to-transparent">
										<svg className="w-14 h-14 mx-auto text-white/20 mb-3" fill="none" stroke="currentColor" viewBox="0 0 24 24">
											<path strokeLinecap="round" strokeLinejoin="round" strokeWidth={1.5} d="M3 3h2l.4 2M7 13h10l4-8H5.4M7 13L5.4 5M7 13l-2.293 2.293c-.63.63-.184 1.707.707 1.707H17m0 0a2 2 0 100 4 2 2 0 000-4zm-8 2a2 2 0 11-4 0 2 2 0 014 0z" />
										</svg>
										<p className="text-sm font-semibold mb-1">Carrito vacío</p>
										<p className="text-xs text-white/40">Agrega viajes para comenzar</p>
									</div>
								)}
							</div>
						</div>
					</aside>
				</div>
			</div>
			{showBookingModal && selectedTrip && (<BookingModal trip={selectedTrip.trip} route={selectedTrip.route} stops={selectedTrip.stops} onClose={() => { setShowBookingModal(false); setSelectedTrip(null); }} onComplete={handleBookingComplete} />)}
		</div>
	);
};

export default Home;