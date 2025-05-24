#!/bin/bash

echo "Pulling latest code..."
git pull origin main

echo "Starting Docker Compose build..."
docker-compose down
docker-compose up --build -d
