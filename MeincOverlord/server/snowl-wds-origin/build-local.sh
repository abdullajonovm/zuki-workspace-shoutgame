SCRIPT_DIR=$( cd -- "$( dirname -- "${BASH_SOURCE[0]}" )" &> /dev/null && pwd )
DOCKER_IMAGE_TAG=$(basename ${SCRIPT_DIR})
cd "$(dirname "${BASH_SOURCE[0]}")"
DOCKER_IMAGE_TAG="${DOCKER_IMAGE_TAG}:1.0"
docker build $DOCKER_OPTS -t "${DOCKER_IMAGE_TAG}" --file Dockerfile-local . 
