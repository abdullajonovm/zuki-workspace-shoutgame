#!/bin/bash

# DO NOT MODIFY THIS SCRIPT IN ANY PROJECT EXCEPT docker-common OR YOUR
# CHANGES WILL BE OVERWRITTEN!

cd "$(dirname "${BASH_SOURCE[0]}")"

DOCKER_IMAGE_TAG="wds:1.6"

docker build $DOCKER_OPTS -t "${DOCKER_IMAGE_TAG}" . -f Dockerfile-local
