# Deployment Guide (No Database)

This project currently stores data in memory. That means uploaded data is cleared when the backend restarts.

For your use case (upload CSV -> view reports -> download CSV in one session), this is acceptable.

## 1) Backend (Railway)

### Service setup
1. Push this backend repo to GitHub.
2. In Railway: `New Project` -> `Deploy from GitHub repo`.
3. Select this repository.
4. After first deploy, open the service and click `Generate Domain`.

### Required Railway environment variables
Set these in Railway -> Variables:

```env
CORS_ALLOWED_ORIGIN_PATTERNS=https://your-frontend.vercel.app,https://*.vercel.app,http://localhost:5173,http://localhost:3000
```

`PORT` is provided automatically by Railway.

If you later use a custom domain for frontend, add it to `CORS_ALLOWED_ORIGIN_PATTERNS`.

### Health check
After deploy, verify:

```text
GET https://your-backend-domain.up.railway.app/api/attendance/health
```

Expected response:

```json
{
  "status": "UP",
  "message": "Attendance API is running"
}
```

## 1B) Backend (Render using Docker)

If you deploy on Render, create a **Web Service** and set runtime/language to **Docker**.

### Render settings
- Branch: `master`
- Root directory: (leave empty)
- Runtime: `Docker`
- Health check path: `/api/attendance/health`

### Required Render environment variables

```env
CORS_ALLOWED_ORIGIN_PATTERNS=https://your-frontend.vercel.app,https://*.vercel.app,http://localhost:5173,http://localhost:3000
```

`PORT` is provided automatically by Render.

### Verify after deploy

```text
GET https://your-backend-name.onrender.com/api/attendance/health
```

Expected:

```json
{
  "status": "UP",
  "message": "Attendance API is running"
}
```

## 2) Frontend (Vercel, React/Vite)

In your frontend repo, add:

### `.env.production`

```env
VITE_API_BASE_URL=https://your-backend-domain
```

### `src/api.js` (exact pattern)

```javascript
import axios from "axios";

const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || "http://localhost:8080";

const api = axios.create({
  baseURL: `${API_BASE_URL}/api`,
});

export default api;
```

### `vercel.json` (for SPA routing)

```json
{
  "rewrites": [
    { "source": "/(.*)", "destination": "/index.html" }
  ]
}
```

Then in Vercel:
1. Import your frontend repo.
2. Add env variable `VITE_API_BASE_URL` (same value as above).
3. Deploy.

## 3) Final verification flow

1. Open frontend URL.
2. Upload CSV file.
3. Confirm employee reports load.
4. Confirm incident calendar shows dates.
5. Confirm download buttons generate CSV files.
6. Restart backend once and confirm data resets (expected behavior).

## 4) Current backend production-ready settings already added

- Dynamic cloud port binding:
  - `server.port=${PORT:8080}`
- Environment-driven CORS:
  - `app.cors.allowed-origin-patterns=${CORS_ALLOWED_ORIGIN_PATTERNS:...}`
- File upload limits:
  - `spring.servlet.multipart.max-file-size=10MB`
  - `spring.servlet.multipart.max-request-size=10MB`
