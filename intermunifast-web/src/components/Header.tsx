import { useLocation } from 'preact-iso';
import { Zap, User } from 'lucide-preact';
import useAccountStore from '../stores/AccountStore';
import { clearAuthToken } from '../api';

export function Header() {
	const { url } = useLocation();
	const { account } = useAccountStore();

	const handleLogout = () => {
		clearAuthToken();
		window.location.href = '/';
	};

	return (
		<header className="fixed top-0 left-0 right-0 z-50 bg-gradient-to-r from-accent via-accent-dark to-accent backdrop-blur-xl border-b border-white/20 shadow-2xl shadow-black/20">
			<div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
				<div className="flex items-center justify-between h-20">
					{/* Logo */}
					<a
						href="/"
						className="flex items-center gap-3 text-white hover:scale-105 transition-transform duration-300 group"
					>
						<div className="w-11 h-11 bg-white/20 rounded-2xl flex items-center justify-center backdrop-blur-sm group-hover:bg-white/30 transition-all duration-300 shadow-lg">
							<Zap className="w-6 h-6 drop-shadow-lg" />
						</div>
						<div className="hidden sm:block">
							<div className="flex items-baseline gap-0.5">
								<span className="text-lg font-light tracking-tight">InterMuni</span>
								<span className="text-lg font-bold tracking-tight">Fast</span>
							</div>
							<p className="text-[10px] text-white/70 -mt-1 font-medium">Viaja rápido y seguro</p>
						</div>
					</a>

					{/* Navigation */}
					<nav className="flex items-center gap-3">
						{account ? (
							<>
								<a
									href="/account"
									className="text-white/90 hover:text-white text-sm font-semibold transition-all duration-200 px-3 py-2 rounded-xl hover:bg-white/10"
								>
									Mis tickets
								</a>
								{account.role === 'ADMIN' && (
									<a
										href="/dashboard"
										className="text-white/90 hover:text-white text-sm font-semibold transition-all duration-200 px-3 py-2 rounded-xl hover:bg-white/10"
									>
										Panel
									</a>
								)}
								<button
									onClick={handleLogout}
									className="flex items-center gap-2 px-5 py-2.5 bg-white/15 hover:bg-white/25 text-white rounded-xl transition-all duration-300 text-sm font-semibold shadow-lg backdrop-blur-sm border border-white/10"
								>
									<User className="w-4 h-4" />
									<span className="hidden sm:inline">Cerrar sesión</span>
								</button>
							</>
						) : (
							<a
								href="/auth"
								className="flex items-center gap-2 px-5 py-2.5 bg-white/15 hover:bg-white/25 text-white rounded-xl transition-all duration-300 text-sm font-semibold shadow-lg backdrop-blur-sm border border-white/10"
							>
								<User className="w-4 h-4" />
								<span className="hidden sm:inline">Iniciar sesión</span>
							</a>
						)}
					</nav>
				</div>
			</div>
		</header>
	);
}
