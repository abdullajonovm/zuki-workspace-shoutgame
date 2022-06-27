#!/bin/bash

SCRIPT_DIR=$( cd -- "$( dirname -- "${BASH_SOURCE[0]}" )" &> /dev/null && pwd )

if [ -e "sync.sh" ]; then
    ${SCRIPT_DIR}/sync.sh
    STATUS=$?

    if [ $STATUS -ne 0 ]   
    then
        echo "[FATAL] ${BASH_SOURCE[0]}: ./sync.sh aborted!!"
    fi
fi

DOCKER_IMAGE_TAG=$(basename ${SCRIPT_DIR})
cd "$(dirname "${BASH_SOURCE[0]}")"
DOCKER_IMAGE_TAG="${DOCKER_IMAGE_TAG}:1.0"
docker build $DOCKER_OPTS -t "${DOCKER_IMAGE_TAG}" --file Dockerfile-apache . 
