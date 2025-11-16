import { FunctionComponent } from 'preact';
import { useState, useEffect } from 'preact/hooks';
import { Search } from 'lucide-preact';

export interface SearchParams {
    origin: string | null;
    destination: string | null;
    date: string | null;
}

interface BusSearchBarProps {
    onSubmit: (params: SearchParams) => void;
    onMobileClick?: () => void;
    initialOrigin?: string;
    initialDestination?: string;
    initialDate?: string;
}

export const BusSearchBar: FunctionComponent<BusSearchBarProps> = ({
    onSubmit,
    onMobileClick,
    initialOrigin = '',
    initialDestination = '',
    initialDate = ''
}) => {
    const [origin, setOrigin] = useState(initialOrigin);
    const [destination, setDestination] = useState(initialDestination);
    const [date, setDate] = useState(initialDate);

    // Update state when initial values change (from URL params)
    useEffect(() => {
        setOrigin(initialOrigin);
        setDestination(initialDestination);
        setDate(initialDate);
    }, [initialOrigin, initialDestination, initialDate]);

    const handleOrigin = (e: Event) => {
        setOrigin((e.currentTarget as HTMLInputElement).value);
    };

    const handleDestination = (e: Event) => {
        setDestination((e.currentTarget as HTMLInputElement).value);
    };

    const handleDate = (e: Event) => {
        setDate((e.currentTarget as HTMLInputElement).value);
    };

    const handleSearchSubmit = () => {
        onSubmit({ origin, destination, date });
    };

    const handleMobileTrigger = () => {
        if (onMobileClick) {
            onMobileClick();
        } else {
            console.log('Botón móvil clickeado. Pasa una prop "onMobileClick" para manejar la apertura de un modal.');
        }
    };

    return (
        <div className="w-full max-w-4xl mx-auto px-4">
            <div className="md:hidden">
                <button
                    onClick={handleMobileTrigger} // Lógica de ac
                    className="w-full flex items-center gap-3 p-3 bg-white rounded-full shadow-lg hover:shadow-xl transition-shadow"
                >
                    <Search className="h-6 w-6 text-green-600 ml-1" />
                    <div className="text-left">
                        <p className="font-bold text-gray-900">Encuentra tu viaje</p>
                        <p className="text-sm text-gray-500">
                            Busca por origen o destino
                        </p>
                    </div>
                </button>
            </div>

            <div className="hidden md:flex items-center bg-white rounded-full shadow-lg overflow-hidden border border-gray-200 divide-x divide-gray-200">

                {/* Sección Origen (Controlada) */}
                <div className="flex-1 relative">
                    <label
                        htmlFor="origin"
                        className="absolute top-2 left-6 text-xs font-bold text-gray-700"
                    >
                        Origen
                    </label>
                    <input
                        id="origin"
                        type="text"
                        placeholder="¿Desde dónde viajas?"
                        className="w-full pl-6 pr-4 pt-5 pb-3 rounded-l-full text-sm text-gray-600 placeholder-gray-400 focus:outline-none"
                        value={origin}
                        onInput={handleOrigin}
                    />
                </div>

                {/* Sección Destino (Controlada) */}
                <div className="flex-1 relative">
                    <label
                        htmlFor="destination"
                        className="absolute top-2 left-6 text-xs font-bold text-gray-700"
                    >
                        Destino
                    </label>
                    <input
                        id="destination"
                        type="text"
                        placeholder="¿A dónde vas?"
                        className="w-full pl-6 pr-4 pt-5 pb-3 text-sm text-gray-600 placeholder-gray-400 focus:outline-none"
                        value={destination}
                        onInput={handleDestination}
                    />
                </div>

                {/* Sección Fecha (Controlada) */}
                <div className="flex-auto relative">
                    <label
                        htmlFor="date"
                        className="absolute top-2 left-6 text-xs font-bold text-gray-700"
                    >
                        Fecha
                    </label>
                    <input
                        id="date"
                        type="text"
                        placeholder="Selecciona la fecha"
                        onFocus={(e) => (e.currentTarget.type = 'date')}
                        onBlur={(e) => (e.currentTarget.type = 'text')}
                        className="w-full pl-6 pr-4 pt-5 pb-3 text-sm text-gray-600 placeholder-gray-400 focus:outline-none"
                        value={date}
                        onInput={handleDate}
                    />
                </div>

                {/* Botón de Búsqueda */}
                <div className="p-2">
                    <button
                        onClick={handleSearchSubmit} // Lógica de envío
                        className="flex items-center justify-center bg-green-500 hover:bg-green-600 text-white font-bold rounded-full h-12 w-12 transition-colors"
                        aria-label="Buscar"
                    >
                        <Search className="h-5 w-5" />
                    </button>
                </div>
            </div>
        </div>
    );
};