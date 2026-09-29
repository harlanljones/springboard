import { defineConfig } from "vite";
import react from "@vitejs/plugin-react";

// Dev: Vite serves the SPA on 5173 and proxies /api to Spring Boot on 8080, so the
// browser sees one origin and no CORS config is needed anywhere.
// Prod: `vite build` writes web/dist, which Maven copies into the jar (see ../pom.xml).
export default defineConfig({
  plugins: [react()],
  server: {
    port: 5173,
    strictPort: true,
    proxy: {
      "/api": {
        target: "http://localhost:8080",
        changeOrigin: true,
      },
    },
  },
});
