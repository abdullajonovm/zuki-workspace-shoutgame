#!/bin/bash

## This script requires:
##   * You are using an EC2 build server with a EC2 tag of 'SnowlBuildServer=true'
##   * There is a local AWS profile called 'snowl-build' which grants the authority to
##     start and stop this server
##   * On the build server you have installed everything necessary to build and deploy
##     the Maven and Docker projects.

set -e

SCRIPT_DIR="$(dirname "${BASH_SOURCE[0]}")"
SCRIPT_DIR="$(cd "$SCRIPT_DIR"; pwd)"
PROJECT_NAME="$(basename "$SCRIPT_DIR")"

WORKSPACE_HOME=$(realpath "${WORKSPACE_HOME:-"$SCRIPT_DIR/../../.."}")
if [ ! -d "$WORKSPACE_HOME/MeincOverlord" ]; then
    echo "Workspace not found at expected path $WORKSPACE_HOME. Try setting WORKSPACE_HOME." >&2
    exit 1
fi

if [ "$1" = "--build" ]; then
    shift
    if [ "$1" = "--no-delta" ]; then
        shift
        cmd="./build.sh"
    else
        cmd="./build-delta.sh"
    fi
    cd "$WORKSPACE_HOME"/MeincOverlord
    if [ "$1" = "--all" ]; then
        shift
        mvn clean install
        cmd="./build.sh"
    else
        while [ "$1" ]; do
            if [ "$1" != "MeincOverlord" ]; then
                build_projects+=("$1")
            fi
            shift
        done
        for build_project in "${build_projects[@]}"; do
            echo -e "\nBuilding $build_project\n"
            cd "$WORKSPACE_HOME/$build_project"
            mvn clean install
        done
    fi
    cd "$SCRIPT_DIR"
    sudo $cmd
    docker_tag="$("$SCRIPT_DIR/../docker_image_tag.sh" "$SCRIPT_DIR")"
    sudo docker push "$docker_tag"
    echo -e "\nSuccessfully built and pushed $docker_tag"
    exit
fi

if ! command -v nmap >/dev/null; then
    echo "The nmap utility is required (e.g. brew install nmap) (e.g. apt install nmap)" >&2
    exit 1
fi

function query_ec2() {
    if [ "$1" != "-q" ]; then
        echo "Checking build server"
    fi
    # read returns exit status of 1 if it is terminated by the input stream
    read -rd '' query_cmd <<-EOF || true
        aws ec2 describe-instances --profile=snowl-build --region=us-east-1 --output=text \
        --filters='Name=tag:SnowlBuildServer,Values=true' \
        --query='Reservations[0].Instances[0].[InstanceId,State.Name,PublicDnsName]'
	EOF
    build_server="$(/bin/bash -c "$query_cmd")"
    build_server_id="$(awk '{print $1}' <<<"$build_server")"
    build_server_state="$(awk '{print $2}' <<<"$build_server")"
    build_server_host="$(awk '{print $3}' <<<"$build_server")"
}

query_ec2

if [ "$build_server_state" = "stopped" ]; then
    ( set -x
      aws ec2 start-instances --profile=snowl-build --region=us-east-1 \
          --instance-ids="$build_server_id" >/dev/null )
    while [ ! "$build_server_state" = running ]; do
        echo "Waiting for server to start..."
        sleep 3
        query_ec2 -q
    done
    # -Pn    don't scan the host to determine its status
    # -oG -  output to stdout using a grep-able format
    # -p 22  port 22 (ssh)
    while ! nmap -Pn -oG - -p 22 "$build_server_host" | grep -q '/open/'; do
        echo "Waiting for server to boot..."
        sleep 3
    done
fi

if [ "$build_server_state" != "running" ]; then
    echo "Server in unknown state: $build_server_state" >&2
    exit 2
else
    echo "Syncing server project"
    project_dirs=(MeincOverlord
                  MeincLauncher
                  MrSoaKernel
                  MeincCommons
                  MrSoaServiceAssembler
                  ServiceAssemblerPlugin
                  SpringAssemblerPlugin
                  HttpUtils
                  MrSoaSupport)
    project_dirs+=($(awk -F '[ /]' '/^- / {print $2}' <"$SCRIPT_DIR/osgi-artifacts.yml"))
    project_dirs+=($(awk -F '[ /]' '/^- / {print $2}' <"$SCRIPT_DIR/service-artifacts.yml"))
    cd "$WORKSPACE_HOME"
    { rsync_output=$(rsync -e "ssh -o StrictHostKeyChecking=no -o CheckHostIP=no" \
        -ia --delete \
        --exclude '*.gz' \
        --exclude '*.tgz' \
        --exclude '*.tar' \
        --exclude '*.exe' \
        --exclude '*/.bzr' \
        --exclude '*/.vagrant' \
        --exclude '*/terraform' \
        --exclude '*/target' \
        --exclude '*/snowl-collector/deploy/*' \
        --exclude '*/snowl-collector/osgi/*' \
        --exclude '*/snowl-web/www' \
        --exclude '*/MeincOverlord/doc' \
        "${project_dirs[@]}" "meinc@$build_server_host:/build/snowl" | tee /dev/fd/5); } 5>&1

    rsync_projects=($(awk -F '[ /]' '/</ {print $2}' <<<"$rsync_output" | uniq))

    ssh -o StrictHostKeyChecking=no -o CheckHostIP=no "meinc@$build_server_host" \
        "/bin/bash -lc '/build/snowl/MeincOverlord/server/snowl-collector/build-remote.sh --build $@ ${rsync_projects[@]}'"
fi
