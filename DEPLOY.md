# Deploying Vriddhi Learn on a free server

Everything runs from one `docker compose` file on a single VM. The browser only ever talks to the
`web` container (Caddy): it serves the React app and forwards `/api/*` to the gateway, so there is
no CORS setup and Postgres/Eureka/the services are never exposed to the internet.

```
Internet ─► web (Caddy :80/:443) ─┬─► static React build
                                  └─► /api/* ─► api-gateway ─► auth / content / exam services ─► postgres
                                                     (all found through discovery-server / Eureka)
```

## What the server needs
5 Java services + Postgres ≈ 2–3 GB RAM (each JVM is capped at 256 MB heap in `docker-compose.prod.yml`).
Ubuntu 22.04/24.04, Docker + the compose plugin, ports 80 and 443 open. It cannot fit on 256–512 MB
"free web service" tiers, which is why a VM is the practical free option.

## Free option: Oracle Cloud Always Free (Arm VM)
1. Sign up at cloud.oracle.com (a card is needed for identity checks; Always Free resources aren't charged).
2. Compute → Create instance → image **Ubuntu**, shape **VM.Standard.A1.Flex** (Ampere/Arm), 2 OCPU / 12 GB
   (the current Always Free allowance for new accounts — check Oracle's Free Tier page, it has changed before).
   If you get "Out of host capacity", retry later or try another availability domain.
3. Networking → your subnet's security list → add ingress rules for TCP **80** and **443** from `0.0.0.0/0`.
4. SSH in and open the OS firewall too (Oracle's Ubuntu images block ports by default):
   ```bash
   sudo iptables -I INPUT 6 -p tcp --dport 80  -j ACCEPT
   sudo iptables -I INPUT 6 -p tcp --dport 443 -j ACCEPT
   sudo apt-get install -y iptables-persistent && sudo netfilter-persistent save
   ```
5. Install Docker:
   ```bash
   curl -fsSL https://get.docker.com | sudo sh
   sudo usermod -aG docker $USER && newgrp docker
   ```
All images used here (`maven`, `eclipse-temurin`, `postgres`, `node`, `caddy`) publish arm64 builds, so nothing needs changing for Arm.

## Deploy
```bash
git clone <your-repo> vriddhi-learn && cd vriddhi-learn     # or scp the project folder
cp .env.example .env
# fill the two secrets:
sed -i "s|^JWT_SECRET=.*|JWT_SECRET=$(openssl rand -base64 48 | tr -d '\n')|" .env
sed -i "s|^DB_PASSWORD=.*|DB_PASSWORD=$(openssl rand -hex 16)|" .env

docker compose -f docker-compose.prod.yml up -d --build      # first build takes several minutes
docker compose -f docker-compose.prod.yml ps
```
Open `http://<server-ip>/`. If a build is killed on a small VM (< 4 GB), add swap first:
`sudo fallocate -l 4G /swapfile && sudo chmod 600 /swapfile && sudo mkswap /swapfile && sudo swapon /swapfile`.

## Free HTTPS + a domain
1. Get a free name at duckdns.org (e.g. `vriddhi.duckdns.org`) pointing at the server's public IP.
2. Set `SITE_ADDRESS=vriddhi.duckdns.org` in `.env`, then `docker compose -f docker-compose.prod.yml up -d`.
   Caddy fetches and renews the Let's Encrypt certificate itself.

## Create the first admin
Register normally on the site, then:
```bash
docker compose -f docker-compose.prod.yml exec postgres \
  psql -U vriddhi -d vriddhi_auth -v email="'you@example.com'" -v role="'ADMIN'" -f /infra/promote-user.sql
```
Log out and in again. Teachers can then apply at `/teacher/register` and be approved under Admin → Teacher applications.

## Operating it
```bash
docker compose -f docker-compose.prod.yml logs -f api-gateway auth-service   # logs
docker compose -f docker-compose.prod.yml up -d --build                      # redeploy after git pull
docker compose -f docker-compose.prod.yml exec postgres pg_dump -U vriddhi vriddhi_auth > auth-backup.sql   # backup (repeat per DB)
```
Never run `docker compose down -v` — it deletes the database volume.
Keep `.env` out of git (already in `.gitignore`) and back up the DBs: a free VM can be reclaimed.
