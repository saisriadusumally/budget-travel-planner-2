import { useCallback, useEffect, useState } from "react";
import { api, money } from "../api.js";
import TripForm from "./TripForm.jsx";
import BudgetView from "./BudgetView.jsx";

export default function Dashboard({ user, onSignOut }) {
  const [trips, setTrips] = useState([]);
  const [selected, setSelected] = useState(null); // trip id, or "new"
  const [error, setError] = useState("");

  const load = useCallback(async () => {
    try {
      const list = await api.trips();
      setTrips(list);
      setSelected((s) => s ?? (list[0] ? list[0].id : "new"));
    } catch (e) { setError(e.message); }
  }, []);

  useEffect(() => { load(); }, [load]);

  return (
    <div className="shell">
      <aside className="sidebar">
        <div className="brand">Budget Travel Planner</div>
        <button className="primary" onClick={() => setSelected("new")}>New trip</button>
        <nav aria-label="Your trips">
          {trips.length === 0 && <p className="muted">No trips yet. Create your first one.</p>}
          {trips.map((t) => (
            <button key={t.id} className={"trip-link" + (selected === t.id ? " active" : "")} onClick={() => setSelected(t.id)}>
              <strong>{t.destination}</strong>
              <span>{t.startDate} · {money(t.totalBudget)}</span>
            </button>
          ))}
        </nav>
        <div className="who">
          <span>{user.name}</span>
          <button className="logout" onClick={onSignOut}>Log out</button>
        </div>
      </aside>
      <main className="content">
        {error && <p className="error" role="alert">{error}</p>}
        {selected === "new" && (
          <TripForm onCreated={async (t) => { await load(); setSelected(t.id); }} />
        )}
        {selected && selected !== "new" && <BudgetView key={selected} tripId={selected} />}
      </main>
    </div>
  );
}
