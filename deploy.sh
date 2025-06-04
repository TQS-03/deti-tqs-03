#!/bin/bash

echo "Pulling latest code..."
git pull origin main

echo "Starting Prod Docker Compose build..."
docker-compose down
docker-compose up --build -d

echo "Starting Staging Docker Compose build..."
docker-compose -f docker-compose_test.yml down
docker-compose -f docker-compose_test.yml up --build -d
