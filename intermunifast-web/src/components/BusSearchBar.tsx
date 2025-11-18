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
        <div className="w-full max-w-5xl mx-auto px-4">
            {/* --- Vista Móvil --- */}
            <div className="md:hidden">
                <button
                    onClick={handleMobileTrigger}
                    className="w-full flex items-center gap-3 p-4 bg-neutral-800/50 backdrop-blur-sm border border-neutral-700 rounded-2xl hover:bg-neutral-800 transition-all"
                >
                    <Search className="h-5 w-5 text-neutral-300" />
                    <div className="text-left">
                        <p className="font-semibold text-neutral-100 text-sm">¿A dónde quieres ir?</p>
                        <p className="text-xs text-neutral-400">Toca para buscar tu viaje</p>
                    </div>
                </button>
            </div>

            {/* --- Vista de Escritorio --- */}
            <div className="hidden md:flex items-stretch bg-neutral-900/50 backdrop-blur-sm border border-neutral-700 rounded-2xl shadow-2xl overflow-hidden">
                {/* Sección Origen */}
                <div className="flex-1 px-5 py-3 group">
                    <label htmlFor="origin" className="block text-xs font-semibold text-neutral-400 group-hover:text-white transition-colors">
                        Desde
                    </label>
                    <input
                        id="origin"
                        type="text"
                        placeholder="Ciudad de origen"
                        className="w-full pt-1 text-base font-medium text-neutral-100 placeholder-neutral-500 focus:outline-none bg-transparent"
                        value={origin}
                        onInput={handleOrigin}
                    />
                </div>

                {/* Botón de intercambio */}
                <div className="flex items-center justify-center border-l border-r border-neutral-800 px-2">
                    <button className="p-2 text-neutral-500 hover:text-white hover:bg-neutral-700 rounded-full transition-colors">
                        <svg className="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M8 7h12m0 0l-4-4m4 4l-4 4m0 6H4m0 0l4 4m-4-4l4-4" />
                        </svg>
                    </button>
                </div>

                {/* Sección Destino */}
                <div className="flex-1 px-5 py-3 group">
                    <label htmlFor="destination" className="block text-xs font-semibold text-neutral-400 group-hover:text-white transition-colors">
                        Hacia
                    </label>
                    <input
                        id="destination"
                        type="text"
                        placeholder="Ciudad de destino"
                        className="w-full pt-1 text-base font-medium text-neutral-100 placeholder-neutral-500 focus:outline-none bg-transparent"
                        value={destination}
                        onInput={handleDestination}
                    />
                </div>

                {/* Sección Fecha */}
                <div className="flex-1 px-5 py-3 border-l border-r border-neutral-800 group">
                    <label htmlFor="date" className="block text-xs font-semibold text-neutral-400 group-hover:text-white transition-colors">
                        Fecha
                    </label>
                    <input
                        id="date"
                        type="date"
                        className="w-full pt-1 text-base font-medium text-neutral-100 focus:outline-none bg-transparent [color-scheme:dark]"
                        value={date}
                        onInput={handleDate}
                    />
                </div>

                {/* Botón de Búsqueda */}
                <div className="flex items-center p-2">
                    <button
                        onClick={handleSearchSubmit}
                        className="w-full h-full px-6 bg-sky-600 hover:bg-sky-500 text-white font-semibold rounded-xl transition-all duration-200 flex items-center justify-center gap-2 shadow-lg shadow-sky-900/40"
                    >
                        <Search className="h-5 w-5" />
                        <span className="hidden lg:inline">Buscar</span>
                    </button>
                </div>
            </div>
        </div>
    );
};