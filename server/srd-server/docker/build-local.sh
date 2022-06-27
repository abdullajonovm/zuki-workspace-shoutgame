#!/bin/bash

cd "$(dirname "${BASH_SOURCE[0]}")"

mkdir -p target
../project/build-local.sh
cp ../project/target/srd-server target/

docker build -t srd-server:0.2.1 -f Dockerfile-local . 
