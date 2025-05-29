#!/bin/bash

set -e

if [ -z "$1" ]; then
  echo "Usage: $0 <docker-image-name>"
  exit 1
fi

IMAGE_NAME="$1"

echo "Updating system packages..."
sudo dnf update -y

echo "Installing Docker..."
sudo dnf install -y docker

echo "Enabling and starting Docker service..."
sudo systemctl enable docker
sudo systemctl start docker

echo "Adding current user to docker group (you may need to log out and back in)..."
sudo usermod -aG docker "$USER"

echo "Docker version:"
docker --version

echo "Pulling Docker image: $IMAGE_NAME"
docker pull "$IMAGE_NAME"

echo "Running Docker container from image: $IMAGE_NAME"
#docker run "$IMAGE_NAME"
