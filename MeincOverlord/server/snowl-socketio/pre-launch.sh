#!/bin/bash

set -e

if [ -d /app ]; then
    rm -rf /app
fi
mkdir -p "/mnt/host/$NOMAD_ALLOC_INDEX/app"
cd / && ln -sf "/mnt/host/$NOMAD_ALLOC_INDEX/app"

DCLONE_SH="/usr/local/sbin/dclone.sh"
if [ -e "$DCLONE_SH" ]; then
    /bin/bash "$DCLONE_SH"
fi
