import { create } from "zustand";

export interface AuthStore {
    token: string | null;
    lastUpdated: number | null;
    setToken: (token: string | null) => void;
    setLastUpdated: (timestamp: number | null) => void;
    logout: () => void;
}

export const useAuthStore = create<AuthStore>((set) => ({
    token: null,
    lastUpdated: null,
    setToken: (token) => set({ token, lastUpdated: Date.now() }),
    setLastUpdated: (timestamp) => set({ lastUpdated: timestamp }),
    logout: () => set({ token: null, lastUpdated: null }),
}));

export default useAuthStore;