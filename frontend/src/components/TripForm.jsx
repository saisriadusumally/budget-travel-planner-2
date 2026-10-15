import { useEffect, useState } from "react";
import { api } from "../api.js";

export default function TripForm({ onCreated }) {
  const [destinations, setDestinations] = useState([]);
  const [form, setForm] = useState({ destination: "", startDate: "", endDate: "", totalBudget: 20000, travelers: 1 });
  const [error, setError] = useState("");
  const [busy, setBusy] = useState(false);
  const set = (k) => (e) => setForm({ ...form, [k]: e.target.value });

  useEffect(() => {
    api.destinations().then((d) => { setDestinations(d); if (d[0]) setForm((f) => ({ ...f, destination: d[0].name })); })
      .catch((e) => setError(e.message));
  }, []);

  const submit = async (e) => {
    e.preventDefault();
    setBusy(true); setError("");
    try {
      const trip = await api.createTrip({
        ...form,
        totalBudget: Number(form.totalBudget), travelers: Number(form.travelers),
      });
      onCreated(trip);
    } catch (err) { setError(err.message); }
    finally { setBusy(false); }
  };

  return (
    <form className="panel trip-form" onSubmit={submit}>
      <h2>Plan a new trip</h2>
      <label>Destination
        <select value={form.destination} onChange={set("destination")}>
          {destinations.map((d) => <option key={d.id} value={d.name}>{d.name}, {d.country}</option>)}
        </select>
      </label>
      <div className="row">
        <label>Start date<input type="date" required value={form.startDate} onChange={set("startDate")} /></label>
        <label>End date<input type="date" required value={form.endDate} min={form.startDate} onChange={set("endDate")} /></label>
      </div>
      <div className="row">
        <label>Total budget (₹)<input type="number" min="1000" step="500" required value={form.totalBudget} onChange={set("totalBudget")} /></label>
        <label>Travelers<input type="number" min="1" max="12" required value={form.travelers} onChange={set("travelers")} /></label>
      </div>
      {error && <p className="error" role="alert">{error}</p>}
      <button className="primary" disabled={busy}>{busy ? "Estimating…" : "Estimate costs"}</button>
    </form>
  );
}
