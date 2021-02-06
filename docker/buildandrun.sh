#!/usr/bin/env bash

echo -e "[INF] \t Building and starting containers (might take some time)"
docker-compose -f project.yml up -d --build --no-color 
echo -e "[INF] \t Docker-compose logs written to compose.log"

