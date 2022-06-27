#!/bin/bash

function log {
    echo "$(date "+%Y/%m/%d %H:%M:%S") dclone: $1"
}

DCLONE_ROOT=/dclone

IFS=$'\x1E'
DCLONE_FILES=$(find "$DCLONE_ROOT" -print0 2>&- | tr "$'\x00'" "$'\x1E'")

for DCLONE_FILE in ${DCLONE_FILES[@]}; do
    TARGET_FILE="${DCLONE_FILE#$DCLONE_ROOT}"
    [ ! "$TARGET_FILE" ] && continue
    if [ -d "$DCLONE_FILE" ]; then
        if [ -e "$TARGET_FILE" ]; then
            log "Exists: $DCLONE_FILE -> $TARGET_FILE"
        else
            log "Cloning: $DCLONE_FILE -> $TARGET_FILE"
            OWNER=$(stat -c '%u:%g' "$DCLONE_FILE")
            PERMS=$(stat -c '%a' "$DCLONE_FILE")
            mkdir -p "$TARGET_FILE"
            chown $OWNER "$TARGET_FILE"
            chmod $PERMS "$TARGET_FILE"
        fi
    elif [ -f "$DCLONE_FILE" ]; then
        if [[ "${OVERWRITE_VOLUME_FILES,,}" != "false" || ! -e "$TARGET_FILE" || "$DCLONE_FILE" -nt "$TARGET_FILE" ]]; then
            log "Cloning: $DCLONE_FILE -> $TARGET_FILE"
            # recursive copy so to preserve symbolic links. BusyBox and GNU versions of cp have
            # incompatible arguments with dealing specifically with symbolic links
            cp -aR $CP_ARG "$DCLONE_FILE" "$TARGET_FILE"
        else
            log "Exists: $DCLONE_FILE -> $TARGET_FILE"
        fi
    else
        log "Cannot clone file $DCLONE_FILE"
    fi
done
unset IFS

mv -f "$DCLONE_ROOT" "$DCLONE_ROOT.cloned" 2>&-
exit 0
