#!/bin/bash

SCRIPT_DIR="$(realpath "$(dirname "${BASH_SOURCE[0]}")")"

SYNC_TO="$SCRIPT_DIR"
[ "$1" ] && SYNC_TO="$1"
for sync_dir in "$SYNC_TO/"{deploy,osgi}; do
    if [ ! -d "$sync_dir" ]; then
        echo "$sync_dir not found" >&2
        exit 1
    fi
done

WORKSPACE_HOME=$(realpath "${WORKSPACE_HOME:-"$SCRIPT_DIR/../../.."}")
if [ ! -d "$WORKSPACE_HOME/MeincOverlord" ]; then
    echo "Workspace not found at expected path $WORKSPACE_HOME. Try setting WORKSPACE_HOME." >&2
    exit 1
fi

TMP_SYNC_TO="$SCRIPT_DIR/tmp"
rm -rf "$TMP_SYNC_TO"
mkdir -p "$TMP_SYNC_TO/"{deploy,osgi}

echo -e "\nSyncing service artifacts to $SYNC_TO/deploy\n"

cd "$TMP_SYNC_TO/deploy"
awk '/^- / {print $2}' "$SCRIPT_DIR/service-artifacts.yml" | \
    xargs -I {} ln -s "$WORKSPACE_HOME/{}"

rsync -aiv --delete --omit-dir-times --copy-unsafe-links "$TMP_SYNC_TO/deploy/" "$SYNC_TO/deploy/"

echo -e "\nSyncing OSGi artifacts to $SYNC_TO/osgi\n"

cd "$TMP_SYNC_TO/osgi"
awk '/^- / {print $2}' "$SCRIPT_DIR/osgi-artifacts.yml" | \
    xargs -I {} ln -s "$WORKSPACE_HOME/{}"

rsync -aiv --delete --omit-dir-times --copy-unsafe-links "$TMP_SYNC_TO/osgi/" "$SYNC_TO/osgi/"

cd "$SCRIPT_DIR"
rm -rf "$TMP_SYNC_TO"

if [ -d "$SYNC_TO/conf" ]; then
    echo -e "\nSyncing MrSOA configuration to $SYNC_TO/conf\n"

    rsync -aiv "$SCRIPT_DIR/meinc.properties.ctmpl" \
              "$SYNC_TO/conf/"
    rsync -aiv "$SCRIPT_DIR/mrsoa.xargs" \
              "$SYNC_TO/conf/"
    rsync -aiv "$SCRIPT_DIR/log4j.xml" \
              "$SYNC_TO/conf/"
fi
