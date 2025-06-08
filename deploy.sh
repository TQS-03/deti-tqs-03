#!/bin/bash

docker buildx prune -f
docker image prune -f
docker container prune -f

echo "Pulling latest code..."
git pull origin main

echo "Starting Prod Docker Compose build..."
docker-compose down
docker-compose up --build -d

echo "Starting Staging Docker Compose build..."
docker-compose -f docker-compose_staging.yml down -v
docker-compose -f docker-compose_staging.yml up --build -d

docker buildx prune -f
