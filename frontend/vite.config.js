import { defineConfig } from "vite";
import react from "@vitejs/plugin-react";

// Serve react app on port 5500
// Foward web (API) request to proxy address  
export default defineConfig({
    plugins: [react()],
    server: {
        port: 5500,
        proxy: { "/api": "http://127.0.0.1:8080" }
    }
});