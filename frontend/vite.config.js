import { defineConfig } from "vite";
import react from "@vitejs/plugin-react";

export default defineConfig({
  plugins: [react()],
  server: {
    port: 3000,  // Changed to match standard frontend port
    host: true,  // Allow access from outside the container
    allowedHosts: ["deti-tqs-03.ua.pt"],
    proxy: {
      '/backend': {
        target: 'http://backend:8080',
        changeOrigin: true,
        rewrite: (path) => path.replace(/^\/backend/, '')
      }
    }
  },
  optimizeDeps: {
    include: ['react-leaflet', 'leaflet'],
  },
});
