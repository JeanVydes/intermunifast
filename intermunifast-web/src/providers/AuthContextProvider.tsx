import { useEffect } from "preact/hooks";
import useAuthStore from "../stores/AuthStore";
import useAccountStore from "../stores/AccountStore";
import { AccountAPI } from "../api";
import { AccountResponse, AccountRole, AccountStatus } from "../api/types/Account";
import AuthAPI from "../api/Auth";

export const ACCOUNT_FETCHING_THRESHOLD = 5 * 60 * 1000; // 5 minutes

export function AuthContextProvider({ children }: { children: preact.ComponentChildren }) {
    const { token: authToken, setToken, lastUpdated: authLastUpdated } = useAuthStore();
    const { setAccountId, setAccount, setLastUpdated, lastUpdated: accountLastUpdated } = useAccountStore();

    useEffect(() => {
        console.log("Initializing AuthContextProvider");
        async function fetchAccount() {
            // Load token from localStorage on app start
            let token: string | null = localStorage.getItem('authToken');

            console.log("AuthContextProvider - fetchAccount - authToken:", authToken, "localStorage token:", token);

            if (token && !authToken) {
                console.log("AuthContextProvider - setting token from localStorage");
                setToken(token);
            }

            // Use the token from store (updated above or already present)
            const currentToken = token || authToken;

            if (currentToken) {
                console.log("AuthContextProvider - fetching account using authToken");
                try {
                    let response = await AuthAPI.getMe();
                    console.log("AuthContextProvider - fetched account from auth me:", response);
                    if (response) {
                        setAccountId(response.data.id);
                        setAccount(response.data);
                        setLastUpdated(Date.now());
                    }
                } catch (error) {
                    console.error("Failed to fetch account:", error);
                    // Clear invalid token
                    localStorage.removeItem('authToken');
                    setToken(null);
                }
            }
        }

        fetchAccount();
    }, []);

    return children;
}