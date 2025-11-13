import { LocationProvider, Router, Route, hydrate, prerender as ssr } from 'preact-iso';

import { Header } from './components/Header.jsx';
import Home from './pages/Home/index.jsx';
import { NotFound } from './pages/_404.jsx';
import './style.css';
import { AuthContextProvider } from './providers/AuthContextProvider.js';
import Account from './pages/Account/index.js';

export function App() {
	return (
		<LocationProvider>
			<AuthContextProvider>
				<Router>
					<Route path="/" component={Home} />
					<Route path="/account" component={Account} />
					<Route default component={NotFound} />
				</Router>
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
