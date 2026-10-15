import { defineConfig, loadEnv } from "vite";
import react from "@vitejs/plugin-react";

// Production: `npm run build` writes the app into the Spring Boot jar's static folder,
// so one server serves both UI and API (same address, no CORS, no hardcoded host).
// Optional hot-reload dev mode: set VITE_API_TARGET in frontend/.env (see .env.example).
export default defineConfig(({ mode }) => {
  const env = loadEnv(mode, process.cwd(), "");
  return {
    plugins: [react()],
    build: { outDir: "../backend/src/main/resources/static", emptyOutDir: true },
    server: env.VITE_API_TARGET ? { proxy: { "/api": env.VITE_API_TARGET } } : {},
  };
});
