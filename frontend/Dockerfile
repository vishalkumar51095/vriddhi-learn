# --- Build the SPA ---
FROM node:22-alpine AS build
WORKDIR /app
COPY package.json package-lock.json ./
RUN npm ci --no-audit --no-fund
COPY . .
# VITE_API_BASE_URL is left empty on purpose: the browser calls same-origin /api/*,
# and Caddy (below) forwards that to the gateway — so no CORS setup is needed in production.
RUN npm run build

# --- Serve it (Caddy: static files + /api reverse proxy + automatic HTTPS when given a domain) ---
FROM caddy:2-alpine
COPY Caddyfile /etc/caddy/Caddyfile
COPY --from=build /app/dist /srv
EXPOSE 80 443
