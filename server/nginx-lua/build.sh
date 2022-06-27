#!/bin/bash

cd "$(dirname "${BASH_SOURCE[0]}")"

set -ex

docker build -t scm.shoutgameplay.com:5000/nginx-lua:1.10.3-alpine-2 .
