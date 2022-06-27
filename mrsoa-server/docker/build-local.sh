#!/bin/bash

# DO NOT MODIFY THIS SCRIPT IN ANY PROJECT EXCEPT docker-common OR YOUR
# CHANGES WILL BE OVERWRITTEN!

cd "$(dirname "${BASH_SOURCE[0]}")"

DOCKER_IMAGE_TAG="$(cat ./docker_image_tag)"
echo ${DOCKER_IMAGE_TAG}
docker build  --tag "${DOCKER_IMAGE_TAG}" --file Dockerfile-local .
