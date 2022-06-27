#!/bin/bash


DIR=$( cd -- "$( dirname -- "${BASH_SOURCE[0]}" )" &> /dev/null && pwd )
ME="$(basename "$(test -L "$0" && readlink "$0" || echo "$0")")"
OVERLORD_PROJECT="${DIR}/MeincOverlord/"

#
# Run maven build
#
function abort_on_error() {
    local status=$1
    local message=$2
    if [ $status -ne 0 ]
    then
        echo $message
        cd ${DIR}
        exit ${status}
    fi
}

#
# Composes scary error messages
#
function fatal_message() {
    local message=$1
    echo "${ME} FATAL: ${message}"
}

#
# Compiles java artifacts (jars)
#
function maven() {
    local project=$1
    local message="$(fatal_message "can't build '${project}' maven project")"

    cd "${project}"
    ./bootstrap.sh    
    abort_on_error $? "${message}"
    cd "${DIR}"
}

#
# Removes ALL docker containers and their master images
#
function prune_docker_images() {
    docker system prune --all --force
    abort_on_error "$?" "Can't clean docker environemnt with 'docker system prune -a'"
}

#
# Rebuild dockers images and leave them in the docker registry.
#
function build_docker_images() {
    local build_scripts_list="${DIR}/build-pipeline.yml"
    local build_scripts=$(egrep  -e "\s+\-\s+\S+" "${build_scripts_list}" | sed -e "s/ - //g")

    for build_script in ${build_scripts}
    do
        build_script="${DIR}/${build_script}"
        ${build_script}
        local status=$?
        
        abort_on_error ${status} "${build_script} failed, aborting build"
    done
}



maven ${OVERLORD_PROJECT}
prune_docker_images 
build_docker_images

cd ${DIR}