import { useCallback, useEffect, useState } from "react";
import { api, money } from "../api.js";

export default function BudgetView({ tripId }) {
  const [data, setData] = useState(null);
  const [plan, setPlan] = useState([]);
  const [recs, setRecs] = useState(null);
  const [error, setError] = useState("");
  const [busy, setBusy] = useState("");

  const refresh = useCallback(async () => {
    try {
      setData(await api.budget(tripId));
      setPlan(await api.itinerary(tripId));
    } catch (e) { setError(e.message); }
  }, [tripId]);

  useEffect(() => { refresh(); }, [refresh]);

  const run = (name, fn) => async () => {
    setBusy(name); setError("");
    try { await fn(); } catch (e) { setError(e.message); } finally { setBusy(""); }
  };

  const makeItinerary = run("plan", async () => setPlan(await api.generateItinerary(tripId)));
  const getRecs = run("recs", async () => setRecs(await api.recommend(tripId)));
  const saveActual = async (id, value) => {
    try { await api.updateExpense(id, Number(value) || 0); await refresh(); } catch (e) { setError(e.message); }
  };

  if (!data) return <p className="muted">{error || "Loading trip…"}</p>;

  const { trip, expenses } = data;
  const maxAmt = Math.max(...expenses.map((e) => Math.max(e.estimatedAmount, e.actualAmount)), 1);
  const spentPct = Math.min(100, Math.round((data.actualTotal / data.totalBudget) * 100));
  const over = data.overBudget;

  return (
    <div className="stack">
      <header className="trip-head">
        <div>
          <h1>{trip.destination}</h1>
          <p className="muted">{trip.startDate} to {trip.endDate} · {data.days} days · {trip.travelers} {trip.travelers > 1 ? "travelers" : "traveler"}</p>
        </div>
        <div className={"balance " + (over ? "bad" : "good")}>
          <span>{over ? "Over budget by" : "Left after estimate"}</span>
          <strong>{money(Math.abs(data.remaining))}</strong>
        </div>
      </header>
      {error && <p className="error" role="alert">{error}</p>}

      <section className="stats">
        <div><span>Total budget</span><strong>{money(data.totalBudget)}</strong></div>
        <div><span>Estimated cost</span><strong>{money(data.estimatedTotal)}</strong></div>
        <div><span>Spent so far</span><strong>{money(data.actualTotal)}</strong></div>
        <div><span>Budget left now</span><strong>{money(data.remainingAfterActual)}</strong></div>
      </section>

      <section className="panel">
        <h2>Expenses: planned vs actual</h2>
        <div className="progress" aria-label={`${spentPct}% of budget spent`}><i style={{ width: spentPct + "%" }} /></div>
        <p className="muted small">{spentPct}% of your budget spent</p>
        <table>
          <thead><tr><th>Category</th><th>Planned</th><th className="bars-col">Compare</th><th>Actual (₹)</th></tr></thead>
          <tbody>
            {expenses.map((e) => (
              <tr key={e.id}>
                <td>{e.category}</td>
                <td>{money(e.estimatedAmount)}</td>
                <td className="bars-col">
                  <div className="bar plan" style={{ width: (e.estimatedAmount / maxAmt) * 100 + "%" }} />
                  <div className={"bar act" + (e.actualAmount > e.estimatedAmount ? " over" : "")} style={{ width: (e.actualAmount / maxAmt) * 100 + "%" }} />
                </td>
                <td>
                  <input type="number" min="0" className="amt" aria-label={`Actual spend on ${e.category}`}
                    defaultValue={e.actualAmount || ""} placeholder="0"
                    onBlur={(ev) => Number(ev.target.value) !== e.actualAmount && saveActual(e.id, ev.target.value)} />
                </td>
              </tr>
            ))}
          </tbody>
        </table>
        <p className="muted small">Type an amount and click away to save it.</p>
      </section>

      <div className="two">
        <section className="panel">
          <div className="panel-head">
            <h2>Itinerary</h2>
            <button className="primary small" onClick={makeItinerary} disabled={busy === "plan"}>
              {busy === "plan" ? "Building…" : plan.length ? "Rebuild" : "Generate itinerary"}
            </button>
          </div>
          {plan.length === 0 && <p className="muted">No itinerary yet. Generate one to get a plan for each day.</p>}
          <ol className="days">
            {plan.map((p) => (
              <li key={p.id}>
                <b>Day {p.dayNumber}</b>
                <span>{p.activity}</span>
                <em>{money(p.cost)}</em>
              </li>
            ))}
          </ol>
        </section>

        <section className="panel">
          <div className="panel-head">
            <h2>Suggestions</h2>
            <button className="primary small" onClick={getRecs} disabled={busy === "recs"}>
              {busy === "recs" ? "Thinking…" : "Get suggestions"}
            </button>
          </div>
          {!recs && <p className="muted">Get ideas to save money or use what is left of your budget.</p>}
          {recs && (
            <div className="recs">
              <ul>{recs.tips.map((t, i) => <li key={i}>{t}</li>)}</ul>
              {recs.alternatives.length > 0 && (
                <>
                  <h3>Destinations that fit your budget</h3>
                  <ul>{recs.alternatives.map((a) => <li key={a.destination}><b>{a.destination}</b>, {a.country}: about {money(a.estimatedCost)}</li>)}</ul>
                </>
              )}
              {recs.optionalActivities.length > 0 && (
                <>
                  <h3>Extra activities you can afford</h3>
                  <ul>{recs.optionalActivities.map((a) => <li key={a}>{a}</li>)}</ul>
                </>
              )}
            </div>
          )}
        </section>
      </div>
    </div>
  );
}
