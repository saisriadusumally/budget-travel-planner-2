# Budget Travel Planner

Java + Spring Boot (REST APIs + JWT login) · React.js · SQL database · rule-based recommendation service.

## Run it (one step)
Needs JDK 17+, Maven and Node 18+. **No database install needed** (an embedded H2 database is used by default).

- Windows: double-click `run.bat`
- Mac/Linux: `./run.sh`

When the console says the app has started, open the address Spring Boot prints for port 8080
(the app, login page and API are all served by that one server).

## Login and logout
- Register or log in on the first page. The server returns a JWT in an **HttpOnly, SameSite=Strict cookie**
  (JavaScript cannot read it, and the browser stores nothing in localStorage).
- **Log out** calls `POST /api/auth/logout`, which expires the cookie. Sessions also expire after 8 hours.
- Every `/api/**` endpoint except login/register/logout needs the cookie, and each user can only see their own trips.

## Settings (environment variables, all optional)
| Variable | Purpose |
|---|---|
| `DB_URL`, `DB_USER`, `DB_PASSWORD` | Use MySQL instead of H2, e.g. `DB_URL=jdbc:mysql://<host>:3306/budget_travel?createDatabaseIfNotExist=true` |
| `JWT_SECRET` | 32+ character secret so logins survive a restart (random if unset) |
| `COOKIE_SECURE=true` | Set when serving over HTTPS |
| `PORT` | Server port (default 8080) |

## Hot-reload development (optional)
Copy `frontend/.env.example` to `frontend/.env`, set `VITE_API_TARGET` to the backend address, then `npm run dev` in `frontend`.

## REST APIs
| Method | Path | Auth | Purpose |
|---|---|---|---|
| POST | /api/auth/register | no | Create account and log in |
| POST | /api/auth/login | no | Log in |
| POST | /api/auth/logout | no | Log out |
| GET | /api/auth/me | yes | Current traveler |
| GET | /api/travelers/{id} | yes | Own profile |
| GET | /api/destinations | yes | List destinations |
| POST | /api/trips | yes | Create trip + estimate expenses |
| GET | /api/trips | yes | My trips |
| GET | /api/trips/{id}/budget | yes | Budget summary |
| POST | /api/itinerary/generate | yes | Generate itinerary `{tripId}` |
| GET | /api/itinerary/{tripId} | yes | Read itinerary |
| POST | /api/recommendations | yes | Suggestions `{tripId}` |
| PUT | /api/expenses/{id} | yes | Update actual amount `{actualAmount}` |
