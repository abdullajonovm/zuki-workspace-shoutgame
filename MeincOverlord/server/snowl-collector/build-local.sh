#!/bin/bash

SCRIPT_DIR=$( cd -- "$( dirname -- "${BASH_SOURCE[0]}" )" &> /dev/null && pwd )
DOCKER_IMAGE_TAG=$(basename ${SCRIPT_DIR})
cd "$(dirname "${BASH_SOURCE[0]}")"
DOCKER_IMAGE_TAG="${DOCKER_IMAGE_TAG}:1.0"

if [ -e "sync.sh" ]; then
    ./sync.sh
fi

if [ ! "$(ls deploy/)" ]; then
    echo "No artifacts found in deploy directory. Copy artifacts first." >&2
    exit 1
fi
if [ ! "$(ls osgi/)" ]; then
    echo "No artifacts found in osgi directory. Copy artifacts first." >&2
    exit 1
fi

docker build $DOCKER_OPTS -t "${DOCKER_IMAGE_TAG}" --file Dockerfile-local --pull=false  . 
