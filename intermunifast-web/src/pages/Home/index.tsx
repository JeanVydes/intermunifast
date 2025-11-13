// En tu componente de página (ej. 'src/routes/home/index.tsx')
import { FunctionComponent } from 'preact';
import { BusSearchBar, SearchParams } from '../../components/BusSearchBar';

export const Home: FunctionComponent = () => {

	const handleSearch = (params: SearchParams) => {
		console.log(params);
	};

	const openMobileSearchModal = () => {

	};

	return (
		<div className="w-full min-h-screen bg-gray-100">
			<div className="pt-8">
				<BusSearchBar
					onSubmit={handleSearch}
					onMobileClick={openMobileSearchModal}
				/>
			</div>
		</div>
	);
};

export default Home;