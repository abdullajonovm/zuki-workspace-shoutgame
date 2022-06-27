#!/bin/bash

cd "$(dirname "${BASH_SOURCE[0]}")"

if [ "$1" == "--no-cache" ]; then
    DOCKER_OPTS="$DOCKER_OPTS --no-cache"
    shift
fi

if [ "$1" == "-t" ]; then
    DOCKER_IMAGE_TAG="$2"
    shift 2
elif [ -f docker_image_tag ]; then
    DOCKER_IMAGE_TAG="$(<docker_image_tag)"
elif [ -x ../docker_image_tag.sh ]; then
    set -e; DOCKER_IMAGE_TAG="$(../docker_image_tag.sh)"; set +e
else
    echo "Missing -t parameter or docker_image_tag file or ../docker_image_tag.sh script" >&2
    exit 1
fi

set -ex

docker build $DOCKER_OPTS -t "$DOCKER_IMAGE_TAG" . 
