#!/bin/bash

function log {
    echo "$(date "+%Y/%m/%d %H:%M:%S") pre-launch: $1"
}

for pre_launch_sh in $(compgen -G "/usr/local/sbin/pre-launch?*.sh" | sort); do
    log "Executing pre-launch script $pre_launch_sh"
    source "$pre_launch_sh"
    if [ $? -ne 0 ]; then
        log "Prelaunch script $pre_launch_sh exited with error - aborting" >&2
        exit 1
    fi
done

if [ "$1" == "-D" ]; then
    WORKING_DIR="$2"
    shift 2
    log "Changing directory to $WORKING_DIR"
    cd "$WORKING_DIR"
fi

if [ "$1" == "-u" ]; then
    EXEC_USER="$2"
    shift 2;
    log "Running as user $EXEC_USER"
fi

if [ "$1" == "-c" ]; then
    shift;
    EXEC_EXE="$1"
    log "Executing $EXEC_EXE"
    if [ "$EXEC_USER" ]; then
        exec su -s /bin/bash -c "$@" "$EXEC_USER"
    else
        exec /bin/bash -c "$@"
    fi
fi
