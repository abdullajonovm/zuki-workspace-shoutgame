#!/bin/bash

# DO NOT MODIFY THIS SCRIPT IN ANY PROJECT EXCEPT docker-common OR YOUR
# CHANGES WILL BE OVERWRITTEN!

cd "$(dirname "${BASH_SOURCE[0]}")"

function exit_with_usage {
    cat <<-EOF >&2
		docker-common server not found on localhost port 40080
		  - launch docker-common/webserve.py, or
		  - set DOCKER_COMMON_HOME to the path of the docker-common project
	EOF
    exit 1
}

function launch_webserve {
    echo "Launching docker-common server"
    "$DOCKER_COMMON_HOME/webserve.py" &
    WEBSERVE_PID=$!
    trap "kill $WEBSERVE_PID" TERM
    wait $WEBSERVE_PID 2>&-
}

function replace_build_script {
    if [ -e _build.sh ]; then
        echo "Replacing build script with latest version"
        mv -f _build.sh build.sh
        chmod +x build.sh
    fi
}
trap "replace_build_script" EXIT

function stop_webserve {
    if [ "$LAUNCH_PID" ]; then
        echo "Stopping docker-common server"
        sleep 1
        kill $LAUNCH_PID
        wait $LAUNCH_PID 2>&-
    fi
}

if ! HTTP_TEST=$(curl -sI http://localhost:40080); then
    if [ ! "$DOCKER_COMMON_HOME" ]; then
        exit_with_usage
    fi
    if [ ! -e "$DOCKER_COMMON_HOME/webserve.py" ]; then
        echo -e "error: Invalid DOCKER_COMMON_HOME provided\n" >&2
        exit_with_usage
    fi

    launch_webserve &
    LAUNCH_PID=$!
    trap "stop_webserve;replace_build_script" EXIT
    sleep 1

elif ! grep -q '^Server: docker-common/1.0' <<<"$HTTP_TEST"; then
    exit_with_usage
fi

# Try to keep the build scripts of various projects unified
curl -sO http://localhost:40080/_build.sh

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

if [ -f "pre-build.sh" ]; then
    /bin/bash pre-build.sh
fi

cmd="docker build $DOCKER_OPTS -t "$DOCKER_IMAGE_TAG" ."
echo "+ $cmd"
$cmd
