import { FunctionComponent } from 'preact';
import { useState, useEffect } from 'preact/hooks';
import { useLocation } from 'preact-iso';
import { BusSearchBar, SearchParams } from '../../components/BusSearchBar';
import { BookingModal } from '../../components/BookingModal';
import { TripAPI, TripResponse, PassengerType } from '../../api';
import { RouteResponse, StopResponse } from '../../api/types/Transport';

interface CartItem {
	trip: TripResponse;
	route: RouteResponse;
	stops: StopResponse[];
	passengerType?: PassengerType;
	selectedSeat?: string;
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

	// State for initial search params from URL
	const [initialOrigin, setInitialOrigin] = useState('');
	const [initialDestination, setInitialDestination] = useState('');
	const [initialDate, setInitialDate] = useState('');

	// Check authentication status
	useEffect(() => {
		const token = localStorage.getItem('authToken');
		setIsAuthenticated(!!token);
	}, []);

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

	const handleBookingComplete = (tripId: number, seat: string, passengerType: PassengerType) => {
		if (!selectedTrip) return;

		const newCartItem: CartItem = {
			trip: selectedTrip.trip,
			route: selectedTrip.route,
			stops: selectedTrip.stops,
			passengerType: passengerType,
			selectedSeat: seat
		};

		setCart([...cart, newCartItem]);
		setShowBookingModal(false);
		setSelectedTrip(null);
	};

	const removeFromCart = (tripId: number) => {
		setCart(cart.filter(item => item.trip.id !== tripId));
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
		<div className="min-h-screen bg-gradient-to-br from-gray-900 via-gray-800 to-gray-900">
			{/* Header */}
			<div className="bg-gradient-to-r from-gray-800 to-gray-900 shadow-xl border-b border-gray-700">
				<div className="max-w-7xl mx-auto px-4 py-8">
					<div className="flex items-center space-x-3">
						{/* Logo/Icon */}
						<div className="bg-gradient-to-br from-amber-500 to-orange-600 rounded-xl p-3 shadow-lg">
							<svg className="w-8 h-8 text-white" fill="none" stroke="currentColor" viewBox="0 0 24 24">
								<path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M13 10V3L4 14h7v7l9-11h-7z" />
							</svg>
						</div>
						{/* Title */}
						<div>
							<h1 className="text-4xl font-extrabold bg-gradient-to-r from-amber-400 via-orange-500 to-amber-600 bg-clip-text text-transparent">
								InterMuni<span className="text-white">Fast</span>
							</h1>
							<p className="text-gray-400 text-sm mt-1">Viaja cómodo y seguro entre ciudades</p>
						</div>
					</div>
				</div>
			</div>

			{/* Search Bar */}
			<div className="max-w-7xl mx-auto px-4 -mt-8">
				<div className="bg-gradient-to-r from-amber-500 to-orange-600 rounded-2xl shadow-2xl p-6">
					<BusSearchBar
						onSubmit={handleSearch}
						onMobileClick={openMobileSearchModal}
						initialOrigin={initialOrigin}
						initialDestination={initialDestination}
						initialDate={initialDate}
					/>
				</div>
			</div>

			{/* Main Content */}
			<div className="max-w-7xl mx-auto px-4 py-8">
				<div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
					{/* Results Section */}
					<div className="lg:col-span-2">
						{results.length > 0 ? (
							<div className="space-y-4">
								<div className="flex items-center justify-between mb-6">
									<h2 className="text-2xl font-bold text-white">
										{results.length} viajes disponibles
									</h2>
								</div>

								{results.map((trip) => {
									const route = routes.find(r => r.id === trip.routeId);
									const routeStops = stops.filter(s => s.routeId === trip.routeId);
									const isInCart = cart.some(item => item.trip.id === trip.id);

									if (!route) return null;

									return (
										<div
											key={trip.id}
											className="bg-gradient-to-br from-gray-800 to-gray-900 rounded-2xl shadow-xl overflow-hidden border border-gray-700 hover:border-amber-500 transition-all duration-300 hover:shadow-2xl hover:shadow-amber-500/20"
										>
											{/* Trip Header */}
											<div className="bg-gradient-to-r from-amber-600 to-orange-700 px-6 py-3">
												<div className="flex items-center justify-between">
													<div className="flex items-center space-x-3">
														<div className="bg-white/20 rounded-lg p-2">
															<svg className="w-5 h-5 text-white" fill="none" stroke="currentColor" viewBox="0 0 24 24">
																<path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M8 7V3m8 4V3m-9 8h10M5 21h14a2 2 0 002-2V7a2 2 0 00-2-2H5a2 2 0 00-2 2v12a2 2 0 002 2z" />
															</svg>
														</div>
														<div>
															<p className="text-white font-medium">{route.code}</p>
															<p className="text-amber-100 text-sm">{route.name}</p>
														</div>
													</div>
												</div>
											</div>

											{/* Trip Content */}
											<div className="p-6">
												{/* Route */}
												<div className="flex items-center justify-between mb-6">
													<div className="flex-1">
														<div className="flex items-center space-x-4">
															<div className="text-center">
																<p className="text-3xl font-bold text-white">
																	{formatTime(trip.departureAt)}
																</p>
																<p className="text-sm text-gray-400 mt-1">
																	{route.origin}
																</p>
															</div>

															<div className="flex-1 relative">
																<div className="h-0.5 bg-gradient-to-r from-amber-500 to-orange-600"></div>
																<div className="absolute top-1/2 left-1/2 transform -translate-x-1/2 -translate-y-1/2 bg-gray-800 px-3 py-1 rounded-full border border-amber-500">
																	<p className="text-xs text-amber-400 font-medium">
																		{route.durationMinutes} min
																	</p>
																</div>
															</div>

															<div className="text-center">
																<p className="text-3xl font-bold text-white">
																	{formatTime(trip.arrivalAt)}
																</p>
																<p className="text-sm text-gray-400 mt-1">
																	{route.destination}
																</p>
															</div>
														</div>
													</div>
												</div>

												{/* Details */}
												<div className="grid grid-cols-2 gap-4 mb-6">
													<div className="bg-gray-900/50 rounded-lg p-3 border border-gray-700">
														<div className="flex items-center space-x-2">
															<svg className="w-5 h-5 text-amber-500" fill="none" stroke="currentColor" viewBox="0 0 24 24">
																<path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M13 7h8m0 0v8m0-8l-8 8-4-4-6 6" />
															</svg>
															<div>
																<p className="text-xs text-gray-400">Distancia</p>
																<p className="text-sm font-semibold text-white">{route.distanceKm} km</p>
															</div>
														</div>
													</div>

													<div className="bg-gray-900/50 rounded-lg p-3 border border-gray-700">
														<div className="flex items-center space-x-2">
															<svg className="w-5 h-5 text-green-500" fill="none" stroke="currentColor" viewBox="0 0 24 24">
																<path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 8c-1.657 0-3 .895-3 2s1.343 2 3 2 3 .895 3 2-1.343 2-3 2m0-8c1.11 0 2.08.402 2.599 1M12 8V7m0 1v8m0 0v1m0-1c-1.11 0-2.08-.402-2.599-1" />
															</svg>
															<div>
																<p className="text-xs text-gray-400">Precio estimado</p>
																<p className="text-sm font-semibold text-white">
																	${(route.pricePerKm * route.distanceKm).toFixed(2)}
																</p>
															</div>
														</div>
													</div>
												</div>

												{/* Stops */}
												{routeStops.length > 0 && (
													<div className="mb-6">
														<details className="group">
															<summary className="cursor-pointer list-none">
																<div className="flex items-center justify-between py-2 px-3 bg-gray-900/50 rounded-lg border border-gray-700 hover:border-amber-500 transition-colors">
																	<div className="flex items-center space-x-2">
																		<svg className="w-5 h-5 text-amber-500" fill="none" stroke="currentColor" viewBox="0 0 24 24">
																			<path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M17.657 16.657L13.414 20.9a1.998 1.998 0 01-2.827 0l-4.244-4.243a8 8 0 1111.314 0z" />
																			<path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M15 11a3 3 0 11-6 0 3 3 0 016 0z" />
																		</svg>
																		<span className="text-sm font-medium text-white">
																			{routeStops.length} paradas
																		</span>
																	</div>
																	<svg className="w-5 h-5 text-gray-400 group-open:rotate-180 transition-transform" fill="none" stroke="currentColor" viewBox="0 0 24 24">
																		<path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M19 9l-7 7-7-7" />
																	</svg>
																</div>
															</summary>
															<div className="mt-3 space-y-2 pl-4">
																{routeStops.map((stop, index) => (
																	<div key={stop.id} className="flex items-center space-x-3 py-2">
																		<div className="flex items-center">
																			<div className="w-8 h-8 rounded-full bg-amber-500/20 border-2 border-amber-500 flex items-center justify-center">
																				<span className="text-xs font-bold text-amber-500">
																					{stop.sequence}
																				</span>
																			</div>
																			{index < routeStops.length - 1 && (
																				<div className="h-8 w-0.5 bg-amber-500/30 ml-4"></div>
																			)}
																		</div>
																		<span className="text-sm text-gray-300">
																			{stop.name}
																		</span>
																	</div>
																))}
															</div>
														</details>
													</div>
												)}

												{/* Action Button */}
												<button
													onClick={() => isInCart ? removeFromCart(trip.id) : addToCart(trip, route, routeStops)}
													className={`w-full py-4 rounded-xl font-semibold transition-all duration-300 ${isInCart
														? 'bg-red-600 hover:bg-red-700 text-white shadow-lg shadow-red-500/30'
														: 'bg-gradient-to-r from-amber-500 to-orange-600 hover:from-amber-600 hover:to-orange-700 text-white shadow-lg shadow-amber-500/30'
														}`}
												>
													{isInCart ? (
														<span className="flex items-center justify-center space-x-2">
															<svg className="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
																<path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M19 7l-.867 12.142A2 2 0 0116.138 21H7.862a2 2 0 01-1.995-1.858L5 7m5 4v6m4-6v6m1-10V4a1 1 0 00-1-1h-4a1 1 0 00-1 1v3M4 7h16" />
															</svg>
															<span>Remover del carrito</span>
														</span>
													) : (
														<span className="flex items-center justify-center space-x-2">
															<svg className="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
																<path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M3 3h2l.4 2M7 13h10l4-8H5.4M7 13L5.4 5M7 13l-2.293 2.293c-.63.63-.184 1.707.707 1.707H17m0 0a2 2 0 100 4 2 2 0 000-4zm-8 2a2 2 0 11-4 0 2 2 0 014 0z" />
															</svg>
															<span>Reservar asiento</span>
														</span>
													)}
												</button>
											</div>
										</div>
									);
								})}
							</div>
						) : (
							<div className="bg-gray-800 rounded-2xl p-12 text-center border border-gray-700">
								<svg className="w-24 h-24 mx-auto text-gray-600 mb-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
									<path strokeLinecap="round" strokeLinejoin="round" strokeWidth={1.5} d="M8 16l-4-4m0 0l4-4m-4 4h16m-4-4v8M4 20h16a2 2 0 002-2V6a2 2 0 00-2-2H4a2 2 0 00-2 2v12a2 2 0 002 2z" />
								</svg>
								<h3 className="text-xl font-semibold text-white mb-2">
									No hay viajes disponibles
								</h3>
								<p className="text-gray-400">
									Busca viajes usando el formulario de arriba
								</p>
							</div>
						)}
					</div>

					{/* Cart Section */}
					<div className="lg:col-span-1">
						<div className="sticky top-4">
							<div className="bg-gradient-to-br from-gray-800 to-gray-900 rounded-2xl shadow-xl border border-gray-700 overflow-hidden">
								{/* Cart Header */}
								<div className="bg-gradient-to-r from-amber-600 to-orange-700 px-6 py-4">
									<div className="flex items-center justify-between">
										<div className="flex items-center space-x-3">
											<div className="bg-white/20 rounded-lg p-2">
												<svg className="w-6 h-6 text-white" fill="none" stroke="currentColor" viewBox="0 0 24 24">
													<path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M3 3h2l.4 2M7 13h10l4-8H5.4M7 13L5.4 5M7 13l-2.293 2.293c-.63.63-.184 1.707.707 1.707H17m0 0a2 2 0 100 4 2 2 0 000-4zm-8 2a2 2 0 11-4 0 2 2 0 014 0z" />
												</svg>
											</div>
											<div>
												<h3 className="text-lg font-bold text-white">Mi Carrito</h3>
												<p className="text-amber-100 text-sm">{cart.length} {cart.length === 1 ? 'viaje' : 'viajes'}</p>
											</div>
										</div>
									</div>
								</div>

								{/* Cart Content */}
								<div className="p-6">
									{cart.length > 0 ? (
										<div className="space-y-4">
											{cart.map((item) => (
												<div key={item.trip.id} className="bg-gray-900/50 rounded-lg p-4 border border-gray-700">
													<div className="flex justify-between items-start mb-2">
														<div className="flex-1">
															<p className="text-white font-medium text-sm">
																{item.route.origin} → {item.route.destination}
															</p>
															<p className="text-gray-400 text-xs mt-1">
																{formatDateTime(item.trip.departureAt)}
															</p>
														</div>
														<button
															onClick={() => removeFromCart(item.trip.id)}
															className="text-red-400 hover:text-red-300 transition-colors"
														>
															<svg className="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
																<path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M6 18L18 6M6 6l12 12" />
															</svg>
														</button>
													</div>
													{item.selectedSeat && (
														<div className="mt-2 text-xs text-gray-400">
															Asiento: <span className="text-amber-400 font-medium">{item.selectedSeat}</span>
														</div>
													)}
													{item.passengerType && (
														<div className="mt-1 text-xs text-gray-400">
															Tipo: <span className="text-amber-400 font-medium">
																{item.passengerType === 'ADULT' ? 'Adulto' : item.passengerType === 'CHILD' ? 'Niño' : 'Anciano'}
															</span>
														</div>
													)}
												</div>
											))}

											<div className="pt-4 border-t border-gray-700">
												<div className="flex justify-between items-center mb-4">
													<span className="text-white font-semibold">Total estimado</span>
													<span className="text-2xl font-bold text-amber-500">
														${cart.reduce((sum, item) => sum + (item.route.pricePerKm * item.route.distanceKm), 0).toFixed(2)}
													</span>
												</div>
												<button className="w-full bg-gradient-to-r from-green-600 to-emerald-700 hover:from-green-700 hover:to-emerald-800 text-white font-semibold py-3 rounded-xl shadow-lg shadow-green-500/30 transition-all duration-300">
													Proceder al pago
												</button>
											</div>
										</div>
									) : (
										<div className="text-center py-8">
											<svg className="w-16 h-16 mx-auto text-gray-600 mb-3" fill="none" stroke="currentColor" viewBox="0 0 24 24">
												<path strokeLinecap="round" strokeLinejoin="round" strokeWidth={1.5} d="M3 3h2l.4 2M7 13h10l4-8H5.4M7 13L5.4 5M7 13l-2.293 2.293c-.63.63-.184 1.707.707 1.707H17m0 0a2 2 0 100 4 2 2 0 000-4zm-8 2a2 2 0 11-4 0 2 2 0 014 0z" />
											</svg>
											<p className="text-gray-400 text-sm">
												Tu carrito está vacío
											</p>
											<p className="text-gray-500 text-xs mt-2">
												Agrega viajes para comenzar
											</p>
										</div>
									)}
								</div>
							</div>
						</div>
					</div>
				</div>
			</div>

			{/* Booking Modal */}
			{showBookingModal && selectedTrip && (
				<BookingModal
					trip={selectedTrip.trip}
					route={selectedTrip.route}
					stops={selectedTrip.stops}
					onClose={() => {
						setShowBookingModal(false);
						setSelectedTrip(null);
					}}
					onComplete={handleBookingComplete}
				/>
			)}
		</div>
	);
};

export default Home;
