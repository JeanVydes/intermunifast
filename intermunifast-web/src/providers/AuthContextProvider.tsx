import { useEffect } from "preact/hooks";
import useAuthStore from "../stores/AuthStore";
import useAccountStore from "../stores/AccountStore";
import { AccountAPI } from "../api";

export const ACCOUNT_FETCHING_THRESHOLD = 5 * 60 * 1000; // 5 minutes

export function AuthContextProvider({ children }: { children: preact.ComponentChildren }) {
    const { token: authToken, lastUpdated: authLastUpdated } = useAuthStore();
    const { setAccountId, setAccount, setLastUpdated, lastUpdated: accountLastUpdated } = useAccountStore();

    useEffect(() => {
        async function fetchAccount() {
            if (authToken && accountLastUpdated && (Date.now() - accountLastUpdated > ACCOUNT_FETCHING_THRESHOLD || !accountLastUpdated)) {
                let response = await AccountAPI.getById("");
                if (response) {
                    setAccountId(response.data.id);
                    setAccount(response.data);
                    setLastUpdated(Date.now());
                }
            }
        }

        fetchAccount();
    }, [authToken, accountLastUpdated, authLastUpdated]);

    return children;
}