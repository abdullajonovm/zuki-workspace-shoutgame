#!/bin/bash

cd "$(dirname "${BASH_SOURCE[0]}")"
DOCKER_IMAGE_TAG="nginx-lua:1.10.3-alpine-2"
docker build -t ${DOCKER_IMAGE_TAG} -f Dockerfile-local .
