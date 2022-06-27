#!/bin/bash

SCRIPT_DIR="$(realpath "$(dirname "${BASH_SOURCE[0]}")")"
SYNC_TO="$SCRIPT_DIR"
HTML5_ADMIN_PROJECT="SnowyOwlAdmin"
HTML5_PLAY_PROJECT="SnowyOwlHtml5"
WORKSPACE_HOME=$(realpath "${WORKSPACE_HOME:-"$SCRIPT_DIR/../../.."}")
HTML5_PROJECTS=("${HTML5_ADMIN_PROJECT}" "${HTML5_PLAY_PROJECT}")
#                   Project-Name           Project-Dir  To-Dir
SYNC_FROM_TO_DIRS=("${HTML5_ADMIN_PROJECT} dist         admin"
                   "${HTML5_PLAY_PROJECT}  dist         play"
                   "SnowyowlStaticContent clientimages clientimages")

#
# Change file ownership to avoid sync conflicts
#
function changeowner() {
    local file=$1
    echo "${DEV_SUDO_PASS}" | sudo chown 100:101 $file
}


[ "$1" ] && SYNC_TO="$1"

if [ ! -d "$SYNC_TO/www" ]; then
    echo "$SYNC_TO not found" >&2
    exit 1
fi

for sync_from_to_dir in "${SYNC_FROM_TO_DIRS[@]}"; do
    read -r from_project from_dir to_dir <<<"$sync_from_to_dir"
    if [ ! -d "$WORKSPACE_HOME/$from_project" ]; then        
        echo "$from_project not found at expected path $WORKSPACE_HOME" >&2
        echo "Make sure $from_project is checked out to the workspace or try setting WORKSPACE_HOME" >&2
        echo
        continue
    fi
    if [ ! -d "$WORKSPACE_HOME/$from_project/$from_dir" ]; then
        echo "Found $from_project but nothing in $from_project/$from_dir" >&2
        echo "Make sure $from_project is built before syncing" >&2
        echo
        continue
    fi
    
    
    echo -e "\nSyncing $from_project to $SYNC_TO/www/$to_dir\n"
    rsync -rlptDi --delete "$WORKSPACE_HOME/$from_project/$from_dir/" "$SYNC_TO/www/$to_dir/"
    FILES_TO_COPY=$(find "$SYNC_TO/www/$to_dir" \( -not -user 100 -or -not -group 101 \)) 
    for FILES_TO_COPY in $FILES_TO_COPY
    do
        changeowner $FILES_TO_COPY
    done
    echo
done
