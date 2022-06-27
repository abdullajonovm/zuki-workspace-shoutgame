#!/bin/bash

set -e

SCRIPT_DIR="$(dirname "${BASH_SOURCE[0]}")"

if ! docker status >/dev/null 2>&1; then
    echo "Docker must be installed and running" >&2
    exit 1
fi

if ! rustup --version >/dev/null 2>&1; then
    echo "Rustup must be installed" >&2
    exit 1
fi

RUST_VERSION=1.27.0

cd "$SCRIPT_DIR"

# Make sure the correct toolchain and target are installed
rustup toolchain install $RUST_VERSION
rustup override set $RUST_VERSION
rustup target install --toolchain $RUST_VERSION x86_64-unknown-linux-musl

# Alpine linux doesn't include glibc (it's big). So we will statically link our binary and
# cross-compile to use musl instead of glibc.

# This is the canonical way to cross-compile, but for this project, it only works if you have the
# musl-tools package installed and have manually compiled OpenSSL for musl.
#cargo +$RUST_VERSION build --release --target=x86_64-unknown-linux-musl

# The cross tool takes care of all this, but Docker must be running for it to work
cargo install cross 2>/dev/null || true
cross build --release --target=x86_64-unknown-linux-musl

cp target/x86_64-unknown-linux-musl/release/srd-server target/
