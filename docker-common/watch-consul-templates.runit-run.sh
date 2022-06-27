#!/bin/bash

# run scripts suppress stderr so use stdout instead
exec 2>&1

function log {
    echo "$(date "+%Y/%m/%d %H:%M:%S") watch-consul-templates: $1" >&2
}

function read_inotify_events {
    if command -v inotifyd >&-; then
        while read -r EVENT FILE; do
            case "$EVENT" in
                "c" | "M")
                    log "Detected modification to $FILE - signaling consul-template"
                    # c == file was modified
                    # M == file was moved. Which only happens when the file is
                    #      replaced. Manually moving the file only changes its
                    #      metadata. Some editors replace the file with a
                    #      modified copy (atomic write).
                    killall -HUP consul-template
                    ;;
            esac
        done <&3
        exec 3<&-

    elif command -v inotifywait; then
        #TODO implement
        _=
    else
        log "Inotify utilities not available. Exiting."
        exit 1
    fi
}

log "Starting daemon"

function shutdown {
    log "Stopping daemon"
    CHILD_JOB_PIDS=$(jobs -rp)
    kill $CHILD_JOB_PIDS
    wait $CHILD_JOB_PIDS 2>&-
    rm -f "$PIPE_FILE"
}
trap "shutdown" EXIT

if [ ! "$CONSUL_TEMPLATES" ]; then
    log "No consul templates specified. Exiting."
    sv down watch-consul-templates
    exit 1
fi

WATCH_TEMPLATE_FILES=()
for pair_text in $CONSUL_TEMPLATES; do
    IFS=":"; read -ra pair <<<"$pair_text"; unset IFS
    TEMPLATE_SOURCE="${pair[0]}"
    TEMPLATE_TARGET="${pair[1]}"
    WATCH_TEMPLATE_FILES+=("$TEMPLATE_SOURCE")
done

PIPE_FILE="$(mktemp -u)"
if ! mkfifo -m 660 "$PIPE_FILE"; then
    log "Cannot create named pipe at $PIPE_FILE. Exiting."
    exit 1
fi
exec 3<>"$PIPE_FILE"

read_inotify_events &

while true; do
    inotifyd - "${WATCH_TEMPLATE_FILES[@]}" >&3 &
    wait %2 || break
done
log "Inotify terminated abnormally"

exit 1
