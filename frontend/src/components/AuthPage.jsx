import { useState } from "react";
import { api } from "../api.js";

export default function AuthPage({ onSignIn, notice }) {
  const [mode, setMode] = useState("login");
  const [form, setForm] = useState({ name: "", email: "", password: "", budgetPreference: "MEDIUM", travelStyle: "Relaxed", interests: "" });
  const [error, setError] = useState("");
  const [busy, setBusy] = useState(false);
  const set = (k) => (e) => setForm({ ...form, [k]: e.target.value });

  const submit = async (e) => {
    e.preventDefault();
    setBusy(true); setError("");
    try {
      const user = mode === "login"
        ? await api.login({ email: form.email, password: form.password })
        : await api.register(form);
      onSignIn(user);
    } catch (err) { setError(err.message); }
    finally { setBusy(false); }
  };

  return (
    <main className="auth">
      <section className="auth-intro">
        <h1>Plan the trip your budget can carry.</h1>
        <p>Enter what you can spend. Get costs, a day-by-day plan, and a running balance while you travel.</p>
        <ol className="flow">
          <li>Set budget</li><li>Pick destination</li><li>See costs</li><li>Get itinerary</li><li>Track spending</li>
        </ol>
      </section>
      <form className="panel auth-form" onSubmit={submit}>
        <h2>{mode === "login" ? "Log in" : "Create your account"}</h2>
        {mode === "register" && (
          <label>Name<input required value={form.name} onChange={set("name")} /></label>
        )}
        <label>Email<input type="email" required value={form.email} onChange={set("email")} /></label>
        <label>Password<input type="password" required minLength={6} value={form.password} onChange={set("password")} /></label>
        {mode === "register" && (
          <>
            <label>Budget preference
              <select value={form.budgetPreference} onChange={set("budgetPreference")}>
                <option value="LOW">Low</option><option value="MEDIUM">Medium</option><option value="HIGH">High</option>
              </select>
            </label>
            <label>Travel style
              <select value={form.travelStyle} onChange={set("travelStyle")}>
                <option>Relaxed</option><option>Adventure</option><option>Cultural</option><option>Foodie</option>
              </select>
            </label>
            <label>Interests<input placeholder="beaches, forts, street food" value={form.interests} onChange={set("interests")} /></label>
          </>
        )}
        {notice && !error && <p className="notice" role="status">{notice}</p>}
        {error && <p className="error" role="alert">{error}</p>}
        <button className="primary" disabled={busy}>{busy ? "Please wait…" : mode === "login" ? "Log in" : "Create account"}</button>
        <button type="button" className="link" onClick={() => { setError(""); setMode(mode === "login" ? "register" : "login"); }}>
          {mode === "login" ? "New here? Create an account" : "Have an account? Log in"}
        </button>
      </form>
    </main>
  );
}
