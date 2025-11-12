#!/bin/bash

docker build --platform linux/amd64 -t capstone:latest /Users/klatmddud/backend/capstone
docker tag capstone:latest klatmddud/capstone:latest
docker push klatmddud/capstone:latest

