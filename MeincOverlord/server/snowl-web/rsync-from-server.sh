#!/bin/bash

echo -e "This script rsync's the Snowl Admin and Play webapps from a NC server to the local VM.\n"

DOCKER_MNT_PATH=/mnt/host
WWW_MNT_PATH=$DOCKER_MNT_PATH/snowl-web/0/www

if [ ! -d "$WWW_MNT_PATH" ]; then
    echo -e "Run this script inside the dev1 VM\n" >&2
    exit 1
fi

if [ ! -r id_rsa ]; then
    echo -e "Run this script from the path where the id_rsa file for user meinc is located\n" >&2
    exit 1
fi

if [ "$1" = "" ]; then
    echo -e "First argument must be the host to rsync from (e.g. nc10-1 or nc11-1)\n" >&2
    exit 1
fi
NC_HOST="$1"
shift

rsync -ai -e 'ssh -i id_rsa' meinc@$NC_HOST.shoutgameplay.com:$WWW_MNT_PATH/ $WWW_MNT_PATH
