import { useEffect, useState } from "react";
import { api, setUnauthorizedHandler } from "./api.js";
import AuthPage from "./components/AuthPage.jsx";
import Dashboard from "./components/Dashboard.jsx";

export default function App() {
  const [user, setUser] = useState(null);
  const [checking, setChecking] = useState(true);
  const [notice, setNotice] = useState("");

  useEffect(() => {
    // Expired or missing session -> back to the login page.
    setUnauthorizedHandler(() => { setUser(null); setNotice("Your session ended. Please log in again."); });
    api.me().then(setUser).catch(() => setUser(null)).finally(() => setChecking(false));
  }, []);

  const signIn = (u) => { setNotice(""); setUser(u); };
  const signOut = async () => {
    try { await api.logout(); } catch { /* cookie may already be gone */ }
    setNotice("You have been logged out.");
    setUser(null);
  };

  if (checking) return <p className="boot">Loading…</p>;
  return user
    ? <Dashboard user={user} onSignOut={signOut} />
    : <AuthPage onSignIn={signIn} notice={notice} />;
}
