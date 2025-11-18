import { FunctionComponent } from 'preact';
import { useState, useEffect } from 'preact/hooks';
import {
    TripResponse,
    SeatResponse,
    PassengerType,
    PaymentMethod,
    CreateTicketRequest,
    CreateBaggageRequest,
    CreateSeatHoldRequest
} from '../api/types/Booking';
import { RouteResponse, StopResponse } from '../api/types/Transport';
import { TripAPI, SeatHoldAPI, TicketAPI, BaggageAPI } from '../api';

interface BookingModalProps {
    trip: TripResponse;
    route: RouteResponse;
    stops: StopResponse[];
    onClose: () => void;
    onComplete: (tripId: number, seat: string, passengerType: PassengerType, ticketId: number, baggageId: number | null) => void;
}

type BookingStep = 'passenger' | 'seat' | 'baggage' | 'payment' | 'confirmation';

interface BaggageInfo {
    weightKg: number;
    estimatedFee: number;
}

export const BookingModal: FunctionComponent<BookingModalProps> = ({
    trip,
    route,
    stops,
    onClose,
    onComplete
}) => {
    const [step, setStep] = useState<BookingStep>('passenger');
    const [passengerType, setPassengerType] = useState<PassengerType>('ADULT');
    const [selectedSeat, setSelectedSeat] = useState<string | null>(null);
    const [seats, setSeats] = useState<SeatResponse[]>([]);
    const [fromStop, setFromStop] = useState<number | null>(null);
    const [toStop, setToStop] = useState<number | null>(null);
    const [baggage, setBaggage] = useState<BaggageInfo | null>(null);
    const [paymentMethod, setPaymentMethod] = useState<PaymentMethod>('CARD');
    const [isProcessing, setIsProcessing] = useState(false);
    const [seatHoldId, setSeatHoldId] = useState<number | null>(null);
    const [expiresAt, setExpiresAt] = useState<Date | null>(null);
    const [timeRemaining, setTimeRemaining] = useState<number>(600);

    useEffect(() => {
        loadSeats();
    }, []);

    useEffect(() => {
        if (expiresAt) {
            const interval = setInterval(() => {
                const now = new Date();
                const remaining = Math.max(0, Math.floor((expiresAt.getTime() - now.getTime()) / 1000));
                setTimeRemaining(remaining);

                if (remaining === 0) {
                    alert('Tu reserva ha expirado. Por favor, intenta de nuevo.');
                    onClose();
                }
            }, 1000);

            return () => clearInterval(interval);
        }
    }, [expiresAt]);

    const loadSeats = async () => {
        try {
            const response = await TripAPI.getSeats(undefined, {
                pathParams: { id: trip.id.toString() }
            });
            setSeats(response.data);
        } catch (error) {
            console.error('Error loading seats:', error);
        }
    };

    const calculateBaggageFee = (weightKg: number): number => {
        const MAX_FREE_WEIGHT = 25;
        const basePrice = route.pricePerKm * route.distanceKm;

        if (weightKg <= MAX_FREE_WEIGHT) {
            return 0;
        }

        const extraWeight = weightKg - MAX_FREE_WEIGHT;
        return extraWeight * (basePrice * 0.03);
    };

    const calculatePrice = (): number => {
        let basePrice = route.pricePerKm * route.distanceKm;

        switch (passengerType) {
            case 'CHILD':
                basePrice *= 0.5;
                break;
            case 'SENIOR':
                basePrice *= 0.7;
                break;
            case 'STUDENT':
                basePrice *= 0.8;
                break;
        }

        if (baggage) {
            basePrice += baggage.estimatedFee;
        }

        return basePrice;
    };

    const handleSelectSeat = async (seatNumber: string) => {
        setSelectedSeat(seatNumber);
    };

    const handleCreateSeatHold = async () => {
        if (!selectedSeat) return;

        setIsProcessing(true);
        try {
            const expirationTime = new Date();
            expirationTime.setMinutes(expirationTime.getMinutes() + 10);

            const seatHoldRequest: CreateSeatHoldRequest = {
                seatNumber: selectedSeat,
                tripId: trip.id,
                fromStopId: fromStop,
                toStopId: toStop,
                expiresAt: expirationTime.toISOString()
            };

            const response = await SeatHoldAPI.create(seatHoldRequest);
            setSeatHoldId(response.data.id);
            setExpiresAt(expirationTime);
            setStep('baggage');
        } catch (error) {
            console.error('Error creating seat hold:', error);
            alert('Error al reservar el asiento. Por favor, intenta de nuevo.');
        } finally {
            setIsProcessing(false);
        }
    };

    const handleSkipBaggage = () => {
        setBaggage(null);
        setStep('payment');
    };

    const handleAddBaggage = (weightKg: number) => {
        const fee = calculateBaggageFee(weightKg);
        setBaggage({ weightKg, estimatedFee: fee });
        setStep('payment');
    };

    const handleAddToCart = async () => {
        if (!selectedSeat) return;

        setIsProcessing(true);
        try {
            const ticketRequest: CreateTicketRequest = {
                seatNumber: selectedSeat,
                tripId: trip.id,
                fromStopId: fromStop,
                toStopId: toStop,
                paymentMethod: paymentMethod,
                paymentIntentId: null,
                passengerType: passengerType
            };

            const ticketResponse = await TicketAPI.create(ticketRequest);

            let baggageId = null;
            if (baggage && baggage.weightKg > 0) {
                const baggageRequest: CreateBaggageRequest = {
                    weightKg: baggage.weightKg,
                    tagCode: `BAG-${ticketResponse.data.id}-${Date.now()}`,
                    ticketId: ticketResponse.data.id
                };

                const baggageResponse = await BaggageAPI.create(baggageRequest);
                baggageId = baggageResponse.data.id;
            }

            if (seatHoldId) {
                await SeatHoldAPI.delete(undefined, {
                    pathParams: { id: seatHoldId.toString() }
                });
            }

            onComplete(trip.id, selectedSeat, passengerType, ticketResponse.data.id, baggageId);
            onClose();
        } catch (error) {
            console.error('Error creating ticket:', error);
            alert('Error al agregar al carrito. Por favor, intenta de nuevo.');
        } finally {
            setIsProcessing(false);
        }
    };

    const formatTime = (seconds: number): string => {
        const mins = Math.floor(seconds / 60);
        const secs = seconds % 60;
        return `${mins}:${secs.toString().padStart(2, '0')}`;
    };

    const renderPassengerStep = () => (
        <div className="p-6">
            <h3 className="text-xl font-bold text-white mb-6">Selecciona el tipo de pasajero</h3>

            <div className="space-y-3">
                {(['ADULT', 'CHILD', 'SENIOR', 'STUDENT'] as PassengerType[]).map((type) => (
                    <button
                        key={type}
                        onClick={() => setPassengerType(type)}
                        className={`w-full p-4 rounded-xl border-2 transition-all duration-200 ${passengerType === type
                            ? 'border-accent bg-accent/10'
                            : 'border-white/10 bg-white/5 hover:border-white/20'
                            }`}
                    >
                        <div className="flex items-center justify-between">
                            <div className="flex items-center space-x-3">
                                <div className={`w-6 h-6 rounded-full border-2 flex items-center justify-center ${passengerType === type ? 'border-accent' : 'border-white/20'
                                    }`}>
                                    {passengerType === type && (
                                        <div className="w-3 h-3 rounded-full bg-accent"></div>
                                    )}
                                </div>
                                <div className="text-left">
                                    <p className="font-semibold text-white">
                                        {type === 'ADULT' && 'Adulto'}
                                        {type === 'CHILD' && 'Niño'}
                                        {type === 'SENIOR' && 'Anciano'}
                                        {type === 'STUDENT' && 'Estudiante'}
                                    </p>
                                    <p className="text-sm text-neutral-400">
                                        {type === 'ADULT' && 'Mayor de 18 años'}
                                        {type === 'CHILD' && 'Menor de 12 años - 50% descuento'}
                                        {type === 'SENIOR' && 'Mayor de 65 años - 30% descuento'}
                                        {type === 'STUDENT' && 'Con credencial vigente - 20% descuento'}
                                    </p>
                                </div>
                            </div>
                        </div>
                    </button>
                ))}
            </div>

            <div className="mt-6 space-y-4">
                <div className="bg-white/5 backdrop-blur-sm rounded-2xl p-5 border border-white/10">
                    <h4 className="text-sm font-semibold text-accent mb-4 flex items-center">
                        <svg className="w-5 h-5 mr-2" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M17.657 16.657L13.414 20.9a1.998 1.998 0 01-2.827 0l-4.244-4.243a8 8 0 1111.314 0z" />
                            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M15 11a3 3 0 11-6 0 3 3 0 016 0z" />
                        </svg>
                        Selecciona tus paradas {stops && stops.length > 0 && `(${stops.length} disponibles)`}
                    </h4>

                    {stops && stops.length > 0 ? (
                        <div className="space-y-4">
                            <div>
                                <label className="block text-sm font-medium text-neutral-300 mb-2">
                                    ¿Dónde subes?
                                </label>
                                <select
                                    value={fromStop || ''}
                                    onChange={(e) => setFromStop(e.currentTarget.value ? Number(e.currentTarget.value) : null)}
                                    className="w-full px-4 py-3 bg-neutral-950 border border-white/10 rounded-xl text-white focus:border-accent focus:ring-2 focus:ring-accent/30 focus:outline-none transition-all"
                                >
                                    <option value="">{route.origin} (Origen)</option>
                                    {stops
                                        .sort((a, b) => a.sequence - b.sequence)
                                        .filter(stop => !toStop || stop.sequence < stops.find(s => s.id === toStop)!.sequence)
                                        .map(stop => (
                                            <option key={stop.id} value={stop.id}>
                                                {stop.name} - Parada {stop.sequence}
                                            </option>
                                        ))}
                                </select>
                                <p className="mt-1 text-xs text-neutral-500">Por defecto: {route.origin}</p>
                            </div>

                            <div>
                                <label className="block text-sm font-medium text-neutral-300 mb-2">
                                    ¿Dónde bajas?
                                </label>
                                <select
                                    value={toStop || ''}
                                    onChange={(e) => setToStop(e.currentTarget.value ? Number(e.currentTarget.value) : null)}
                                    className="w-full px-4 py-3 bg-neutral-950 border border-white/10 rounded-xl text-white focus:border-accent focus:ring-2 focus:ring-accent/30 focus:outline-none transition-all"
                                >
                                    <option value="">{route.destination} (Destino)</option>
                                    {stops
                                        .sort((a, b) => a.sequence - b.sequence)
                                        .filter(stop => !fromStop || stop.sequence > stops.find(s => s.id === fromStop)!.sequence)
                                        .map(stop => (
                                            <option key={stop.id} value={stop.id}>
                                                {stop.name} - Parada {stop.sequence}
                                            </option>
                                        ))}
                                </select>
                                <p className="mt-1 text-xs text-neutral-500">Por defecto: {route.destination}</p>
                            </div>

                            {(fromStop || toStop) && (
                                <div className="mt-4 p-3 bg-white/5 rounded-xl border border-white/10">
                                    <p className="text-xs font-medium text-neutral-400 mb-2">Tu trayecto:</p>
                                    <div className="flex items-center space-x-2 text-sm">
                                        <span className="text-white font-medium">
                                            {fromStop ? stops.find(s => s.id === fromStop)?.name : route.origin}
                                        </span>
                                        <svg className="w-4 h-4 text-accent" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                                            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M13 7l5 5m0 0l-5 5m5-5H6" />
                                        </svg>
                                        <span className="text-white font-medium">
                                            {toStop ? stops.find(s => s.id === toStop)?.name : route.destination}
                                        </span>
                                    </div>
                                </div>
                            )}
                        </div>
                    ) : (
                        <div className="text-center py-4">
                            <p className="text-neutral-400 text-sm">
                                No hay paradas intermedias disponibles para esta ruta.
                            </p>
                            <p className="text-neutral-500 text-xs mt-1">
                                Viajarás de {route.origin} a {route.destination}
                            </p>
                        </div>
                    )}
                </div>
            </div>

            <button
                onClick={() => setStep('seat')}
                className="w-full mt-6 bg-accent hover:bg-accent-dark text-white font-semibold py-4 rounded-xl shadow-lg transition-all duration-200"
            >
                Continuar
            </button>
        </div>
    );

    const renderSeatStep = () => (
        <div className="p-6">
            <h3 className="text-xl font-bold text-white mb-6">Selecciona tu asiento</h3>

            <div className="flex items-center justify-center space-x-6 mb-6 text-sm">
                <div className="flex items-center space-x-2">
                    <div className="w-8 h-8 bg-green-500 rounded"></div>
                    <span className="text-neutral-300">Disponible</span>
                </div>
                <div className="flex items-center space-x-2">
                    <div className="w-8 h-8 bg-accent rounded"></div>
                    <span className="text-neutral-300">Preferencial</span>
                </div>
                <div className="flex items-center space-x-2">
                    <div className="w-8 h-8 bg-neutral-600 rounded"></div>
                    <span className="text-neutral-300">Ocupado</span>
                </div>
            </div>

            <div className="bg-white/5 backdrop-blur-sm rounded-2xl p-6 mb-6 border border-white/10">
                <div className="grid grid-cols-4 gap-3">
                    {seats.map((seat) => {
                        const isSelected = selectedSeat === seat.number;
                        const isPreferential = seat.type === 'PREFERENTIAL';

                        return (
                            <button
                                key={seat.id}
                                onClick={() => handleSelectSeat(seat.number)}
                                className={`h-12 rounded-lg font-semibold text-sm transition-all duration-200 ${isSelected
                                    ? 'bg-blue-500 text-white scale-105 shadow-lg'
                                    : isPreferential
                                        ? 'bg-accent text-white hover:bg-accent-dark'
                                        : 'bg-green-500 text-white hover:bg-green-600'
                                    }`}
                            >
                                {seat.number}
                            </button>
                        );
                    })}
                </div>
            </div>

            {selectedSeat && (
                <div className="bg-accent/10 rounded-xl p-4 mb-6 border border-accent">
                    <p className="text-white text-center">
                        Asiento seleccionado: <span className="font-bold text-accent">{selectedSeat}</span>
                    </p>
                </div>
            )}

            <div className="flex space-x-3">
                <button
                    onClick={() => setStep('passenger')}
                    className="flex-1 bg-white/5 hover:bg-white/10 text-white font-semibold py-4 rounded-xl transition-all duration-200 border border-white/10"
                >
                    Atrás
                </button>
                <button
                    onClick={handleCreateSeatHold}
                    disabled={!selectedSeat || isProcessing}
                    className="flex-1 bg-accent hover:bg-accent-dark text-white font-semibold py-4 rounded-xl shadow-lg transition-all duration-200 disabled:opacity-50 disabled:cursor-not-allowed"
                >
                    {isProcessing ? 'Reservando...' : 'Continuar'}
                </button>
            </div>
        </div>
    );

    const renderBaggageStep = () => {
        const [weight, setWeight] = useState<string>('0');

        return (
            <div className="p-6">
                <h3 className="text-xl font-bold text-white mb-6">¿Llevas equipaje?</h3>

                <div className="bg-accent/10 border border-accent/30 rounded-xl p-4 mb-6">
                    <p className="text-accent text-sm">
                        <strong>Importante:</strong> Los primeros 25kg son gratis. Cada kilo adicional tiene un cargo del 3% del precio del boleto.
                    </p>
                </div>

                <div className="mb-6">
                    <label className="block text-sm font-medium text-neutral-300 mb-2">
                        Peso del equipaje (kg)
                    </label>
                    <input
                        type="number"
                        min="0"
                        step="0.1"
                        value={weight}
                        onChange={(e) => setWeight(e.currentTarget.value)}
                        className="w-full px-4 py-3 bg-neutral-950 border border-white/10 rounded-xl text-white focus:border-accent focus:ring-2 focus:ring-accent/30 focus:outline-none"
                        placeholder="0.0"
                    />
                </div>

                {Number(weight) > 0 && (
                    <div className="bg-white/5 rounded-xl p-4 mb-6 border border-white/10">
                        <div className="flex justify-between items-center">
                            <span className="text-neutral-300">Cargo por equipaje:</span>
                            <span className="text-white font-bold">
                                ${calculateBaggageFee(Number(weight)).toFixed(2)}
                            </span>
                        </div>
                    </div>
                )}

                <div className="flex space-x-3">
                    <button
                        onClick={handleSkipBaggage}
                        className="flex-1 bg-white/5 hover:bg-white/10 text-white font-semibold py-4 rounded-xl transition-all duration-200 border border-white/10"
                    >
                        Sin equipaje
                    </button>
                    <button
                        onClick={() => handleAddBaggage(Number(weight))}
                        disabled={!weight || Number(weight) <= 0}
                        className="flex-1 bg-accent hover:bg-accent-dark text-white font-semibold py-4 rounded-xl shadow-lg transition-all duration-200 disabled:opacity-50 disabled:cursor-not-allowed"
                    >
                        Continuar
                    </button>
                </div>
            </div>
        );
    };

    const renderPaymentStep = () => (
        <div className="p-6">
            <h3 className="text-xl font-bold text-white mb-6">Resumen del ticket</h3>

            {expiresAt && (
                <div className="bg-accent/10 border border-accent/30 rounded-xl p-4 mb-6">
                    <div className="flex items-center justify-between">
                        <p className="text-accent text-sm">
                            Tu reserva expira en:
                        </p>
                        <p className="text-accent font-bold text-lg">
                            {formatTime(timeRemaining)}
                        </p>
                    </div>
                </div>
            )}

            <div className="bg-blue-500/10 border border-blue-500/30 rounded-xl p-4 mb-6">
                <p className="text-blue-300 text-sm">
                    <strong>ℹ️ Nota:</strong> El ticket se agregará al carrito con pago pendiente. Podrás pagar todos tus tickets de una vez al finalizar.
                </p>
            </div>

            <div className="mb-6">
                <label className="block text-sm font-medium text-neutral-300 mb-3">
                    Selecciona tu método de pago preferido
                </label>
                <div className="space-y-3">
                    {(['CARD', 'CASH', 'DIGITAL_WALLET', 'TRANSFER', 'QR'] as PaymentMethod[]).map((method) => (
                        <button
                            key={method}
                            onClick={() => setPaymentMethod(method)}
                            className={`w-full p-4 rounded-xl border-2 transition-all duration-200 ${paymentMethod === method
                                ? 'border-accent bg-accent/10'
                                : 'border-white/10 bg-white/5 hover:border-white/20'
                                }`}
                        >
                            <div className="flex items-center space-x-3">
                                <div className={`w-6 h-6 rounded-full border-2 flex items-center justify-center ${paymentMethod === method ? 'border-accent' : 'border-white/20'
                                    }`}>
                                    {paymentMethod === method && (
                                        <div className="w-3 h-3 rounded-full bg-accent"></div>
                                    )}
                                </div>
                                <p className="font-semibold text-white">
                                    {method === 'CARD' && 'Tarjeta de crédito/débito'}
                                    {method === 'CASH' && 'Efectivo en terminal'}
                                    {method === 'DIGITAL_WALLET' && 'Billetera digital'}
                                    {method === 'TRANSFER' && 'Transferencia bancaria'}
                                    {method === 'QR' && 'Código QR'}
                                </p>
                            </div>
                        </button>
                    ))}
                </div>
            </div>

            <div className="bg-white/5 backdrop-blur-sm rounded-2xl p-5 mb-6 border border-white/10">
                <h4 className="text-white font-semibold mb-4">Resumen</h4>
                <div className="space-y-2 text-sm">
                    <div className="flex justify-between">
                        <span className="text-neutral-400">Ruta:</span>
                        <span className="text-white">{route.origin} → {route.destination}</span>
                    </div>
                    <div className="flex justify-between">
                        <span className="text-neutral-400">Asiento:</span>
                        <span className="text-white">{selectedSeat}</span>
                    </div>
                    <div className="flex justify-between">
                        <span className="text-neutral-400">Tipo de pasajero:</span>
                        <span className="text-white">
                            {passengerType === 'ADULT' && 'Adulto'}
                            {passengerType === 'CHILD' && 'Niño'}
                            {passengerType === 'SENIOR' && 'Anciano'}
                            {passengerType === 'STUDENT' && 'Estudiante'}
                        </span>
                    </div>
                    {baggage && (
                        <div className="flex justify-between">
                            <span className="text-neutral-400">Equipaje:</span>
                            <span className="text-white">{baggage.weightKg} kg (${baggage.estimatedFee.toFixed(2)})</span>
                        </div>
                    )}
                    <div className="border-t border-white/10 pt-3 mt-3">
                        <div className="flex justify-between items-center">
                            <span className="text-white font-semibold">Total estimado:</span>
                            <span className="text-2xl font-bold text-accent">
                                ${calculatePrice().toFixed(2)}
                            </span>
                        </div>
                    </div>
                </div>
            </div>

            <div className="flex space-x-3">
                <button
                    onClick={() => setStep('baggage')}
                    disabled={isProcessing}
                    className="flex-1 bg-white/5 hover:bg-white/10 text-white font-semibold py-4 rounded-xl transition-all duration-200 disabled:opacity-50 border border-white/10"
                >
                    Atrás
                </button>
                <button
                    onClick={handleAddToCart}
                    disabled={isProcessing}
                    className="flex-1 bg-accent hover:bg-accent-dark text-white font-semibold py-4 rounded-xl shadow-lg transition-all duration-200 disabled:opacity-50 disabled:cursor-not-allowed"
                >
                    {isProcessing ? 'Agregando...' : 'Agregar al Carrito'}
                </button>
            </div>
        </div>
    );

    return (
        <div className="fixed inset-0 bg-black/90 backdrop-blur-lg z-50 flex items-center justify-center p-4 animate-in fade-in duration-300">
            <div className="bg-gradient-to-br from-neutral-900/98 to-neutral-900/95 backdrop-blur-2xl rounded-3xl max-w-2xl w-full max-h-[90vh] overflow-auto border border-white/20 shadow-2xl shadow-black/80 animate-in slide-in-from-bottom-4 duration-300">
                <div className="bg-gradient-to-r from-accent via-accent-dark to-accent px-7 py-5 flex justify-between items-center sticky top-0 z-10 border-b border-white/10 shadow-xl">
                    <div>
                        <h2 className="text-2xl font-bold text-white">Reservar Viaje</h2>
                        <p className="text-white/90 text-sm font-medium flex items-center gap-2 mt-1">
                            <svg className="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M17.657 16.657L13.414 20.9a1.998 1.998 0 01-2.827 0l-4.244-4.243a8 8 0 1111.314 0z" />
                            </svg>
                            {route.origin} → {route.destination}
                        </p>
                    </div>
                    <button
                        onClick={onClose}
                        className="text-white hover:bg-white/20 transition-all rounded-xl p-2"
                        disabled={isProcessing}
                    >
                        <svg className="w-6 h-6" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M6 18L18 6M6 6l12 12" />
                        </svg>
                    </button>
                </div>

                {step !== 'confirmation' && (
                    <div className="px-7 pt-6">
                        <div className="flex items-center justify-between mb-3">
                            {['passenger', 'seat', 'baggage', 'payment'].map((s, index) => (
                                <div key={s} className="flex items-center flex-1">
                                    <div className={`w-10 h-10 rounded-full flex items-center justify-center text-sm font-bold transition-all duration-300 ${step === s
                                        ? 'bg-gradient-to-br from-accent to-accent-light text-white shadow-lg shadow-accent/50 scale-110'
                                        : ['passenger', 'seat', 'baggage', 'payment'].indexOf(step) > index
                                            ? 'bg-gradient-to-br from-emerald-500 to-emerald-600 text-white shadow-lg shadow-emerald-500/30'
                                            : 'bg-white/10 text-neutral-400 border border-white/20'
                                        }`}>
                                        {['passenger', 'seat', 'baggage', 'payment'].indexOf(step) > index ? (
                                            <svg className="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                                                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M5 13l4 4L19 7" />
                                            </svg>
                                        ) : (
                                            index + 1
                                        )}
                                    </div>
                                    {index < 3 && (
                                        <div className={`flex-1 h-1.5 mx-2 rounded-full transition-all duration-300 ${['passenger', 'seat', 'baggage', 'payment'].indexOf(step) > index
                                            ? 'bg-gradient-to-r from-emerald-500 to-emerald-600'
                                            : 'bg-white/10'
                                            }`}></div>
                                    )}
                                </div>
                            ))}
                        </div>
                        <div className="flex justify-between text-xs text-neutral-400 mb-6 font-semibold">
                            <span>Pasajero</span>
                            <span>Asiento</span>
                            <span>Equipaje</span>
                            <span>Pago</span>
                        </div>
                    </div>
                )}

                {step === 'passenger' && renderPassengerStep()}
                {step === 'seat' && renderSeatStep()}
                {step === 'baggage' && renderBaggageStep()}
                {step === 'payment' && renderPaymentStep()}
            </div>
        </div>
    );
};

export default BookingModal;
