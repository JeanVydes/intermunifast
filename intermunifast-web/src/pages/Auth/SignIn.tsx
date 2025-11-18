import { useState, useEffect } from 'preact/hooks';
import { useLocation } from 'preact-iso';
import { AuthAPI } from '../../api/Auth';
import { useAuthStore } from '../../stores/AuthStore';
import { useAccountStore } from '../../stores/AccountStore';

export default function SignIn() {
    const { route } = useLocation();
    const setToken = useAuthStore(state => state.setToken);
    const { setAccount, setAccountId, setLastUpdated, accountId } = useAccountStore();
    const [formData, setFormData] = useState({
        email: '',
        password: '',
    });
    const [error, setError] = useState('');
    const [emailError, setEmailError] = useState('');
    const [loading, setLoading] = useState(false);

    // Get redirect URL from query params
    const getRedirectUrl = () => {
        const params = new URLSearchParams(window.location.search);
        return params.get('redirect') || '/dashboard';
    };

    // Redirect if already authenticated
    useEffect(() => {
        if (accountId) {
            route(getRedirectUrl());
        }
    }, [accountId]);

    const handleChange = (e: any) => {
        const { name, value } = e.target;
        setFormData(prev => ({
            ...prev,
            [name]: value
        }));

        if (name === 'email') {
            const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
            if (value && !emailRegex.test(value)) {
                setEmailError('Please enter a valid email address');
            } else {
                setEmailError('');
            }
        }
    };

    const handleSubmit = async (e: Event) => {
        e.preventDefault();
        setError('');

        // Validation
        if (!formData.email || !formData.password) {
            setError('Please fill in all fields');
            return;
        }

        setLoading(true);

        try {
            // Call the signin API
            const response = await AuthAPI.signIn({
                email: formData.email,
                password: formData.password,
            });

            // Store the token
            const { token } = response.data;

            // Set token in Zustand store and localStorage
            setToken(token);
            localStorage.setItem('authToken', token);

            // Fetch and set user profile in AccountStore
            const userProfileResponse = await AuthAPI.getMe();
            setAccount(userProfileResponse.data);
            setAccountId(userProfileResponse.data.id);
            setLastUpdated(Date.now());

            // Redirect to original destination or dashboard
            const redirectUrl = getRedirectUrl();
            route(redirectUrl);
        } catch (err: any) {
            setError(err.response?.data?.message || 'Invalid email or password. Please try again.');
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
                    <h1 className="text-3xl font-bold text-white mb-2">Bienvenido</h1>
                    <p className="text-neutral-400">Inicia sesión en tu cuenta</p>
                </div>

                {error && (
                    <div className="mb-6 bg-red-500/10 border border-red-500/30 text-red-400 px-4 py-3 rounded-xl text-sm">
                        {error}
                    </div>
                )}

                <form onSubmit={handleSubmit} className="space-y-5">
                    <div>
                        <label htmlFor="email" className="block text-sm font-medium text-neutral-300 mb-2">
                            Correo electrónico
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
                        <label htmlFor="password" className="block text-sm font-medium text-neutral-300 mb-2">
                            Contraseña
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

                    <div className="flex items-center justify-between text-sm">
                        <label className="flex items-center cursor-pointer">
                            <input
                                type="checkbox"
                                className="h-4 w-4 text-accent focus:ring-accent border-white/20 rounded bg-neutral-900"
                            />
                            <span className="ml-2 text-neutral-400">Recordarme</span>
                        </label>
                        <a href="#" className="text-accent hover:text-accent-dark font-medium transition-colors">
                            ¿Olvidaste tu contraseña?
                        </a>
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
                                Iniciando sesión...
                            </>
                        ) : (
                            'Iniciar sesión'
                        )}
                    </button>
                </form>

                <div className="mt-6 text-center">
                    <p className="text-sm text-neutral-400">
                        ¿No tienes cuenta?{' '}
                        <a href="/auth/signup" className="text-accent hover:text-accent-dark font-bold transition-colors">
                            Regístrate
                        </a>
                    </p>
                </div>
            </div>
        </div>
    );
}
