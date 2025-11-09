import { create } from "zustand";
import { AccountResponse } from "../api";
import { AuthStore } from "./AuthStore";

export interface AccountStore {
    accountId: number | null;
    account: AccountResponse | null;
    lastUpdated: number | null;
    setAccount: (account: AccountResponse | null) => void;
    setAccountId: (accountId: number | null) => void;
    setLastUpdated: (lastUpdated: number | null) => void;
}
export const useAccountStore = create<AccountStore>((set) => ({
    accountId: null,
    account: null,
    lastUpdated: null,
    setAccount: (account) => set({ account }),
    setAccountId: (accountId) => set({ accountId }),
    setLastUpdated: (lastUpdated) => set({ lastUpdated }),
}));

export default useAccountStore;