#!/bin/bash

echo -e "This script patches the Snowl Admin and Play webapps to request SRD's from dev1.\n"

DOCKER_MNT_PATH=/mnt/host
WWW_MNT_PATH=$DOCKER_MNT_PATH/snowl-web/0/www

if [ ! -d "$WWW_MNT_PATH" ]; then
    echo -e "Run this script inside the dev1 VM\n" >&2
    exit 1
fi

grep -Rl 'snowl-[[:alnum:]-]\+--0--nc[[:digit:]]\+-[[:digit:]]\+' $WWW_MNT_PATH \
    | xargs sed -i'' \
        -e 's#snowl-\([[:alnum:]-]\+\)--0--nc[[:digit:]]\+-[[:digit:]]\+\.shoutgameplay.com#snowl-\1--0--dev1.shoutgameplay.com#g' \
        -e 's#\(https://snowl-[[:alnum:]-]\+--0--dev1.shoutgameplay.com\)\(:[[:digit:]]\+\)\?#\1:8443#g'
