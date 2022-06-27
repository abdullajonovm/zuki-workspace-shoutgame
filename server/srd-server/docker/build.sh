#!/bin/bash

cd "$(dirname "${BASH_SOURCE[0]}")"

set -ex

mkdir -p target
../project/build.sh
cp ../project/target/srd-server target/

docker build -t scm.shoutgameplay.com:5000/srd-server:0.2.1 .
