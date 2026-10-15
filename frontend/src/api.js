// Same-origin API calls. The login session lives in an HttpOnly cookie set by the server,
// so nothing is stored in the browser by this code.
let onUnauthorized = () => {};
export const setUnauthorizedHandler = (fn) => { onUnauthorized = fn; };

async function request(method, url, body) {
  const res = await fetch(url, {
    method,
    credentials: "same-origin",
    headers: { "Content-Type": "application/json" },
    body: body ? JSON.stringify(body) : undefined,
  });
  const data = await res.json().catch(() => ({}));
  if (res.status === 401 && !url.startsWith("/api/auth/")) onUnauthorized();
  if (!res.ok) {
    const err = new Error(
      res.status === 401 && !url.startsWith("/api/auth/login")
        ? "Your session ended. Please log in again."
        : data.message || "Something went wrong. Try again."
    );
    err.status = res.status;
    throw err;
  }
  return data;
}

export const api = {
  register: (b) => request("POST", "/api/auth/register", b),
  login: (b) => request("POST", "/api/auth/login", b),
  logout: () => request("POST", "/api/auth/logout"),
  me: () => request("GET", "/api/auth/me"),
  destinations: () => request("GET", "/api/destinations"),
  trips: () => request("GET", "/api/trips"),
  createTrip: (b) => request("POST", "/api/trips", b),
  budget: (id) => request("GET", `/api/trips/${id}/budget`),
  itinerary: (id) => request("GET", `/api/itinerary/${id}`),
  generateItinerary: (tripId) => request("POST", "/api/itinerary/generate", { tripId }),
  recommend: (tripId) => request("POST", "/api/recommendations", { tripId }),
  updateExpense: (id, actualAmount) => request("PUT", `/api/expenses/${id}`, { actualAmount }),
};

export const money = (n) => "₹" + Math.round(n || 0).toLocaleString("en-IN");
