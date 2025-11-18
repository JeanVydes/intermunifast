import { LocationProvider, Router, Route, hydrate, prerender as ssr } from 'preact-iso';

import { Header } from './components/Header.jsx';
import Home from './pages/Home/index.jsx';
import { NotFound } from './pages/_404.jsx';
import './style.css';
import { AuthContextProvider } from './providers/AuthContextProvider.js';
import { RoleProvider } from './providers/RoleProvider.js';
import Account from './pages/Account/index.js';
import DashboardHome from './pages/Dashboard/index.js';
import BusesPage from './pages/Dashboard/Buses.js';
import TripsPage from './pages/Dashboard/Trips.js';
import RoutesPage from './pages/Dashboard/Routes.js';
import UsersPage from './pages/Dashboard/Users.js';
import { SignIn, SignUp } from './pages/Auth/index.js';
import PendingTickets from './pages/Dashboard/PendingTickets.js';

export function App() {
	return (
		<LocationProvider>
			<AuthContextProvider>
				<RoleProvider>
					<div id="app">
						<Header />
						<Router>
							<Route path="/" component={Home} />
							<Route path="/auth/signin" component={SignIn} />
							<Route path="/auth/signup" component={SignUp} />
							<Route path="/account" component={Account} />
							<Route path="/dashboard" component={DashboardHome} />
							<Route path="/dashboard/buses" component={BusesPage} />
							<Route path="/dashboard/trips" component={TripsPage} />
							<Route path="/dashboard/routes" component={RoutesPage} />
							<Route path="/dashboard/tickets" component={PendingTickets} />
							<Route path="/dashboard/users" component={UsersPage} />
							<Route default component={NotFound} />
						</Router>
					</div>
				</RoleProvider>
			</AuthContextProvider>
		</LocationProvider>
	);
}

if (typeof window !== 'undefined') {
	hydrate(<App />, document.getElementById('app'));
}

export async function prerender(data) {
	return await ssr(<App {...data} />);
}
