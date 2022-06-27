#!/bin/bash

#
#  Rust toolchain: rustup, cargo and cross
#  Manually build openssl: https://qiita.com/liubin/items/6c94f0b61f746c08b74c
#

SCRIPT_DIR="$(dirname "${BASH_SOURCE[0]}")"
cd "$SCRIPT_DIR"
export OPENSSL_LIB_DIR="/home/rsolano/workspaces/resultier/openssl-1.0.2"
export OPENSSL_INCLUDE_DIR="/home/rsolano/workspaces/resultier/openssl-1.0.2/include"
RUST_VERSION=1.27.0

# Make sure the correct toolchain and target are installed
rustup toolchain install $RUST_VERSION
rustup override set $RUST_VERSION
rustup target install --toolchain $RUST_VERSION x86_64-unknown-linux-gnu

# Alpine linux doesn't include glibc (it's big). So we will statically link our binary and
# cross-compile to use musl instead of glibc.

# This is the canonical way to cross-compile, but for this project, it only works if you have the
# musl-tools package installed and have manually compiled OpenSSL for musl.

cargo +$RUST_VERSION build --release --target=x86_64-unknown-linux-gnu

# The cross tool takes care of all this, but Docker must be running for it to work
# cargo install cross 2>/dev/null || true
# cross build --release --target=x86_64-unknown-linux-musl

cp target/x86_64-unknown-linux-gnu/release/srd-server target/
