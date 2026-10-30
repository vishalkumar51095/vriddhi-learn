#!/bin/bash
# Creates one database per service on first container start, plus the
# pgcrypto extension each needs for gen_random_uuid() in the Flyway migrations.
set -e

if [ -n "$POSTGRES_MULTIPLE_DATABASES" ]; then
  for db in $(echo "$POSTGRES_MULTIPLE_DATABASES" | tr ',' ' '); do
    if [ "$db" != "$POSTGRES_DB" ]; then
      psql -v ON_ERROR_STOP=1 --username "$POSTGRES_USER" <<-EOSQL
        CREATE DATABASE $db;
EOSQL
    fi
    psql -v ON_ERROR_STOP=1 --username "$POSTGRES_USER" --dbname "$db" <<-EOSQL
      CREATE EXTENSION IF NOT EXISTS pgcrypto;
EOSQL
  done
fi
