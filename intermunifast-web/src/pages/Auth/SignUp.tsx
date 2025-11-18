import { useState, useEffect } from 'preact/hooks';
import { useLocation } from 'preact-iso';
import { AccountAPI } from '../../api/Account';
import useAccountStore from '../../stores/AccountStore';

export default function SignUp() {
    const { route } = useLocation();
    const { accountId } = useAccountStore();
    const [formData, setFormData] = useState({
        name: '',
        email: '',
        password: '',
        confirmPassword: '',
        phone: '',
        isAdmin: false,
    });
    const [error, setError] = useState('');
    const [emailError, setEmailError] = useState('');
    const [loading, setLoading] = useState(false);

    // Get redirect URL from query params or default to dashboard
    const getRedirectUrl = () => {
        const params = new URLSearchParams(window.location.search);
        const redirect = params.get('redirect');
        return redirect ? decodeURIComponent(redirect) : '/dashboard';
    };

    // Redirect if already authenticated
    useEffect(() => {
        if (accountId) {
            route(getRedirectUrl());
        }
    }, [accountId]);

    const handleChange = (e: any) => {
        const { name, value, type, checked } = e.target;
        setFormData(prev => ({
            ...prev,
            [name]: type === 'checkbox' ? checked : value
        }));

        if (name === 'email') {
            const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
            if (value && !emailRegex.test(value)) {
                setEmailError('Por favor ingresa un email válido');
            } else {
                setEmailError('');
            }
        }
    };

    const handleSubmit = async (e: Event) => {
        e.preventDefault();
        setError('');

        // Validation
        if (!formData.name || !formData.email || !formData.password || !formData.phone) {
            setError('Please fill in all required fields');
            return;
        }

        if (formData.password !== formData.confirmPassword) {
            setError('Passwords do not match');
            return;
        }

        if (formData.password.length < 6) {
            setError('Password must be at least 6 characters long');
            return;
        }

        setLoading(true);

        try {
            // Call the account creation API
            await AccountAPI.create({
                name: formData.name,
                email: formData.email,
                password: formData.password,
                phone: formData.phone,
                isAdmin: formData.isAdmin,
            });

            // Redirect to sign in page with the redirect parameter
            const redirectUrl = getRedirectUrl();
            const encodedRedirect = encodeURIComponent(redirectUrl);
            route(`/auth/signin?redirect=${encodedRedirect}`);
        } catch (err: any) {
            setError(err.response?.data?.message || 'Failed to create account. Please try again.');
        } finally {
            setLoading(false);
        }
    };

    return (
        <div className="min-h-screen bg-neutral-950 flex items-center justify-center p-4">
            <div className="max-w-md w-full bg-white/5 backdrop-blur-xl border border-white/10 rounded-3xl shadow-2xl p-8">
                <div className="text-center mb-8">
                    <div className="inline-flex items-center justify-center w-16 h-16 rounded-2xl bg-accent/10 border border-accent/30 mb-4">
                        <svg className="w-8 h-8 text-accent" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M13 10V3L4 14h7v7l9-11h-7z" />
                        </svg>
                    </div>
                    <h1 className="text-3xl font-bold text-white mb-2">Crear cuenta</h1>
                    <p className="text-neutral-400">Regístrate en InterMuniFast</p>
                </div>

                {error && (
                    <div className="mb-6 bg-red-500/10 border border-red-500/30 text-red-400 px-4 py-3 rounded-xl text-sm">
                        {error}
                    </div>
                )}

                <form onSubmit={handleSubmit} className="space-y-4">
                    <div>
                        <label htmlFor="name" className="block text-sm font-medium text-neutral-300 mb-2">
                            Nombre completo *
                        </label>
                        <input
                            type="text"
                            id="name"
                            name="name"
                            value={formData.name}
                            onChange={handleChange}
                            className="w-full px-4 py-3 bg-neutral-900 border border-white/10 rounded-xl text-white placeholder-neutral-500 focus:border-accent focus:ring-2 focus:ring-accent/30 outline-none transition-all"
                            placeholder="Juan Pérez"
                            required
                        />
                    </div>

                    <div>
                        <label htmlFor="email" className="block text-sm font-medium text-neutral-300 mb-2">
                            Correo electrónico *
                        </label>
                        <input
                            type="email"
                            id="email"
                            name="email"
                            value={formData.email}
                            onChange={handleChange}
                            className={`w-full px-4 py-3 bg-neutral-900 border ${emailError ? 'border-red-500' : 'border-white/10'} rounded-xl text-white placeholder-neutral-500 focus:border-accent focus:ring-2 focus:ring-accent/30 outline-none transition-all`}
                            placeholder="tu@email.com"
                            required
                        />
                        {emailError && (
                            <p className="mt-1 text-xs text-red-400">{emailError}</p>
                        )}
                    </div>

                    <div>
                        <label htmlFor="phone" className="block text-sm font-medium text-neutral-300 mb-2">
                            Teléfono *
                        </label>
                        <input
                            type="tel"
                            id="phone"
                            name="phone"
                            value={formData.phone}
                            onChange={handleChange}
                            className="w-full px-4 py-3 bg-neutral-900 border border-white/10 rounded-xl text-white placeholder-neutral-500 focus:border-accent focus:ring-2 focus:ring-accent/30 outline-none transition-all"
                            placeholder="+1 234 567 8900"
                            required
                        />
                    </div>

                    <div>
                        <label htmlFor="password" className="block text-sm font-medium text-neutral-300 mb-2">
                            Contraseña *
                        </label>
                        <input
                            type="password"
                            id="password"
                            name="password"
                            value={formData.password}
                            onChange={handleChange}
                            className="w-full px-4 py-3 bg-neutral-900 border border-white/10 rounded-xl text-white placeholder-neutral-500 focus:border-accent focus:ring-2 focus:ring-accent/30 outline-none transition-all"
                            placeholder="••••••••"
                            required
                        />
                    </div>

                    <div>
                        <label htmlFor="confirmPassword" className="block text-sm font-medium text-neutral-300 mb-2">
                            Confirmar contraseña *
                        </label>
                        <input
                            type="password"
                            id="confirmPassword"
                            name="confirmPassword"
                            value={formData.confirmPassword}
                            onChange={handleChange}
                            className="w-full px-4 py-3 bg-neutral-900 border border-white/10 rounded-xl text-white placeholder-neutral-500 focus:border-accent focus:ring-2 focus:ring-accent/30 outline-none transition-all"
                            placeholder="••••••••"
                            required
                        />
                    </div>

                    <div className="bg-white/5 backdrop-blur-sm rounded-2xl p-4 border border-white/10">
                        <label className="block text-sm font-medium text-neutral-300 mb-3">
                            Tipo de cuenta
                        </label>
                        <div className="space-y-3">
                            <label className="flex items-start cursor-pointer">
                                <input
                                    type="radio"
                                    name="isAdmin"
                                    checked={!formData.isAdmin}
                                    onChange={() => setFormData(prev => ({ ...prev, isAdmin: false }))}
                                    className="mt-1 h-4 w-4 text-accent focus:ring-accent border-white/20 bg-neutral-900"
                                />
                                <div className="ml-3">
                                    <span className="block text-sm font-medium text-white">Usuario regular</span>
                                    <span className="block text-xs text-neutral-400">Reserva tickets y gestiona tus viajes</span>
                                </div>
                            </label>

                            <label className="flex items-start cursor-pointer">
                                <input
                                    type="radio"
                                    name="isAdmin"
                                    checked={formData.isAdmin}
                                    onChange={() => setFormData(prev => ({ ...prev, isAdmin: true }))}
                                    className="mt-1 h-4 w-4 text-accent focus:ring-accent border-white/20 bg-neutral-900"
                                />
                                <div className="ml-3">
                                    <span className="block text-sm font-medium text-white">Administrador</span>
                                    <span className="block text-xs text-neutral-400">Gestiona buses, rutas y configuración del sistema</span>
                                </div>
                            </label>
                        </div>
                    </div>

                    <button
                        type="submit"
                        disabled={loading || !!emailError}
                        className="w-full bg-accent hover:bg-accent-dark disabled:bg-neutral-700 disabled:text-neutral-500 text-white font-bold py-3 rounded-xl transition-all duration-200 flex items-center justify-center shadow-lg shadow-accent/20 disabled:shadow-none"
                    >
                        {loading ? (
                            <>
                                <svg className="animate-spin -ml-1 mr-3 h-5 w-5 text-white" xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24">
                                    <circle className="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" strokeWidth="4"></circle>
                                    <path className="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"></path>
                                </svg>
                                Creando cuenta...
                            </>
                        ) : (
                            'Crear cuenta'
                        )}
                    </button>
                </form>

                <div className="mt-6 text-center">
                    <p className="text-sm text-neutral-400">
                        ¿Ya tienes cuenta?{' '}
                        <a href="/auth/signin" className="text-accent hover:text-accent-dark font-bold transition-colors">
                            Inicia sesión
                        </a>
                    </p>
                </div>
            </div>
        </div>
    );
}
