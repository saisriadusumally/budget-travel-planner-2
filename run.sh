#!/usr/bin/env bash
# One-command start (Mac/Linux): builds the React app into the backend, then starts everything.
set -e
cd "$(dirname "$0")/frontend" && npm install && npm run build
cd ../backend && mvn spring-boot:run
