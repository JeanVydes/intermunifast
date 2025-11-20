import { create } from 'zustand';
import { RouteResponse } from '../api/types/Transport';

interface RouteStore {
    routes: RouteResponse[];
    lastUpdated: number | null;
    isLoading: boolean;

    setRoutes: (routes: RouteResponse[]) => void;
    addRoute: (route: RouteResponse) => void;
    updateRoute: (id: number, route: RouteResponse) => void;
    removeRoute: (id: number) => void;
    setLoading: (loading: boolean) => void;
    clearRoutes: () => void;
}

const useRouteStore = create<RouteStore>((set) => ({
    routes: [],
    lastUpdated: null,
    isLoading: false,

    setRoutes: (routes) => set({ routes, lastUpdated: Date.now() }),

    addRoute: (route) => set((state) => ({
        routes: [...state.routes, route],
        lastUpdated: Date.now()
    })),

    updateRoute: (id, route) => set((state) => ({
        routes: state.routes.map(r => r.id === id ? route : r),
        lastUpdated: Date.now()
    })),

    removeRoute: (id) => set((state) => ({
        routes: state.routes.filter(r => r.id !== id),
        lastUpdated: Date.now()
    })),

    setLoading: (loading) => set({ isLoading: loading }),

    clearRoutes: () => set({ routes: [], lastUpdated: null, isLoading: false })
}));

export default useRouteStore;
