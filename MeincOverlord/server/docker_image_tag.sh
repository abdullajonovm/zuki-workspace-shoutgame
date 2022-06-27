#!/bin/bash

if ! command -v jq >&-; then
    echo "Missing required command line utility: jq" >&2
    exit 1
fi
if ! command -v nomad >&-; then
    echo "Missing required command line utility: nomad" >&2
    exit 1
fi
if ! nomad status >/dev/null 2>&1; then
    echo "Nomad must be running" >&2
    exit 1
fi

NOMAD_JOB_FILE_PATH="nomad-job.hcl"
if [ "$1" ]; then
    if [ "$(basename "$1")" == "$NOMAD_JOB_FILE_PATH" ]; then
        NOMAD_JOB_FILE_PATH="$1"
    else
        NOMAD_JOB_FILE_PATH="$(realpath -m "$1/$NOMAD_JOB_FILE_PATH")"
    fi
fi
if [ ! -f "$NOMAD_JOB_FILE_PATH" ]; then
    echo "Usage: $(basename "$0") [nomad-job-path]" >&2
    exit 1
fi

nomad run -output "$NOMAD_JOB_FILE_PATH" \
    | jq -r '.Job.TaskGroups[].Tasks[] | select(.Driver == "docker") | .Config.image' \
    | head -n 1
